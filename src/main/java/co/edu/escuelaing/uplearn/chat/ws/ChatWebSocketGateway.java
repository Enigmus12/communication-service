package co.edu.escuelaing.uplearn.chat.ws;

import co.edu.escuelaing.uplearn.chat.domain.Message;
import co.edu.escuelaing.uplearn.chat.dto.ChatMessageData;
import co.edu.escuelaing.uplearn.chat.dto.SendMessageRequest;
import co.edu.escuelaing.uplearn.chat.service.AuthorizationService;
import co.edu.escuelaing.uplearn.chat.service.ChatService;
import co.edu.escuelaing.uplearn.chat.service.ReservationClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Gateway WebSocket para la funcionalidad de chat. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketGateway extends TextWebSocketHandler {

    private static final String ATTR_USER_ID = "userId";
    private static final String QUERY_PARAM_TOKEN = "token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthorizationService authz;
    private final ChatService chatService;
    private final ReservationClient reservations;
    private final ObjectMapper json = new ObjectMapper();
    private final StringRedisTemplate redis;
    private final RedisMessageListenerContainer container;

    // Mapa de sesiones activas por usuario
    private final Map<String, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    /** Inicializar listener de Redis para mensajes de chat */
    @PostConstruct
    public void initRedisListener() {
        container.addMessageListener((message, pattern) -> {
            try {
                String payload = new String(message.getBody());
                ChatMessageData dto = json.readValue(payload, ChatMessageData.class);
                // Entregar a ambos participantes conectados
                deliverTo(dto.getToUserId(), payload);
                deliverTo(dto.getFromUserId(), payload);
            } catch (Exception e) {
                log.error("Error procesando mensaje Redis: {}", e.toString());
            }
        }, new PatternTopic("chat:*"));
    }

    /**
     * Entregar mensaje serializado a todas las sesiones activas de un usuario
     *
     * @param userId         el ID del usuario
     * @param serializedJson el mensaje serializado en JSON
     */
    private void deliverTo(String userId, String serializedJson) {
        var sessions = sessionsByUser.getOrDefault(userId, Collections.emptySet());
        for (var s : sessions) {
            try {
                s.sendMessage(new TextMessage(serializedJson));
            } catch (IOException e) {
                log.debug("Ignoring IOException sending to user {}: {}", userId, e.toString());
            }
        }
    }

    /**
     * Manejar nueva conexión WebSocket
     *
     * @param session la sesión WebSocket establecida
     * @throws Exception en caso de error
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst(QUERY_PARAM_TOKEN);
        if (token == null || token.isBlank()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Falta token"));
            return;
        }

        String userId;
        try {
            userId = authz.subject(BEARER_PREFIX + token);
        } catch (Exception e) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token inválido"));
            return;
        }

        session.getAttributes().put(ATTR_USER_ID, userId);
        sessionsByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WS conectado userId={} sessions={}", userId, sessionsByUser.get(userId).size());

        // Entregar pendientes
        var pending = chatService.pendingFor(userId);
        if (!pending.isEmpty()) {
            for (Message m : pending) {
                try {
                    ChatMessageData dto = chatService.toDto(m);
                    session.sendMessage(new TextMessage(json.writeValueAsString(dto)));
                } catch (Exception ex) {
                    log.error("Error enviando mensaje pendiente {} a {}: {}", m.getId(), userId, ex.toString(), ex);
                }
            }
            chatService.markDelivered(pending);
        }
    }

    /**
     * Manejar mensaje entrante por WebSocket
     *
     * @param session la sesión WebSocket
     * @param message el mensaje recibido
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get(ATTR_USER_ID);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("No autenticado"));
            return;
        }

        final String payload = message.getPayload();
        if (payload == null || payload.isBlank()) {
            return;
        }

        if ("ping".equalsIgnoreCase(payload.trim())) {
            return;
        }

        JsonNode root;
        try {
            root = json.readTree(payload);
        } catch (Exception ex) {
            log.debug("WS: ignorando payload no JSON: {}", payload);
            return;
        }

        if (root.has("type") && "ping".equalsIgnoreCase(root.get("type").asText())) {
            return;
        }

        SendMessageRequest req = json.treeToValue(root, SendMessageRequest.class);
        String toUserId = req.getToUserId();
        String content = req.getContent();
        if (toUserId == null || toUserId.isBlank() || content == null || content.isBlank()) {
            log.warn("WS: payload inválido, faltan campos requeridos: {}", payload);
            return;
        }

        String token = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst(QUERY_PARAM_TOKEN);
        String bearer = BEARER_PREFIX + token;

        if (!reservations.canChat(bearer, toUserId)) {
            log.warn("Bloqueado intento de chat entre {} y {} sin reservas válidas", userId, toUserId);
            session.sendMessage(new TextMessage(json.writeValueAsString(
                    Map.of("error", "No autorizado para chatear"))));
            return;
        }

        String chatId = chatService.chatIdOf(userId, toUserId);
        chatService.ensureChat(userId, toUserId);
        Message saved = chatService.saveMessage(chatId, userId, toUserId, content);
        ChatMessageData dto = chatService.toDto(saved);

        String serialized = json.writeValueAsString(dto);
        redis.convertAndSend("chat:" + chatId, serialized);
    }

    /**
     * Manejar cierre de conexión WebSocket
     *
     * @param session la sesión WebSocket cerrada
     * @param status  el estado de cierre
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get(ATTR_USER_ID);
        if (userId != null) {
            var set = sessionsByUser.get(userId);
            if (set != null) {
                set.remove(session);
                if (set.isEmpty()) {
                    sessionsByUser.remove(userId);
                }
            }
        }
    }
}

package co.edu.escuelaing.uplearn.chat.ws;

import co.edu.escuelaing.uplearn.chat.domain.Message;
import co.edu.escuelaing.uplearn.chat.dto.ChatMessageData;
import co.edu.escuelaing.uplearn.chat.dto.SendMessageRequest;
import co.edu.escuelaing.uplearn.chat.service.AuthorizationService;
import co.edu.escuelaing.uplearn.chat.service.ChatService;
import co.edu.escuelaing.uplearn.chat.service.ReservationClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketGateway extends TextWebSocketHandler {

    private final AuthorizationService authz;
    private final ChatService chatService;
    private final ReservationClient reservations;
    private final ObjectMapper json = new ObjectMapper();
    private final StringRedisTemplate redis;
    private final RedisMessageListenerContainer container;

    // Mapa de sesiones activas por usuario
    private final Map<String, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

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

    private void deliverTo(String userId, String serializedJson) {
        var sessions = sessionsByUser.getOrDefault(userId, Collections.emptySet());
        for (var s : sessions) {
            try {
                s.sendMessage(new TextMessage(serializedJson));
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Validar token (viene como query param token=?)
        String token = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().getFirst("token");
        if (token == null || token.isBlank()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Falta token"));
            return;
        }
        String userId;
        try {
            userId = authz.subject("Bearer " + token);
        } catch (Exception e) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token inválido"));
            return;
        }
        session.getAttributes().put("userId", userId);
        sessionsByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WS conectado userId={} sessions={}", userId, sessionsByUser.get(userId).size());

        // Entregar pendientes
        var pending = chatService.pendingFor(userId);
        if (!pending.isEmpty()) {
            for (Message m : pending) {
                ChatMessageData dto = ChatService.toDto(m);
                session.sendMessage(new TextMessage(json.writeValueAsString(dto)));
            }
            chatService.markDelivered(pending);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("No autenticado"));
            return;
        }
        SendMessageRequest req = json.readValue(message.getPayload(), SendMessageRequest.class);
        String toUserId = req.getToUserId();
        String content = req.getContent();

        // Validar permiso usando reservation-service
        String bearer = "Bearer "
                + UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().getFirst("token");
        if (!reservations.canChat(bearer, toUserId)) {
            log.warn("Bloqueado intento de chat entre {} y {} sin reservas válidas", userId, toUserId);
            session.sendMessage(new TextMessage(
                    json.writeValueAsString(java.util.Map.of("error", "No autorizado para chatear"))));
            return;
        }

        // Guardar y publicar
        String chatId = chatService.chatIdOf(userId, toUserId);
        chatService.ensureChat(userId, toUserId);
        Message saved = chatService.saveMessage(chatId, userId, toUserId, content);
        ChatMessageData dto = ChatService.toDto(saved);

        String serialized = json.writeValueAsString(dto);
        redis.convertAndSend("chat:" + chatId, serialized);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            var set = sessionsByUser.get(userId);
            if (set != null) {
                set.remove(session);
                if (set.isEmpty())
                    sessionsByUser.remove(userId);
            }
        }
    }

    @FunctionalInterface
    interface MessageListener {
        void onMessage(org.springframework.data.redis.connection.Message channel,
                org.springframework.data.redis.connection.Message message);
    }
}

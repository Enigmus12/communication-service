package co.edu.escuelaing.uplearn.chat.controller;

import co.edu.escuelaing.uplearn.chat.domain.Message;
import co.edu.escuelaing.uplearn.chat.dto.ChatContact;
import co.edu.escuelaing.uplearn.chat.dto.ChatMessageData;
import co.edu.escuelaing.uplearn.chat.dto.PublicProfile;
import co.edu.escuelaing.uplearn.chat.service.AuthorizationService;
import co.edu.escuelaing.uplearn.chat.service.ChatService;
import co.edu.escuelaing.uplearn.chat.service.ReservationClient;
import co.edu.escuelaing.uplearn.chat.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AuthorizationService authz;
    private final ChatService chat;
    private final ReservationClient reservations;
    private final UserServiceClient users;

    /**
     * Lista de contactos con los que el usuario autenticado tiene reservas válidas.
     */
    @GetMapping("/contacts")
    public List<ChatContact> contacts(@RequestHeader("Authorization") String authorization) {
        var me = authz.me(authorization);
        String myId = me.getId();

        Set<String> ids = reservations.counterpartIds(authorization, myId);

        return ids.stream().map(id -> {
            PublicProfile p = users.getPublicProfileById(id);
            if (p == null) {
                p = PublicProfile.builder().id(id).name("Usuario").email("").build();
            }
            return ChatContact.builder()
                    .id(p.getId())
                    .sub(p.getSub())
                    .name(p.getName())
                    .email(p.getEmail())
                    .avatarUrl(p.getAvatarUrl())
                    .build();
        }).toList();
    }

    /**
     * Historial de mensajes para un chat concreto.
     * Se intenta mapear cada mensaje; si alguno falla, se loguea y se omite
     * para evitar que un solo registro corrupto rompa todo el endpoint.
     */
    @GetMapping("/history/{chatId}")
    public ResponseEntity<?> history(
            @PathVariable("chatId") String chatId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            List<Message> raw = chat.history(chatId);
            if (raw == null) {
                return ResponseEntity.ok(List.of());
            }
            List<ChatMessageData> out = new ArrayList<>(raw.size());
            for (Message m : raw) {
                try {
                    out.add(chat.toDto(m));
                } catch (Exception ex) {
                    String mid = safeMessageId(m);
                    log.error("Error convirtiendo mensaje {} de chat {}: {}", mid, chatId, ex.toString(), ex);
                }
            }
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            log.error("Error cargando historial para chat {}: {}", chatId, e.toString(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error cargando historial del chat", "details", e.getMessage()));
        }
    }

    /** 
     * Obtener un ID de mensaje seguro para logging 
     */
    private String safeMessageId(Message m) {
        try {
            if (m != null && m.getId() != null) {
                return m.getId();
            }
        } catch (Exception ex) {
            log.debug("Ignored error obtaining message id", ex);
        }
        return "<unknown>";
    }

    /**
     * Calcula el chatId entre el usuario autenticado y otro usuario.
     */
    @GetMapping("/chat-id/with/{otherUserId}")
    public Map<String, String> chatId(
            @PathVariable("otherUserId") String otherUserId,
            @RequestHeader("Authorization") String authorization) {

        String meId = authz.subject(authorization);
        String chatId = chat.chatIdOf(meId, otherUserId);

        return Map.of(
                "chatId", chatId,
                "meId", meId);
    }
}

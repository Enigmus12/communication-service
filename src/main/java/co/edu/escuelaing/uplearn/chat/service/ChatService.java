package co.edu.escuelaing.uplearn.chat.service;

import co.edu.escuelaing.uplearn.chat.domain.Chat;
import co.edu.escuelaing.uplearn.chat.domain.Message;
import co.edu.escuelaing.uplearn.chat.dto.ChatMessageData;
import co.edu.escuelaing.uplearn.chat.repository.ChatRepository;
import co.edu.escuelaing.uplearn.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chats;
    private final MessageRepository messages;
    /** Generar un chatId único y consistente para dos usuarios */
    public String chatIdOf(String a, String b) {
        String userA = a.compareTo(b) <= 0 ? a : b;
        String userB = a.compareTo(b) <= 0 ? b : a;
        String key = userA + ":" + userB;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte d : digest) sb.append(String.format("%02x", d));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /** Asegurar que un chat entre dos usuarios existe, creándolo si es necesario */
    public Chat ensureChat(String a, String b) {
        String userA = a.compareTo(b) <= 0 ? a : b;
        String userB = a.compareTo(b) <= 0 ? b : a;
        return chats.findByUserAAndUserB(userA, userB)
                .orElseGet(() -> chats.save(Chat.builder()
                        .id(chatIdOf(a,b))
                        .userA(userA).userB(userB)
                        .participants(Set.of(userA,userB))
                        .createdAt(Instant.now())
                        .build()));
    }
    /** Guardar un mensaje en un chat */
    public Message saveMessage(String chatId, String from, String to, String content) {
        Message msg = Message.builder()
                .chatId(chatId)
                .fromUserId(from)
                .toUserId(to)
                .content(content)
                .createdAt(Instant.now())
                .delivered(false)
                .read(false)
                .build();
        return messages.save(msg);
    }
    /** Obtener el historial de mensajes de un chat */
    public List<Message> history(String chatId) {
        return messages.findByChatIdOrderByCreatedAtAsc(chatId);
    }

    /** Obtener los mensajes pendientes de entrega para un usuario */
    public List<Message> pendingFor(String userId) {
        return messages.findByToUserIdAndDeliveredIsFalseOrderByCreatedAtAsc(userId);
    }
    /** Marcar una lista de mensajes como entregados */
    public void markDelivered(List<Message> list) {
        list.forEach(m -> m.setDelivered(true));
        messages.saveAll(list);
    }
    /** Convertir una entidad Message a DTO ChatMessageData */
    public static ChatMessageData toDto(Message m) {
        return ChatMessageData.builder()
                .id(m.getId())
                .chatId(m.getChatId())
                .fromUserId(m.getFromUserId())
                .toUserId(m.getToUserId())
                .content(m.getContent())
                .createdAt(m.getCreatedAt().toString())
                .delivered(m.isDelivered())
                .read(m.isRead())
                .build();
    }
}

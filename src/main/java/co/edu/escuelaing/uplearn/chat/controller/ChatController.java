package co.edu.escuelaing.uplearn.chat.controller;

import co.edu.escuelaing.uplearn.chat.dto.ChatContact;
import co.edu.escuelaing.uplearn.chat.dto.PublicProfile;
import co.edu.escuelaing.uplearn.chat.service.AuthorizationService;
import co.edu.escuelaing.uplearn.chat.service.ChatService;
import co.edu.escuelaing.uplearn.chat.service.ReservationClient;
import co.edu.escuelaing.uplearn.chat.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.edu.escuelaing.uplearn.chat.dto.ChatMessageData;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AuthorizationService authz;
    private final ChatService chat;
    private final ReservationClient reservations;
    private final UserServiceClient users;
    /**
     * Retrieves the chat contacts for the authenticated user.
     *
     * @param authorization The authorization header containing the user's token.
     * @return A list of ChatContact objects representing the user's chat contacts.
     */
    @GetMapping("/contacts")
    public List<ChatContact> contacts(@RequestHeader("Authorization") String authorization) {
        var me = authz.me(authorization);
        String myId = me.getId();

        Set<String> ids = reservations.counterpartIds(authorization, myId);

        return ids.stream().map(id -> {
            PublicProfile p = users.getPublicProfileById(id);
            if (p == null)
                p = PublicProfile.builder().id(id).name("Usuario").email("").build();
            return ChatContact.builder()
                    .id(p.getId())
                    .sub(p.getSub())
                    .name(p.getName())
                    .email(p.getEmail())
                    .avatarUrl(p.getAvatarUrl())
                    .build();
        }).collect(Collectors.toList());
    }
    /**
     * Retrieves the chat history for a specific chat.
     *
     * @param chatId        The ID of the chat whose history is to be retrieved.
     * @param authorization The authorization header containing the user's token.
     * @return A list of ChatMessageData objects representing the chat history.
     */
    @GetMapping("/history/{chatId}")
    public List<ChatMessageData> history(@PathVariable String chatId,
                                         @RequestHeader("Authorization") String authorization) {
        return chat.history(chatId).stream()
                .map(co.edu.escuelaing.uplearn.chat.service.ChatService::toDto)
                .toList();
    }
    /**
     * Retrieves the chat ID for a chat between the authenticated user and another user.
     *
     * @param otherUserId   The ID of the other user in the chat.
     * @param authorization The authorization header containing the user's token.
     * @return A map containing the chat ID and the authenticated user's ID.
     */
    @GetMapping("/chat-id/with/{otherUserId}")
    public Map<String, String> chatId(@PathVariable String otherUserId,
                                      @RequestHeader("Authorization") String authorization) {
        String meId;
        try {
            meId = authz.me(authorization).getId(); 
        } catch (Exception e) {
            meId = authz.subject(authorization);
        }
        return Map.of("chatId", chat.chatIdOf(meId, otherUserId), "meId", meId);
    }
}

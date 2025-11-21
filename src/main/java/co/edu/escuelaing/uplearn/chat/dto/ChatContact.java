package co.edu.escuelaing.uplearn.chat.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatContact {
    private String id;         // user id
    private String sub;        // alias
    private String name;
    private String email;
    private String avatarUrl;
}

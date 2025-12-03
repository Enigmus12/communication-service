package co.edu.escuelaing.uplearn.chat.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatContact {
    private String id;         
    private String sub;       
    private String name;
    private String email;
    private String avatarUrl;
}

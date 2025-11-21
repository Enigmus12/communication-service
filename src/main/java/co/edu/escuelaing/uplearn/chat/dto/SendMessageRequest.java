package co.edu.escuelaing.uplearn.chat.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SendMessageRequest {
    private String toUserId;
    private String content;
}

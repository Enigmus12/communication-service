package co.edu.escuelaing.uplearn.chat.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SendMessageRequest {
    private String toUserId;
    private String content;
}

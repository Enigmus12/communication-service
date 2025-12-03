package co.edu.escuelaing.uplearn.chat.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("chats")
public class Chat {
    @Id
    private String id;                  
    @Indexed private String userA;     
    @Indexed private String userB;      
    private Instant createdAt;
    private Set<String> participants;   
}

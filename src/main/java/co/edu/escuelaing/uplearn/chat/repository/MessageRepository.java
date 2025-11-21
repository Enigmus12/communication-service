package co.edu.escuelaing.uplearn.chat.repository;

import co.edu.escuelaing.uplearn.chat.domain.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
/**
 * Repository interface for managing Message entities in MongoDB.
 */
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByChatIdOrderByCreatedAtAsc(String chatId);
    List<Message> findByToUserIdAndDeliveredIsFalseOrderByCreatedAtAsc(String toUserId);
}

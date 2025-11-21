package co.edu.escuelaing.uplearn.chat.repository;

import co.edu.escuelaing.uplearn.chat.domain.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
/**
 * Repository interface for managing Chat entities in MongoDB.
 */
public interface ChatRepository extends MongoRepository<Chat, String> {
    Optional<Chat> findByUserAAndUserB(String userA, String userB);
}

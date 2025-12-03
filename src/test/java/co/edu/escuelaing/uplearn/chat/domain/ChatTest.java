package co.edu.escuelaing.uplearn.chat.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChatTest {

    @Test
    void builder_y_getters_OK() {
        Instant now = Instant.now();
        Set<String> participants = Set.of("u1", "u2");

        Chat chat = Chat.builder()
                .id("cid")
                .userA("u1")
                .userB("u2")
                .createdAt(now)
                .participants(participants)
                .build();

        assertEquals("cid", chat.getId());
        assertEquals("u1", chat.getUserA());
        assertEquals("u2", chat.getUserB());
        assertEquals(now, chat.getCreatedAt());
        assertEquals(participants, chat.getParticipants());
        assertTrue(chat.toString().contains("cid"));
    }

    @Test
    void equals_y_hashCode_OK_y_FAIL() {
        Instant now = Instant.now();
        Set<String> participants = Set.of("u1", "u2");

        Chat c1 = Chat.builder()
                .id("cid")
                .userA("u1")
                .userB("u2")
                .createdAt(now)
                .participants(participants)
                .build();

        Chat c2 = new Chat();
        c2.setId("cid");
        c2.setUserA("u1");
        c2.setUserB("u2");
        c2.setCreatedAt(now);
        c2.setParticipants(participants);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        Chat different = Chat.builder().id("other").userA("u1").userB("u2").build();
        assertNotEquals(c1, different);
        assertNotEquals(null, c1);
        assertNotEquals("not-a-chat", c1);
    }
}

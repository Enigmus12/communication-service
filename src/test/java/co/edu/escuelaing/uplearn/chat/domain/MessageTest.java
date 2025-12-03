package co.edu.escuelaing.uplearn.chat.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void builder_y_getters_OK() {
        Instant now = Instant.now();

        Message msg = Message.builder()
                .id("m1")
                .chatId("c1")
                .fromUserId("u1")
                .toUserId("u2")
                .content("hola")
                .createdAt(now)
                .delivered(true)
                .read(false)
                .build();

        assertEquals("m1", msg.getId());
        assertEquals("c1", msg.getChatId());
        assertEquals("u1", msg.getFromUserId());
        assertEquals("u2", msg.getToUserId());
        assertEquals("hola", msg.getContent());
        assertEquals(now, msg.getCreatedAt());
        assertTrue(msg.isDelivered());
        assertFalse(msg.isRead());
        assertTrue(msg.toString().contains("m1"));
    }

    @Test
    void equals_y_hashCode_OK_y_FAIL() {
        Instant now = Instant.now();

        Message m1 = Message.builder()
                .id("m1").chatId("c1")
                .fromUserId("u1").toUserId("u2")
                .content("hola").createdAt(now)
                .delivered(true).read(false)
                .build();

        Message m2 = new Message();
        m2.setId("m1");
        m2.setChatId("c1");
        m2.setFromUserId("u1");
        m2.setToUserId("u2");
        m2.setContent("hola");
        m2.setCreatedAt(now);
        m2.setDelivered(true);
        m2.setRead(false);

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());

        Message diff = Message.builder().id("m2").build();
        assertNotEquals(m1, diff);
        assertNotEquals(null, m1);
        assertNotEquals("not-a-message", m1);
    }
}

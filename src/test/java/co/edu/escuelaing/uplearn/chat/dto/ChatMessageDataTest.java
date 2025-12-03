package co.edu.escuelaing.uplearn.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageDataTest {

    @Test
    void builder_y_getters_OK() {
        ChatMessageData d = ChatMessageData.builder()
                .id("m1")
                .chatId("c1")
                .fromUserId("u1")
                .toUserId("u2")
                .content("hola")
                .createdAt("now")
                .delivered(true)
                .read(false)
                .build();

        assertEquals("m1", d.getId());
        assertEquals("c1", d.getChatId());
        assertEquals("u1", d.getFromUserId());
        assertEquals("u2", d.getToUserId());
        assertEquals("hola", d.getContent());
        assertEquals("now", d.getCreatedAt());
        assertTrue(d.isDelivered());
        assertFalse(d.isRead());
        assertTrue(d.toString().contains("m1"));
    }

    @Test
    void equals_y_hashCode_OK_y_FAIL() {
        ChatMessageData d1 = new ChatMessageData("m1", "c1", "u1", "u2",
                "hola", "now", true, false);
        ChatMessageData d2 = new ChatMessageData("m1", "c1", "u1", "u2",
                "hola", "now", true, false);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());

        ChatMessageData diff = ChatMessageData.builder().id("other").build();
        assertNotEquals(d1, diff);
        assertNotEquals(null, d1);
        assertNotEquals("not-msg", d1);
    }
}

package co.edu.escuelaing.uplearn.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SendMessageRequestTest {

    @Test
    void builder_y_getters_OK() {
        SendMessageRequest req = SendMessageRequest.builder()
                .toUserId("u2")
                .content("hola")
                .build();

        assertEquals("u2", req.getToUserId());
        assertEquals("hola", req.getContent());
        assertTrue(req.toString().contains("hola"));
    }

    @Test
    void equals_y_hashCode_OK_y_FAIL() {
        SendMessageRequest r1 = new SendMessageRequest("u2", "hola");
        SendMessageRequest r2 = new SendMessageRequest("u2", "hola");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        SendMessageRequest diff = SendMessageRequest.builder()
                .toUserId("other")
                .content("x")
                .build();

        assertNotEquals(diff, r1);
        assertNotEquals(null, r1);
        assertNotEquals("not-req", r1);
    }
}

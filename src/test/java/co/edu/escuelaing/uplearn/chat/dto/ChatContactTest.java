package co.edu.escuelaing.uplearn.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatContactTest {

    @Test
    void builder_y_getters_OK() {
        ChatContact c = ChatContact.builder()
                .id("1")
                .sub("sub")
                .name("Name")
                .email("user@mail.com")
                .avatarUrl("http://avatar")
                .build();

        assertEquals("1", c.getId());
        assertEquals("sub", c.getSub());
        assertEquals("Name", c.getName());
        assertEquals("user@mail.com", c.getEmail());
        assertEquals("http://avatar", c.getAvatarUrl());
        assertTrue(c.toString().contains("Name"));
    }

    @Test
    void equals_y_hashCode_OK_y_FAIL() {
        ChatContact c1 = new ChatContact("1", "sub", "Name", "mail", "url");
        ChatContact c2 = new ChatContact("1", "sub", "Name", "mail", "url");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        ChatContact diff = ChatContact.builder().id("2").build();
        assertNotEquals(c1, diff);
        assertNotEquals(null, c1);
        assertNotEquals("not-contact", c1);
    }
}

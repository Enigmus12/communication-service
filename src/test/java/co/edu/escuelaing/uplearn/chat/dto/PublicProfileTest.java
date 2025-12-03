package co.edu.escuelaing.uplearn.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PublicProfileTest {

    @Test
    void builder_y_getters_OK() {
        PublicProfile p = PublicProfile.builder()
                .id("id")
                .sub("sub")
                .name("Name")
                .email("mail")
                .avatarUrl("url")
                .build();

        assertEquals("id", p.getId());
        assertEquals("sub", p.getSub());
        assertEquals("Name", p.getName());
        assertEquals("mail", p.getEmail());
        assertEquals("url", p.getAvatarUrl());
        assertTrue(p.toString().contains("Name"));
    }

    @Test
    void equals_y_hashCode_OK_y_FAIL() {
        PublicProfile p1 = new PublicProfile("id", "sub", "Name", "e", "url");
        PublicProfile p2 = new PublicProfile("id", "sub", "Name", "e", "url");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        PublicProfile diff = PublicProfile.builder().id("other").build();
        assertNotEquals(p1, diff);
        assertNotEquals(null, p1);
        assertNotEquals("not-prof", p1);
    }
}

package co.edu.escuelaing.uplearn.chat.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RolesResponseTest {

    @Test
    void getters_y_setters_OK() {
        RolesResponse r = new RolesResponse();
        r.setId("id");
        r.setEmail("mail");
        r.setName("name");
        r.setRoles(List.of("STUDENT", "TUTOR"));
        r.setHasRoles(true);
        r.setLastUpdated("now");

        assertEquals("id", r.getId());
        assertEquals("mail", r.getEmail());
        assertEquals("name", r.getName());
        assertEquals(List.of("STUDENT", "TUTOR"), r.getRoles());
        assertTrue(r.isHasRoles());
        assertEquals("now", r.getLastUpdated());
        assertTrue(r.toString().contains("id"));
    }

    @Test
    void equals_y_hashCode_OK_y_FAIL() {
        RolesResponse r1 = new RolesResponse();
        r1.setId("id");
        RolesResponse r2 = new RolesResponse();
        r2.setId("id");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        RolesResponse diff = new RolesResponse();
        diff.setId("other");
        assertNotEquals(r1, diff);
        assertNotEquals(null, r1);
        assertNotEquals("not-roles", r1);
    }
}

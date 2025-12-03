package co.edu.escuelaing.uplearn.chat.config;

import co.edu.escuelaing.uplearn.chat.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class WebConfigTest {

    @Test
    void addCorsMappings_origenesDesdePropiedad_OK1() {
        WebConfig cfg = new WebConfig();
        TestUtils.setField(cfg, "allowed", "http://a.com,http://b.com");

        CorsRegistry reg = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class, RETURNS_SELF);

        when(reg.addMapping("/**")).thenReturn(registration);

        cfg.addCorsMappings(reg);

        verify(reg).addMapping("/**");
        verify(registration).allowedOrigins("http://a.com", "http://b.com");
    }

    @Test
    void addCorsMappings_metodosHeadersCredenciales_OK2() {
        WebConfig cfg = new WebConfig();
        TestUtils.setField(cfg, "allowed", "http://a.com");

        CorsRegistry reg = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class, RETURNS_SELF);
        when(reg.addMapping("/**")).thenReturn(registration);

        cfg.addCorsMappings(reg);

        verify(reg).addMapping("/**");
        verify(registration).allowedOrigins("http://a.com");
        verify(registration).allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS");
        verify(registration).allowedHeaders("*");
        verify(registration).allowCredentials(true);
    }

    @Test
    void addCorsMappings_propiedadVacia_FAIL1() {
        WebConfig cfg = new WebConfig();
        TestUtils.setField(cfg, "allowed", "");

        CorsRegistry reg = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class, RETURNS_SELF);
        when(reg.addMapping("/**")).thenReturn(registration);

        cfg.addCorsMappings(reg);

        verify(registration).allowedOrigins("");
    }

    @Test
    void addCorsMappings_propiedadNull_FAIL2() {
        WebConfig cfg = new WebConfig();
        CorsRegistry reg = mock(CorsRegistry.class);
        assertThrows(NullPointerException.class, () -> cfg.addCorsMappings(reg));
    }
}

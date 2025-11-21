package co.edu.escuelaing.uplearn.chat.service;

import co.edu.escuelaing.uplearn.chat.dto.RolesResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserServiceClient client;
    /** Obtener información del usuario autenticado */
    public RolesResponse me(String bearer) {
        return client.getMyRolesCached(bearer);
    }

    /** Verificar que el usuario autenticado tiene el rol requerido */
    public void requireRole(String bearer, String role) {
        var me = me(bearer);
        boolean ok = me != null && me.getRoles() != null &&
                me.getRoles().stream().map(r -> r==null? null : r.toUpperCase(Locale.ROOT))
                        .anyMatch(r -> role.equalsIgnoreCase(r));
        if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
    }

    /** Extraer el "sub" del JWT */
    public String subject(String bearer) {
        String token = extractToken(bearer);
        try {
            String payload = token.split("\\.")[1];
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            JsonNode json = new ObjectMapper().readTree(new String(decoded, StandardCharsets.UTF_8));
            return json.get("sub").asText();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }
    /** Extraer el token del header Authorization */
    private static String extractToken(String bearer) {
        if (bearer == null || bearer.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta Authorization");
        String b = bearer.trim();
        if (b.toLowerCase(Locale.ROOT).startsWith("bearer "))
            b = b.substring(7).trim();
        return b;
    }
}

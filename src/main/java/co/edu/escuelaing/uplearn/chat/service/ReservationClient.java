package co.edu.escuelaing.uplearn.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@Component @Slf4j
public class ReservationClient {

    private final WebClient http;
   
    public ReservationClient(@Value("${reservations.api.base}") String base) {
        this.http = WebClient.builder().baseUrl(base).build();
    }
    /** Verificar si el usuario autenticado puede chatear con otro usuario */
    public boolean canChat(String bearer, String withUserId) {
        try {
            Map resp = http.get()
                    .uri(uriBuilder -> uriBuilder.path("/can-chat").queryParam("withUserId", withUserId).build())
                    .header(HttpHeaders.AUTHORIZATION, bearer)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(Map.class).block();
            return resp != null && Boolean.TRUE.equals(resp.get("canChat"));
        } catch (WebClientResponseException e) {
            log.warn("canChat error {} {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.warn("canChat error {}", e.toString());
            return false;
        }
    }

    /** IDs de contrapartes válidas (reservas ACEPTADO/INCUMPLIDA) para el usuario autenticado */
    public Set<String> counterpartIds(String bearer, String myId) {
        Set<String> out = new HashSet<>();
        try {
            // Como ESTUDIANTE
            List<Map> my = http.get()
                    .uri("/my")
                    .header(HttpHeaders.AUTHORIZATION, bearer)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToFlux(Map.class).collectList().block();
            if (my != null) {
                for (Map r : my) {
                    String status = String.valueOf(r.getOrDefault("status",""));
                    if (status.equalsIgnoreCase("ACEPTADO") || status.equalsIgnoreCase("INCUMPLIDA")) {
                        String tutorId = (String) r.get("tutorId");
                        if (tutorId != null && !tutorId.equals(myId)) out.add(tutorId);
                    }
                }
            }
        } catch (Exception e) { log.debug("reservations/my fallo: {}", e.toString()); }

        try {
            // Como TUTOR
            List<Map> forMe = http.get()
                    .uri("/for-me")
                    .header(HttpHeaders.AUTHORIZATION, bearer)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToFlux(Map.class).collectList().block();
            if (forMe != null) {
                for (Map r : forMe) {
                    String status = String.valueOf(r.getOrDefault("status",""));
                    if (status.equalsIgnoreCase("ACEPTADO") || status.equalsIgnoreCase("INCUMPLIDA")) {
                        String studentId = (String) r.get("studentId");
                        if (studentId != null && !studentId.equals(myId)) out.add(studentId);
                    }
                }
            }
        } catch (Exception e) { log.debug("reservations/for-me fallo: {}", e.toString()); }

        return out;
    }
}

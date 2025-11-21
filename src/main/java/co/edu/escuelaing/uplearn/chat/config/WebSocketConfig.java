package co.edu.escuelaing.uplearn.chat.config;

import co.edu.escuelaing.uplearn.chat.ws.ChatWebSocketGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketGateway chatGateway;
    /**
     * Registers WebSocket handlers for the application.
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatGateway, "/ws/chat")
                .setAllowedOrigins("*"); // CORS is handled above for HTTP; WS here needs explicit origins
    }
}

package co.edu.escuelaing.uplearn.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Value("${redis.host}")
    private String host;
    @Value("${redis.port}")
    private int port;

    /**
     * Configuración de la conexión a Redis.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    /**
     * Configuración del template de Redis para operaciones con cadenas.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    /**
     * Configuración del contenedor de listeners de mensajes de Redis.
     */
    @Bean
    public RedisMessageListenerContainer listenerContainer(LettuceConnectionFactory cf) {
        RedisMessageListenerContainer c = new RedisMessageListenerContainer();
        c.setConnectionFactory(cf);
        return c;
    }
}

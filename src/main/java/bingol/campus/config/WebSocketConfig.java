package bingol.campus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // Grup ve özel mesajlar için broker
        config.setApplicationDestinationPrefixes("/app"); // İstemciden gelen mesajlar için
        config.setUserDestinationPrefix("/user"); // Kullanıcı bazlı mesajlaşma için
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat") // WebSocket bağlantı noktası (Burayı düzelttik)
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

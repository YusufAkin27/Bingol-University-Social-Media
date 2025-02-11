package bingol.campus.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OnlineUserPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public OnlineUserPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 5000)
    public void publishOnlineUsers() {
        messagingTemplate.convertAndSend("/topic/onlineUsers", ChatWebSocketHandler.getOnlineUsers());
    }
}

package bingol.campus.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public ChatWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            onlineUsers.add(username);
            System.out.println("✅ Kullanıcı Bağlandı: " + username);
        } else {
            System.out.println("⚠ Kullanıcı adı alınamadı! SessionAttributes boş.");
        }

        messagingTemplate.convertAndSend("/topic/onlineUsers", onlineUsers);
    }


    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            onlineUsers.remove(username);
            System.out.println("User Disconnected: " + username);
        }

        messagingTemplate.convertAndSend("/topic/onlineUsers", onlineUsers);
    }

    public static Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}

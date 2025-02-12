package bingol.campus.chat.config;

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

    public static boolean isUserOnline(String username) {
        return onlineUsers.contains(username);
    }

    public static Set<String> getOnlineUsers() {
        return onlineUsers;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

            if (headerAccessor.getSessionAttributes() != null) {
                String username = (String) headerAccessor.getSessionAttributes().get("username");

                if (username != null && !username.isEmpty()) {
                    onlineUsers.add(username);
                    System.out.println("✅ User Connected: " + username);
                    messagingTemplate.convertAndSend("/topic/onlineUsers", onlineUsers);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ WebSocket Connect Error: " + e.getMessage());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

            if (headerAccessor.getSessionAttributes() != null) {
                String username = (String) headerAccessor.getSessionAttributes().get("username");

                if (username != null && onlineUsers.remove(username)) {
                    System.out.println("❌ User Disconnected: " + username);
                    messagingTemplate.convertAndSend("/topic/onlineUsers", onlineUsers);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ WebSocket Disconnect Error: " + e.getMessage());
        }
    }
}

package bingol.campus.chat.listener;

import bingol.campus.chat.service.OnlineStatusService;
import bingol.campus.student.exceptions.StudentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineStatusService onlineStatusService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) throws StudentNotFoundException {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long studentId = Long.valueOf(headerAccessor.getSessionAttributes().get("studentId").toString());
        onlineStatusService.updateUserStatus(studentId, true);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) throws StudentNotFoundException {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long studentId = Long.valueOf(headerAccessor.getSessionAttributes().get("studentId").toString());
        onlineStatusService.updateUserStatus(studentId, false);
    }
}
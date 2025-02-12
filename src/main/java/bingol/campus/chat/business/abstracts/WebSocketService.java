package bingol.campus.chat.business.abstracts;

import bingol.campus.chat.config.ChatWebSocketHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, List<String>> groupChatStore = new ConcurrentHashMap<>(); // Grup sohbeti mesajlarını saklamak için
    private final Map<String, List<String>> privateChatStore = new ConcurrentHashMap<>(); // Özel sohbet mesajlarını saklamak için

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 🔹 Grup Sohbetleri Fonksiyonları
    public void sendMessageToGroupChat(Long chatId, String messageContent) {
        groupChatStore.computeIfAbsent(chatId, k -> new java.util.ArrayList<>()).add(messageContent);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, messageContent);
    }

    public List<String> getGroupChatMessages(Long chatId) {
        return groupChatStore.getOrDefault(chatId, java.util.Collections.emptyList());
    }

    public void updateGroupMessage(Long chatId, int messageIndex, String updatedContent) {
        if (groupChatStore.containsKey(chatId)) {
            List<String> messages = groupChatStore.get(chatId);
            if (messageIndex < messages.size()) {
                messages.set(messageIndex, updatedContent);
                messagingTemplate.convertAndSend("/topic/chat/" + chatId, updatedContent);
            }
        }
    }

    public void deleteGroupMessage(Long chatId, int messageIndex) {
        if (groupChatStore.containsKey(chatId)) {
            List<String> messages = groupChatStore.get(chatId);
            if (messageIndex < messages.size()) {
                messages.remove(messageIndex);
                messagingTemplate.convertAndSend("/topic/chat/" + chatId, "deleted");
            }
        }
    }

    // 🔹 Özel Mesaj Fonksiyonları
    public void sendMessageToUser(String username, String messageContent) {
        privateChatStore.computeIfAbsent(username, k -> new java.util.ArrayList<>()).add(messageContent);
        messagingTemplate.convertAndSendToUser(username, "/queue/messages", messageContent);
    }

    public List<String> getPrivateChatMessages(String username) {
        return privateChatStore.getOrDefault(username, java.util.Collections.emptyList());
    }

    public void updatePrivateMessage(String username, int messageIndex, String updatedContent) {
        if (privateChatStore.containsKey(username)) {
            List<String> messages = privateChatStore.get(username);
            if (messageIndex < messages.size()) {
                messages.set(messageIndex, updatedContent);
                messagingTemplate.convertAndSendToUser(username, "/queue/messages", updatedContent);
            }
        }
    }

    public void deletePrivateMessage(String username, int messageIndex) {
        if (privateChatStore.containsKey(username)) {
            List<String> messages = privateChatStore.get(username);
            if (messageIndex < messages.size()) {
                messages.remove(messageIndex);
                messagingTemplate.convertAndSendToUser(username, "/queue/messages", "deleted");
            }
        }
    }

    // 🔹 Grup Sohbetlerim
    public Set<Long> getGroupChats() {
        return groupChatStore.keySet();
    }

    // 🔹 Özel Sohbetlerim
    public Set<String> getPrivateChats() {
        return privateChatStore.keySet();
    }

    // 🔹 Çevrim içi kullanıcıları getir
    public Set<String> getOnlineUsers() {
        return ChatWebSocketHandler.getOnlineUsers();
    }


}

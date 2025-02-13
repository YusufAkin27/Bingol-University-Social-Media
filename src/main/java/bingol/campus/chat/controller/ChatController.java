package bingol.campus.chat.controller;


import bingol.campus.chat.response.CreateChatRequest;
import bingol.campus.chat.response.MessageResponse;
import bingol.campus.chat.response.SendMessageRequest;
import bingol.campus.chat.service.ChatService;
import bingol.campus.chat.service.OnlineStatusService;
import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.exceptions.StudentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final OnlineStatusService onlineStatusService;

    // Yeni bir sohbet oluştur
    @PostMapping
    public ResponseMessage createChat(@AuthenticationPrincipal UserDetails userDetails, @RequestBody CreateChatRequest request) throws StudentNotFoundException, BlockingBetweenStudent {
        return chatService.createChat(userDetails.getUsername(), request);
    }

    // Belirli bir sohbeti getir
    @GetMapping("/{chatId}")
    public DataResponseMessage getChat(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long chatId) throws StudentNotFoundException {
        return chatService.getChatById(userDetails.getUsername(), chatId);
    }

    // Tüm sohbetleri getir
    @GetMapping
    public DataResponseMessage getAllChats(@AuthenticationPrincipal UserDetails userDetails) throws StudentNotFoundException {
        return chatService.getAllChats(userDetails.getUsername());
    }

    // Mesaj gönder
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public DataResponseMessage sendMessage(@AuthenticationPrincipal UserDetails userDetails, SendMessageRequest request) throws StudentNotFoundException {
        return chatService.sendMessage(userDetails.getUsername(), request);
    }

    // Mesaj güncelle
    @MessageMapping("/updateMessage")
    @SendTo("/topic/messages")
    public DataResponseMessage updateMessage(@AuthenticationPrincipal UserDetails userDetails, MessageResponse request) throws StudentNotFoundException {
        return chatService.updateMessage(userDetails.getUsername(), request);
    }

    // Mesaj sil
    @MessageMapping("/deleteMessage")
    @SendTo("/topic/messages")
    public ResponseMessage deleteMessage(@AuthenticationPrincipal UserDetails userDetails, Long chatId, Long messageId) throws StudentNotFoundException {
        return chatService.deleteMessage(userDetails.getUsername(), chatId, messageId);
    }

    // Kullanıcının son görülme zamanını getir
    @GetMapping("/last-seen/{username}")
    public DataResponseMessage getLastSeen(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String username) throws StudentNotFoundException {
        return onlineStatusService.lastSeen(userDetails.getUsername(), username);
    }

    // Kullanıcı durumu getir
    @GetMapping("/user-status/{studentId}")
    public DataResponseMessage getUserStatus(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long studentId) throws StudentNotFoundException {
        return onlineStatusService.getUserStatus(userDetails.getUsername(), studentId);
    }

    // Kullanıcı durumu güncelle
    @PostMapping("/user-status/{studentId}")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long studentId, @RequestParam boolean isOnline) throws StudentNotFoundException {
        onlineStatusService.updateUserStatus(studentId, isOnline);
        return ResponseEntity.noContent().build();
    }
}
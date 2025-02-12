package bingol.campus.chat.controller;

import bingol.campus.chat.business.abstracts.GroupChatService;
import bingol.campus.chat.business.abstracts.PrivateChatService;
import bingol.campus.chat.config.ChatWebSocketHandler;
import bingol.campus.chat.core.request.SendMessageRequest;
import bingol.campus.chat.core.response.MessageResponse;
import bingol.campus.chat.core.response.PrivateChatResponse;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.student.exceptions.StudentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import bingol.campus.chat.business.abstracts.GroupChatService;
import bingol.campus.chat.business.abstracts.PrivateChatService;
import bingol.campus.chat.core.request.*;
import bingol.campus.chat.core.response.GroupChatResponse;
import bingol.campus.chat.core.response.MessageResponse;
import bingol.campus.chat.core.response.PrivateChatResponse;
import bingol.campus.chat.config.ChatWebSocketHandler;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.exceptions.StudentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GroupChatService groupChatService;
    private final PrivateChatService privateChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/online-status/{username}")
    public void checkOnlineStatus(@DestinationVariable String username) {
        boolean isOnline = ChatWebSocketHandler.isUserOnline(username);
        messagingTemplate.convertAndSend("/queue/online-status/" + username, isOnline);
    }
    @MessageMapping("/chat/create")
    @SendTo("/user/queue/chats")
    public PrivateChatResponse createChat(
            @AuthenticationPrincipal(expression = "username") String username,
            @Payload String receiverUsername) throws StudentNotFoundException {

        PrivateChatResponse newChat = privateChatService.createPrivateChat(username, receiverUsername);

        // Kullanıcıya yeni sohbeti bildir
        messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/chats", newChat);

        return newChat;
    }


    @MessageMapping("/private/{chatId}/messages")
    public void getPrivateMessages(@AuthenticationPrincipal UserDetails userDetails, @DestinationVariable Long chatId) {
        try {
            // Kullanıcının bu sohbetin bir üyesi olup olmadığını kontrol et
            if (!privateChatService.isUserInChat(userDetails.getUsername(), chatId)) {
                messagingTemplate.convertAndSendToUser(userDetails.getUsername(), "/queue/errors", "Bu sohbetin mesajlarını görüntüleme yetkiniz yok.");
                return;
            }

            List<MessageResponse> messages = privateChatService.getPrivateMessages(userDetails.getUsername(), chatId);
            messagingTemplate.convertAndSendToUser(userDetails.getUsername(), "/queue/private-messages/" + chatId, messages);
        } catch (StudentNotFoundException e) {
            messagingTemplate.convertAndSendToUser(userDetails.getUsername(), "/queue/errors", "Hata: " + e.getMessage());
        }
    }

    @MessageMapping("/private/list")
    public void getPrivateChats(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<PrivateChatResponse> chats = privateChatService.getPrivateChats(userDetails.getUsername());
            messagingTemplate.convertAndSendToUser(userDetails.getUsername(), "/queue/private-chats", chats);
        } catch (StudentNotFoundException e) {
            messagingTemplate.convertAndSendToUser(userDetails.getUsername(), "/queue/errors", "Hata: " + e.getMessage());
        }
    }

    @MessageMapping("/private/message/send")
    public void sendPrivateMessage(@AuthenticationPrincipal UserDetails userDetails, SendMessageRequest request) throws StudentNotFoundException {
        // Kullanıcı gerçekten bu sohbetin bir parçası mı kontrol et
        if (!privateChatService.isUserInChat(userDetails.getUsername(), request.getChatId())) {
            messagingTemplate.convertAndSendToUser(userDetails.getUsername(), "/queue/errors", "Bu sohbete mesaj gönderme yetkiniz yok.");
            return;
        }

        MessageResponse messageResponse = privateChatService.sendPrivateMessage(userDetails.getUsername(), request);

        // Sohbetteki tüm katılımcılara mesajı gönder
        List<ChatParticipant> participants = privateChatService.getChatParticipants(request.getChatId());
        for (ChatParticipant participant : participants) {
            messagingTemplate.convertAndSendToUser(participant.getStudent().getUserNumber(), "/queue/private-messages/" + request.getChatId(), messageResponse);
        }
    }



    // ✅ Mesaj Güncelleme (Özel & Grup)
    @PutMapping("/message/update")
    public DataResponseMessage<MessageResponse> updateMessage(@AuthenticationPrincipal UserDetails userDetails,
                                                              @RequestBody UpdateMessageRequest request) throws StudentNotFoundException {
        return privateChatService.updateMessage(userDetails.getUsername(), request);
    }

    // ✅ Mesaj Silme (Özel & Grup)
    @DeleteMapping("/message/delete")
    public ResponseMessage deleteMessage(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody DeleteMessageRequest request) throws StudentNotFoundException {
        return privateChatService.deleteMessage(userDetails.getUsername(), request);
    }
    // ✅ Kullanıcının çevrimiçi arkadaşlarını getir
    @GetMapping("/friends/online")
    public DataResponseMessage<List<String>> getOnlineFriends(@AuthenticationPrincipal UserDetails userDetails) {
        return privateChatService.getOnlineFriends(userDetails.getUsername());
    }

    // ✅ Belirli bir özel sohbeti sil
    @DeleteMapping("/private/{chatId}")
    public ResponseMessage deletePrivateChat(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long chatId) throws StudentNotFoundException {
        return privateChatService.deletePrivateChat(userDetails.getUsername(), chatId);
    }

    @GetMapping("/private/{chatId}/search")
    public DataResponseMessage<List<MessageResponse>> searchPrivateChatMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId,
            @RequestParam String keyword) throws StudentNotFoundException {
        return privateChatService.searchMessagesInPrivateChat(userDetails.getUsername(), chatId, keyword);
    }

    // 📅 Belirli bir tarihten sonraki mesajları getir (Özel sohbet)
    @GetMapping("/private/{chatId}/messages-after")
    public DataResponseMessage<List<MessageResponse>> getPrivateMessagesAfter(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId,
            @RequestParam LocalDateTime timestamp) throws StudentNotFoundException {
        return privateChatService.getMessagesAfterInPrivateChat(userDetails.getUsername(), chatId, timestamp);
    }
    // 👤 Belirli bir kullanıcı tarafından gönderilen mesajları getir (Özel sohbet)
    @GetMapping("/private/{chatId}/messages-from/{senderId}")
    public DataResponseMessage<List<MessageResponse>> getPrivateMessagesFromUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId,
            @PathVariable Long senderId) throws StudentNotFoundException {
        return privateChatService.getMessagesFromUserInPrivateChat(userDetails.getUsername(), chatId, senderId);
    }

    // 🔟 Son X mesajı getir (Özel sohbet)
    @GetMapping("/private/{chatId}/last-messages")
    public DataResponseMessage<List<MessageResponse>> getLastPrivateMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId,
            @RequestParam int limit) throws StudentNotFoundException {
        return privateChatService.getLastMessagesInPrivateChat(userDetails.getUsername(), chatId, limit);
    }









    // ✅ Yeni grup sohbeti oluştur
    @PostMapping("/group/create")
    public DataResponseMessage<GroupChatResponse> createGroupChat(@AuthenticationPrincipal UserDetails userDetails,
                                                                  @RequestBody CreateChatRequest request) throws StudentNotFoundException {
        return groupChatService.createGroupChat(userDetails.getUsername(), request);
    }


    // ✅ Kullanıcının grup sohbetlerini getir
    @GetMapping("/group/list")
    public DataResponseMessage<List<GroupChatResponse>> getGroupChats(@AuthenticationPrincipal UserDetails userDetails) throws StudentNotFoundException {
        return groupChatService.getGroupChats(userDetails.getUsername());
    }



    // ✅ Kullanıcının grup mesajlarını getir
    @GetMapping("/group/{chatId}/messages")
    public DataResponseMessage<List<MessageResponse>> getGroupMessages(@AuthenticationPrincipal UserDetails userDetails,
                                                                       @PathVariable Long chatId) throws StudentNotFoundException {
        return groupChatService.getGroupMessages(userDetails.getUsername(), chatId);
    }


    // ✅ Grup sohbetine mesaj gönder
    @PostMapping("/group/message/send")
    public DataResponseMessage<MessageResponse> sendGroupMessage(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @RequestBody SendMessageRequest request) throws StudentNotFoundException {
        return groupChatService.sendGroupMessage(userDetails.getUsername(), request);
    }


    // ✅ Grup sohbetine katılımcı ekle
    @PostMapping("/group/participant/add")
    public ResponseMessage addGroupParticipant(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestBody AddParticipantRequest request) throws StudentNotFoundException {
        return groupChatService.addGroupParticipant(userDetails.getUsername(), request);
    }

    // ✅ Grup sohbetinden katılımcıyı çıkar
    @DeleteMapping("/group/{chatId}/participant/{participantId}")
    public ResponseMessage removeGroupParticipant(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable Long chatId, @PathVariable Long participantId) throws StudentNotFoundException {
        return groupChatService.removeGroupParticipant(userDetails.getUsername(), chatId, participantId);
    }

    // ✅ Belirli bir grup sohbetine katılan kullanıcıları getir
    @GetMapping("/group/{chatId}/participants")
    public DataResponseMessage<List<Long>> getGroupParticipants(@AuthenticationPrincipal UserDetails userDetails,
                                                                @PathVariable Long chatId) throws StudentNotFoundException {
        return groupChatService.getGroupParticipants(userDetails.getUsername(), chatId);
    }


    // ✅ Belirli bir grup sohbetini sil
    @DeleteMapping("/group/{chatId}")
    public ResponseMessage deleteGroupChat(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable Long chatId) throws StudentNotFoundException {
        return groupChatService.deleteGroupChat(userDetails.getUsername(), chatId);
    }
    // ✅ Gruptan ayrıl (kendi isteğiyle)
    @PostMapping("/group/{chatId}/leave")
    public ResponseMessage leaveGroup(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long chatId) throws StudentNotFoundException {
        return groupChatService.leaveGroup(userDetails.getUsername(), chatId);
    }

    // ✅ Kullanıcıyı gruptan çıkar (Adminler yapabilir)
    @DeleteMapping("/group/{chatId}/participant/{participantId}/remove")
    public ResponseMessage removeUserFromGroup(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long chatId, @PathVariable Long participantId) throws StudentNotFoundException {
        return groupChatService.removeUserFromGroup(userDetails.getUsername(), chatId, participantId);
    }

    // ✅ Grup profilini güncelle (Ad, açıklama, profil resmi vs.)
    @PutMapping("/group/{chatId}/update")
    public DataResponseMessage<GroupChatResponse> updateGroupProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @PathVariable Long chatId,
                                                                     @RequestBody UpdateGroupRequest request) throws StudentNotFoundException {
        return groupChatService.updateGroupProfile(userDetails.getUsername(), chatId, request);
    }

    // ✅ Gruba yeni bir admin ata (Sadece mevcut adminler yapabilir)
    @PostMapping("/group/{chatId}/admin/add/{userId}")
    public ResponseMessage assignGroupAdmin(@AuthenticationPrincipal UserDetails userDetails,
                                            @PathVariable Long chatId, @PathVariable Long userId) throws StudentNotFoundException {
        return groupChatService.assignGroupAdmin(userDetails.getUsername(), chatId, userId);
    }

    // ✅ Grup adminliğini kaldır (Sadece mevcut adminler yapabilir)
    @DeleteMapping("/group/{chatId}/admin/remove/{userId}")
    public ResponseMessage removeGroupAdmin(@AuthenticationPrincipal UserDetails userDetails,
                                            @PathVariable Long chatId, @PathVariable Long userId) throws StudentNotFoundException {
        return groupChatService.removeGroupAdmin(userDetails.getUsername(), chatId, userId);
    }

    // ✅ Grup adminlerini listele
    @GetMapping("/group/{chatId}/admins")
    public DataResponseMessage<List<Long>> getGroupAdmins(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable Long chatId) throws StudentNotFoundException {
        return groupChatService.getGroupAdmins(userDetails.getUsername(), chatId);
    }


    // 🔍 Grup sohbette mesaj arama
    @GetMapping("/group/{chatId}/search")
    public DataResponseMessage<List<MessageResponse>> searchGroupChatMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId,
            @RequestParam String keyword) throws StudentNotFoundException {
        return groupChatService.searchMessagesInGroupChat(userDetails.getUsername(), chatId, keyword);
    }



    // 📅 Belirli bir tarihten sonraki mesajları getir (Grup sohbet)
    @GetMapping("/group/{chatId}/messages-after")
    public DataResponseMessage<List<MessageResponse>> getGroupMessagesAfter(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId,
            @RequestParam LocalDateTime timestamp) throws StudentNotFoundException {
        return groupChatService.getMessagesAfterInGroupChat(userDetails.getUsername(), chatId, timestamp);
    }

    // 👤 Belirli bir kullanıcı tarafından gönderilen mesajları getir (Grup sohbet)
    @GetMapping("/group/{chatId}/messages-from/{senderId}")
    public DataResponseMessage<List<MessageResponse>> getGroupMessagesFromUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId,
            @PathVariable Long senderId) throws StudentNotFoundException {
        return groupChatService.getMessagesFromUserInGroupChat(userDetails.getUsername(), chatId, senderId);
    }


    // 🔟 Son X mesajı getir (Grup sohbet)
    @GetMapping("/group/{chatId}/last-messages")
    public DataResponseMessage<List<MessageResponse>> getLastGroupMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId,
            @RequestParam int limit) throws StudentNotFoundException {
        return groupChatService.getLastMessagesInGroupChat(userDetails.getUsername(), chatId, limit);
    }

}

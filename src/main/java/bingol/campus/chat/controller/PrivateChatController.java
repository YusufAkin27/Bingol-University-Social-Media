package bingol.campus.chat.controller;

import bingol.campus.chat.business.abstracts.PrivateChatService;
import bingol.campus.chat.dto.MessageDTO;
import bingol.campus.chat.dto.PrivateChatDTO;
import bingol.campus.chat.exceptions.PrivateChatNotFoundException;
import bingol.campus.chat.request.EditMessageRequest;
import bingol.campus.chat.request.SendMessageRequest;
import bingol.campus.chat.request.DeleteMessageRequest;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.exceptions.StudentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/privateChat")
@RequiredArgsConstructor
public class PrivateChatController {

    private final PrivateChatService privateChatService;

    // Yeni özel sohbet oluştur
    @PostMapping("/createChat/{username}")
    public ResponseMessage createChat(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String username)
            throws StudentNotFoundException {
        return privateChatService.createChat(userDetails.getUsername(), username);
    }

    // Mesaj gönder
    @PostMapping("/send")
    public DataResponseMessage<MessageDTO> sendPrivateMessage(@AuthenticationPrincipal UserDetails userDetails,
                                                              @RequestBody SendMessageRequest sendMessageRequest)
            throws StudentNotFoundException, PrivateChatNotFoundException {
        return privateChatService.sendPrivateMessage(userDetails.getUsername(), sendMessageRequest);
    }

    // Kullanıcının sohbetlerini getir
    @GetMapping("/getChats")
    public DataResponseMessage<List<PrivateChatDTO>> getChats(@AuthenticationPrincipal UserDetails userDetails)
            throws StudentNotFoundException {
        return privateChatService.getChats(userDetails.getUsername());
    }

    // Belirli bir sohbetin mesajlarını getir
    @GetMapping("/getMessages/{chatId}")
    public DataResponseMessage<List<MessageDTO>> getMessages(@AuthenticationPrincipal UserDetails userDetails,
                                                             @PathVariable UUID chatId)
            throws StudentNotFoundException, PrivateChatNotFoundException {
        return privateChatService.getMessages(userDetails.getUsername(), chatId);
    }

    // Mesajı düzenle
    @PutMapping("/editMessage")
    public ResponseMessage editMessage(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody EditMessageRequest editMessageRequest) {
        return privateChatService.editMessage(userDetails.getUsername(), editMessageRequest);
    }

    // Mesajı sil (kendinden veya herkesten)
    @DeleteMapping("/deleteMessage")
    public ResponseMessage deleteMessage(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody DeleteMessageRequest deleteMessageRequest) {
        return privateChatService.deleteMessage(userDetails.getUsername(), deleteMessageRequest);
    }

    // Sohbeti sil (kullanıcıdan kaldırma)
    @DeleteMapping("/deleteChat/{chatId}")
    public ResponseMessage deleteChat(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable UUID chatId)
            throws StudentNotFoundException, PrivateChatNotFoundException {
        return privateChatService.deleteChat(userDetails.getUsername(), chatId);
    }

    // Mesajın kimler tarafından okunduğunu getir
    @GetMapping("/readReceipts/{messageId}")
    public DataResponseMessage<List<String>> getReadReceipts(@AuthenticationPrincipal UserDetails userDetails,
                                                             @PathVariable UUID messageId) {
        return privateChatService.getReadReceipts(userDetails.getUsername(), messageId);
    }

    @GetMapping("/userStatus/{username}")
    public DataResponseMessage<Map<String, Object>> getUserStatus(@PathVariable String username) throws StudentNotFoundException {
      return privateChatService.getUserStatus(username);
    }


}

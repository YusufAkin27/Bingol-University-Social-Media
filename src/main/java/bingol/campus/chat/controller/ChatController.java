package bingol.campus.chat.controller;

import bingol.campus.chat.business.concretes.MessageManager;
import bingol.campus.chat.core.response.MessageDTO;
import bingol.campus.chat.core.response.PrivateChatDTO;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.entity.PrivateChat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageManager messageManager;


    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestParam Long receiverId,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageDTO message = messageManager.sendMessage(receiverId, content, userDetails);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/private/{otherUserId}")
    public ResponseEntity<PrivateChatDTO> getPrivateChat(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PrivateChatDTO chat = messageManager.getPrivateChat(otherUserId, userDetails);
        return ResponseEntity.ok(chat);
    }
}

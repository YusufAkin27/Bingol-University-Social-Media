package bingol.campus.chat.controller;

import bingol.campus.chat.business.abstracts.PrivateChatService;
import bingol.campus.chat.exceptions.PrivateChatNotFoundException;
import bingol.campus.chat.request.SendMessageRequest;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.exceptions.StudentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/privateChat")
@RequiredArgsConstructor
public class PrivateChatController {

    private final PrivateChatService privateChatService;


    @PostMapping("/createChat/{username}")
    public ResponseMessage createChat(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String username) throws StudentNotFoundException {
        return privateChatService.createChat(userDetails.getUsername(), username);
    }

    @PostMapping("/send")
    public DataResponseMessage sendPrivateMessage(@AuthenticationPrincipal UserDetails userDetails, @RequestBody SendMessageRequest sendMessageRequest) throws StudentNotFoundException, PrivateChatNotFoundException {
        return privateChatService.sendPrivateMessage(userDetails.getUsername(), sendMessageRequest);
    }

    @GetMapping("/getChats")
    public DataResponseMessage getChats(@AuthenticationPrincipal UserDetails userDetails) throws StudentNotFoundException {
        return privateChatService.getChats(userDetails.getUsername());
    }

    @GetMapping("/getMessages/{chatId}")
    public DataResponseMessage getMessages(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long chatId) throws StudentNotFoundException, PrivateChatNotFoundException {
        return privateChatService.getMessages(userDetails.getUsername(), chatId);
    }


}

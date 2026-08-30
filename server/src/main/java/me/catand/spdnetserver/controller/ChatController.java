package me.catand.spdnetserver.controller;

import me.catand.spdnetserver.ChatService;
import me.catand.spdnetserver.SocketService;
import me.catand.spdnetserver.controller.dto.ApiResponse;
import me.catand.spdnetserver.data.events.SChatMessage;
import me.catand.spdnetserver.security.RequestAuth;
import me.catand.spdnetserver.service.BannedWordsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SocketService socketService;

    @Autowired
    private BannedWordsService bannedWordsService;

    // SPDNet: 用于解析登录令牌对应的用户名，防止任意伪造他人身份发消息
    @Autowired
    private RequestAuth requestAuth;

    @GetMapping("/messages")
    public ApiResponse<List<SChatMessage>> getMessages(@RequestParam(defaultValue = "50") int count) {
        return ApiResponse.success("获取成功", chatService.getMessages(count));
    }

    @PostMapping("/send")
    public ApiResponse<Void> sendMessage(@RequestBody Map<String, String> request,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse response) {
        String name = request.get("name");
        String message = request.get("message");

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }

        if (message == null || message.trim().isEmpty()) {
            return ApiResponse.error("消息内容不能为空");
        }

        // SPDNet: 防伪造——必须登录，且只能以令牌对应的本人身份发送，不能冒充其他玩家
        String currentUser = requestAuth.resolveCurrentUsername(httpRequest);
        if (currentUser == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return ApiResponse.error("未登录");
        }
        if (!currentUser.equals(name)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return ApiResponse.error("只能以本人身份发送消息");
        }

        if (message.length() > 500) {
            return ApiResponse.error("消息内容不能超过500字符");
        }

        // SPDNet: 检查消息是否包含屏蔽词
        if (bannedWordsService.containsBannedWord(message)) {
            String bannedWord = bannedWordsService.getFirstBannedWord(message);
            return ApiResponse.error("消息包含敏感词: " + bannedWord);
        }

        // SPDNet: 系统广播消息使用服务端时间
        chatService.addMessage(name, message, "");
        socketService.broadcastChatMessage(name, message);

        return ApiResponse.success("发送成功");
    }
}

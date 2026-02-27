package me.catand.spdnetserver.controller;

import me.catand.spdnetserver.ChatService;
import me.catand.spdnetserver.SocketService;
import me.catand.spdnetserver.controller.dto.ApiResponse;
import me.catand.spdnetserver.data.events.SChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SocketService socketService;

    @GetMapping("/messages")
    public ApiResponse<List<SChatMessage>> getMessages(@RequestParam(defaultValue = "50") int count) {
        return ApiResponse.success("获取成功", chatService.getMessages(count));
    }

    @PostMapping("/send")
    public ApiResponse<Void> sendMessage(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String message = request.get("message");

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }

        if (message == null || message.trim().isEmpty()) {
            return ApiResponse.error("消息内容不能为空");
        }

        if (message.length() > 500) {
            return ApiResponse.error("消息内容不能超过500字符");
        }

        chatService.addMessage(name, message);
        socketService.broadcastChatMessage(name, message);

        return ApiResponse.success("发送成功");
    }
}

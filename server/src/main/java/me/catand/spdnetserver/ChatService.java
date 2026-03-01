package me.catand.spdnetserver;

import me.catand.spdnetserver.data.events.SChatMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class ChatService {
    private static final int MAX_MESSAGES = 100;
    private final ConcurrentLinkedDeque<SChatMessage> messages = new ConcurrentLinkedDeque<>();

    public void addMessage(String name, String message, String time) {
        // SPDNet: 如果客户端没有提供时间，则使用服务端时间
        if (time == null || time.isEmpty()) {
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        SChatMessage chatMessage = new SChatMessage(name, message, time);
        messages.addFirst(chatMessage);
        while (messages.size() > MAX_MESSAGES) {
            messages.removeLast();
        }
    }

    public List<SChatMessage> getMessages(int count) {
        List<SChatMessage> result = new ArrayList<>();
        int i = 0;
        for (SChatMessage msg : messages) {
            if (i >= count) break;
            result.add(msg);
            i++;
        }
        return result;
    }
}

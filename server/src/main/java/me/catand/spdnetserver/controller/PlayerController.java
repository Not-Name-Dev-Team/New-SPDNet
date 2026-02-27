package me.catand.spdnetserver.controller;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnetserver.MailSender;
import me.catand.spdnetserver.SocketService;
import me.catand.spdnetserver.controller.dto.ApiResponse;
import me.catand.spdnetserver.controller.dto.PlayerInfo;
import me.catand.spdnetserver.controller.dto.RegisterRequest;
import me.catand.spdnetserver.controller.dto.SendKeyRequest;
import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.repositories.GameRecordRepository;
import me.catand.spdnetserver.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private SocketService socketService;

    @Value("${spd.mail.sender:}")
    private String mailSenderAddress;

    @Value("${spd.mail.username:}")
    private String mailSenderUsername;

    @Value("${spd.mail.password:}")
    private String mailSenderPassword;

    @Value("${spd.mail.host:smtp.126.com}")
    private String mailSenderHost;

    private MailSender mailSender;

    @PostConstruct
    public void init() {
        if (mailSenderAddress != null && !mailSenderAddress.isEmpty()) {
            mailSender = new MailSender(mailSenderAddress, mailSenderUsername, mailSenderPassword, mailSenderHost);
        }
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        String name = request.getName();
        String email = request.getEmail();

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }

        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("邮箱不能为空");
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ApiResponse.error("邮箱格式不正确");
        }

        if (playerRepository.existsByName(name)) {
            return ApiResponse.error("用户名已存在");
        }

        if (playerRepository.existsByEmail(email)) {
            return ApiResponse.error("该邮箱已注册");
        }

        Player player = new Player();
        player.setName(name);
        player.setEmail(email);
        player.setKey(generateKey(email));
        player.setPower("玩家");

        playerRepository.save(player);

        if (mailSender != null) {
            try {
                String mailContent = String.format(
                    "<h2>欢迎注册破碎地牢联机服务</h2>" +
                    "<p>您的用户名: <strong>%s</strong></p>" +
                    "<p>您的连接Key: <strong>%s</strong></p>" +
                    "<p>请妥善保管您的Key，这是您登录游戏的唯一凭证。</p>" +
                    "<p>如需查询Key，请访问网页端或使用QQ机器人。</p>",
                    name, player.getKey()
                );
                mailSender.sendMail(email, "破碎地牢联机服务 - 注册成功", mailContent);
            } catch (Exception e) {
                log.error("发送邮件失败: {}", e.getMessage());
            }
        }

        log.info("新用户注册: {}", name);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("key", player.getKey());

        return ApiResponse.success("注册成功，Key已发送到您的邮箱", data);
    }

    @PostMapping("/send-key")
    public ApiResponse<Void> sendKey(@RequestBody SendKeyRequest request) {
        String email = request.getEmail();

        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("邮箱不能为空");
        }

        Player player = playerRepository.findByEmail(email);
        if (player == null) {
            return ApiResponse.error("该邮箱未注册");
        }

        if (mailSender == null) {
            return ApiResponse.error("邮件服务未配置");
        }

        try {
            String mailContent = String.format(
                "<h2>破碎地牢联机服务 - Key查询</h2>" +
                "<p>您的用户名: <strong>%s</strong></p>" +
                "<p>您的连接Key: <strong>%s</strong></p>" +
                "<p>请妥善保管您的Key，这是您登录游戏的唯一凭证。</p>",
                player.getName(), player.getKey()
            );
            mailSender.sendMail(email, "破碎地牢联机服务 - Key查询", mailContent);
            return ApiResponse.success("Key已发送到您的邮箱");
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            return ApiResponse.error("发送邮件失败: " + e.getMessage());
        }
    }

    @GetMapping("/online")
    public ApiResponse<List<PlayerInfo>> getOnlinePlayers() {
        Map<?, Player> playerMap = socketService.getPlayerMap();
        List<PlayerInfo> onlinePlayers = playerMap.values().stream()
            .map(p -> new PlayerInfo(p.getName(), p.getPower(), true))
            .collect(Collectors.toList());
        return ApiResponse.success("获取成功", onlinePlayers);
    }

    @GetMapping("/players")
    public ApiResponse<List<PlayerInfo>> getAllPlayers() {
        List<Player> players = playerRepository.findAll();
        Map<?, Player> onlineMap = socketService.getPlayerMap();

        List<PlayerInfo> playerInfos = players.stream()
            .map(p -> new PlayerInfo(
                p.getName(),
                p.getPower(),
                onlineMap.values().stream().anyMatch(op -> op.getName().equals(p.getName()))
            ))
            .collect(Collectors.toList());

        return ApiResponse.success("获取成功", playerInfos);
    }

    @GetMapping("/leaderboard")
    public ApiResponse<Map<String, Object>> getLeaderboard(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "false") boolean winOnly
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());

        Page<GameRecord> records;
        if (winOnly) {
            records = gameRecordRepository.findByWinTrue(pageable);
        } else {
            records = gameRecordRepository.findAll(pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records.getContent());
        data.put("totalElements", records.getTotalElements());
        data.put("totalPages", records.getTotalPages());
        data.put("currentPage", page);

        return ApiResponse.success("获取成功", data);
    }

    @GetMapping("/player/{name}/records")
    public ApiResponse<List<GameRecord>> getPlayerRecords(@PathVariable String name) {
        Player player = playerRepository.findByName(name);
        if (player == null) {
            return ApiResponse.error("玩家不存在");
        }

        List<GameRecord> records = gameRecordRepository.findByPlayerOrderByScoreDesc(player);
        return ApiResponse.success("获取成功", records);
    }

    @GetMapping("/player/{name}")
    public ApiResponse<Map<String, Object>> getPlayerInfo(@PathVariable String name) {
        Player player = playerRepository.findByName(name);
        if (player == null) {
            return ApiResponse.error("玩家不存在");
        }

        Map<?, Player> onlineMap = socketService.getPlayerMap();
        boolean isOnline = onlineMap.values().stream()
            .anyMatch(p -> p.getName().equals(name));

        Map<String, Object> data = new HashMap<>();
        data.put("name", player.getName());
        data.put("power", player.getPower());
        data.put("online", isOnline);
        data.put("achievementCount", player.getAchievements() != null ? player.getAchievements().size() : 0);

        long totalGames = gameRecordRepository.countByPlayer(player);
        long wins = gameRecordRepository.countByPlayerAndWinTrue(player);
        data.put("totalGames", totalGames);
        data.put("wins", wins);

        return ApiResponse.success("获取成功", data);
    }

    @GetMapping("/server/info")
    public ApiResponse<Map<String, Object>> getServerInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("version", "3.2.5");
        data.put("netVersion", "0.0.1");
        data.put("onlineCount", socketService.getPlayerMap().size());
        data.put("totalPlayers", playerRepository.count());
        return ApiResponse.success("获取成功", data);
    }

    private String generateKey(String email) {
        String salt = "SPDNet-Key-Salt:" + email + ":" + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(salt.getBytes()).substring(0, 16);
    }
}

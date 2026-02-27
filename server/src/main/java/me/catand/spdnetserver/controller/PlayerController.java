package me.catand.spdnetserver.controller;

import me.catand.spdnetserver.SocketService;
import me.catand.spdnetserver.controller.dto.*;
import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.entitys.UserRole;
import me.catand.spdnetserver.repositories.GameRecordRepository;
import me.catand.spdnetserver.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        String name = request.getName();
        String email = request.getEmail();
        String password = request.getPassword();

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }

        if (name.length() < 2 || name.length() > 16) {
            return ApiResponse.error("用户名长度需在2-16个字符之间");
        }

        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("邮箱不能为空");
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ApiResponse.error("邮箱格式不正确");
        }

        if (password == null || password.trim().isEmpty()) {
            return ApiResponse.error("密码不能为空");
        }

        if (password.length() < 6 || password.length() > 32) {
            return ApiResponse.error("密码长度需在6-32个字符之间");
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
        player.setPassword(passwordEncoder.encode(password));
        player.setRole(UserRole.USER);

        playerRepository.save(player);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        return ApiResponse.success("注册成功", data);
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String name = request.getName();
        String password = request.getPassword();

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }

        if (password == null || password.trim().isEmpty()) {
            return ApiResponse.error("密码不能为空");
        }

        Player player = playerRepository.findByName(name);
        if (player == null) {
            return ApiResponse.error("用户名或密码错误");
        }

        if (!passwordEncoder.matches(password, player.getPassword())) {
            return ApiResponse.error("用户名或密码错误");
        }

        if (player.getRole() == UserRole.BANNED) {
            return ApiResponse.error("账号已被封禁");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", player.getName());
        data.put("role", player.getRole().getDisplayName());

        return ApiResponse.success("登录成功", data);
    }

    @GetMapping("/online")
    public ApiResponse<List<PlayerInfo>> getOnlinePlayers() {
        Map<?, Player> playerMap = socketService.getPlayerMap();
        List<PlayerInfo> onlinePlayers = playerMap.values().stream()
            .map(p -> new PlayerInfo(p.getName(), p.getRole().getDisplayName(), true))
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
                p.getRole().getDisplayName(),
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
        data.put("role", player.getRole().getDisplayName());
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
}

package me.catand.spdnetserver.controller;

import me.catand.spdnetserver.SocketService;
import me.catand.spdnetserver.controller.dto.*;
import me.catand.spdnetserver.entitys.*;
import me.catand.spdnetserver.repositories.*;
import me.catand.spdnetserver.service.MailService;
import me.catand.spdnetserver.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private PlayerBestiaryRepository bestiaryRepository;

    @Autowired
    private PlayerCatalogRepository catalogRepository;

    @Autowired
    private PlayerDocumentRepository documentRepository;

    @Autowired
    private SocketService socketService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private MailService mailService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/send-code")
    public ApiResponse<Map<String, Object>> sendVerificationCode(@RequestBody SendCodeRequest request) {
        String email = request.getEmail();

        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("邮箱不能为空");
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ApiResponse.error("邮箱格式不正确");
        }

        if (playerRepository.existsByEmail(email)) {
            return ApiResponse.error("该邮箱已注册");
        }

        if (!mailService.isEnabled()) {
            return ApiResponse.error("邮件服务未启用，请联系管理员");
        }

        // 获取或生成验证码（如果未过期则返回相同的验证码）
        String code = verificationCodeService.getOrGenerateCode(email);
        boolean sent = mailService.sendVerificationCode(email, code);

        if (!sent) {
            return ApiResponse.error("验证码发送失败，请稍后重试");
        }

        // 存储验证码（如果是已存在的验证码，storeCode会更新过期时间）
        verificationCodeService.storeCode(email, code);

        Map<String, Object> data = new HashMap<>();
        data.put("expireMinutes", 5);

        return ApiResponse.success("验证码已发送", data);
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        String name = request.getName();
        String email = request.getEmail();
        String password = request.getPassword();
        String verificationCode = request.getVerificationCode();

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

        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return ApiResponse.error("验证码不能为空");
        }

        if (playerRepository.existsByName(name)) {
            return ApiResponse.error("用户名已存在");
        }

        if (playerRepository.existsByEmail(email)) {
            return ApiResponse.error("该邮箱已注册");
        }

        if (!verificationCodeService.verifyCode(email, verificationCode)) {
            return ApiResponse.error("验证码错误或已过期");
        }

        Player player = new Player();
        player.setName(name);
        player.setEmail(email);
        player.setPassword(passwordEncoder.encode(password));
        player.setRole(UserRole.USER);
        player.setCreatedAt(LocalDateTime.now());

        playerRepository.save(player);
        verificationCodeService.removeCode(email);

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
        @RequestParam(required = false) String playerName,
        @RequestParam(required = false) Integer challengeCount,
        @RequestParam(required = false) Boolean winOnly,
        @RequestParam(required = false) String gameMode,
        @RequestParam(defaultValue = "score") String sortBy
    ) {
        // 构建排序
        Sort sort;
        switch (sortBy) {
            case "duration":
                sort = Sort.by("duration").ascending();
                break;
            case "id":
                sort = Sort.by("id").descending();
                break;
            case "score":
            default:
                sort = Sort.by("score").descending();
                break;
        }
        Pageable pageable = PageRequest.of(page, size, sort);

        // 根据条件查询
        Page<GameRecord> records;
        if (playerName != null && !playerName.trim().isEmpty()) {
            // 按玩家名查询
            Player player = playerRepository.findByName(playerName.trim());
            if (player == null) {
                records = Page.empty(pageable);
            } else {
                records = gameRecordRepository.findByPlayer(player, pageable);
            }
        } else if (challengeCount != null) {
            // 按挑战数查询
            records = gameRecordRepository.findByChallengeAmount(challengeCount, pageable);
        } else if (winOnly != null && winOnly) {
            // 只显示胜利
            if (gameMode != null && !gameMode.isEmpty()) {
                records = gameRecordRepository.findByWinTrueAndGameMode(gameMode, pageable);
            } else {
                records = gameRecordRepository.findByWinTrue(pageable);
            }
        } else if (gameMode != null && !gameMode.isEmpty()) {
            // 按游戏模式查询
            records = gameRecordRepository.findByGameMode(gameMode, pageable);
        } else {
            records = gameRecordRepository.findAll(pageable);
        }

        for (GameRecord record : records.getContent()) {
            if (record.getPlayer() != null) {
                record.setPlayerName(record.getPlayer().getName());
            }
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

        // 成就数量
        data.put("achievementCount", player.getAchievements() != null ? player.getAchievements().size() : 0);

        // 游戏统计
        long totalGames = gameRecordRepository.countByPlayer(player);
        long wins = gameRecordRepository.countByPlayerAndWinTrue(player);
        data.put("totalGames", totalGames);
        data.put("wins", wins);

        // 计算总分数（所有游戏记录分数之和）
        List<GameRecord> allRecords = gameRecordRepository.findByPlayerOrderByScoreDesc(player);
        long totalScore = allRecords.stream().mapToLong(GameRecord::getScore).sum();
        data.put("totalScore", totalScore);

        // 最高记录
        int maxScore = allRecords.isEmpty() ? 0 : allRecords.get(0).getScore();
        int maxDepth = allRecords.stream().mapToInt(GameRecord::getMaxDepth).max().orElse(0);
        int maxLevel = allRecords.stream().mapToInt(r -> r.getLevel() > 0 ? r.getLevel() : 1).max().orElse(1);
        data.put("maxScore", maxScore);
        data.put("maxDepth", maxDepth);
        data.put("maxLevel", maxLevel);

        // 注册时间和最后登录信息（公开信息）
        data.put("createdAt", player.getCreatedAt());
        data.put("lastLoginAt", player.getLastLoginAt());

        // 图鉴、日志、文档信息
        List<PlayerBestiary> bestiaryList = bestiaryRepository.findByPlayerId(player.getId());
        List<PlayerCatalog> catalogList = catalogRepository.findByPlayerId(player.getId());
        List<PlayerDocument> documentList = documentRepository.findByPlayerId(player.getId());

        // 图鉴统计
        long bestiarySeen = bestiaryList.stream().filter(PlayerBestiary::isSeen).count();
        data.put("bestiaryTotal", bestiaryList.size());
        data.put("bestiarySeen", bestiarySeen);

        // 物品图鉴统计
        long catalogSeen = catalogList.stream().filter(PlayerCatalog::isSeen).count();
        data.put("catalogTotal", catalogList.size());
        data.put("catalogSeen", catalogSeen);

        // 文档统计
        long documentFound = documentList.stream().filter(PlayerDocument::isFound).count();
        data.put("documentTotal", documentList.size());
        data.put("documentFound", documentFound);

        // 详细列表（可选，如果需要展示具体项目）
        data.put("bestiaryList", bestiaryList.stream()
            .filter(PlayerBestiary::isSeen)
            .map(b -> Map.of(
                "type", b.getBestiaryType(),
                "entity", b.getEntityClass(),
                "encountered", b.getEncountered()
            ))
            .collect(Collectors.toList()));

        data.put("catalogList", catalogList.stream()
            .filter(PlayerCatalog::isSeen)
            .map(c -> Map.of(
                "type", c.getCatalogType(),
                "item", c.getItemClass(),
                "useCount", c.getUseCount()
            ))
            .collect(Collectors.toList()));

        data.put("documentList", documentList.stream()
            .filter(PlayerDocument::isFound)
            .map(d -> Map.of(
                "type", d.getDocumentType(),
                "page", d.getPageName()
            ))
            .collect(Collectors.toList()));

        return ApiResponse.success("获取成功", data);
    }

    @GetMapping("/player/{name}/private")
    public ApiResponse<Map<String, Object>> getPlayerPrivateInfo(@PathVariable String name) {
        // 这里可以添加权限检查，确保只能查看自己的私密信息
        Player player = playerRepository.findByName(name);
        if (player == null) {
            return ApiResponse.error("玩家不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", player.getName());
        data.put("email", player.getEmail());
        data.put("createdAt", player.getCreatedAt());
        data.put("lastLoginAt", player.getLastLoginAt());
        data.put("lastLoginIp", player.getLastLoginIp());

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

    @PostMapping("/change-name")
    public ApiResponse<Map<String, Object>> changeName(@RequestBody ChangeNameRequest request) {
        String currentName = request.getCurrentName();
        String password = request.getPassword();
        String newName = request.getNewName();

        if (currentName == null || currentName.trim().isEmpty()) {
            return ApiResponse.error("当前用户名不能为空");
        }

        if (password == null || password.trim().isEmpty()) {
            return ApiResponse.error("密码不能为空");
        }

        if (newName == null || newName.trim().isEmpty()) {
            return ApiResponse.error("新用户名不能为空");
        }

        if (newName.length() < 2 || newName.length() > 16) {
            return ApiResponse.error("新用户名长度需在2-16个字符之间");
        }

        Player player = playerRepository.findByName(currentName);
        if (player == null) {
            return ApiResponse.error("用户不存在");
        }

        if (!passwordEncoder.matches(password, player.getPassword())) {
            return ApiResponse.error("密码错误");
        }

        if (playerRepository.existsByName(newName)) {
            return ApiResponse.error("该用户名已被使用");
        }

        player.setName(newName);
        playerRepository.save(player);

        Map<String, Object> data = new HashMap<>();
        data.put("name", newName);
        data.put("role", player.getRole().getDisplayName());

        return ApiResponse.success("修改成功", data);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        String name = request.getName();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return ApiResponse.error("原密码不能为空");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ApiResponse.error("新密码不能为空");
        }

        if (newPassword.length() < 6 || newPassword.length() > 32) {
            return ApiResponse.error("新密码长度需在6-32个字符之间");
        }

        Player player = playerRepository.findByName(name);
        if (player == null) {
            return ApiResponse.error("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, player.getPassword())) {
            return ApiResponse.error("原密码错误");
        }

        player.setPassword(passwordEncoder.encode(newPassword));
        playerRepository.save(player);

        return ApiResponse.success("密码修改成功");
    }
}

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        // 根据条件查询 - 使用组合筛选
        Page<GameRecord> records;
        if ((playerName != null && !playerName.trim().isEmpty()) ||
            challengeCount != null ||
            (winOnly != null && winOnly) ||
            (gameMode != null && !gameMode.isEmpty())) {
            // 使用组合筛选查询
            String username = (playerName != null && !playerName.trim().isEmpty()) ? playerName.trim() : null;
            Boolean win = (winOnly != null && winOnly) ? true : null;
            String mode = (gameMode != null && !gameMode.isEmpty()) ? gameMode : null;
            Integer challenge = challengeCount;
            records = gameRecordRepository.findWithFilters(username, win, mode, challenge, pageable);
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

        // 成就数量和列表
        Set<String> achievements = player.getAchievements();
        data.put("achievementCount", achievements != null ? achievements.size() : 0);
        data.put("achievementTotal", 94); // 总成就数量（来自Badges.java）
        data.put("achievements", achievements != null ? new ArrayList<>(achievements) : new ArrayList<>());

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
        // 历史最高通关挑战数量（只统计获胜的记录）
        int maxChallengeAmount = allRecords.stream()
            .filter(GameRecord::isWin)
            .mapToInt(GameRecord::getChallengeAmount)
            .max()
            .orElse(0);
        data.put("maxScore", maxScore);
        data.put("maxDepth", maxDepth);
        data.put("maxLevel", maxLevel);
        data.put("maxChallengeAmount", maxChallengeAmount);

        // 最近游戏记录（取前5条）
        List<Map<String, Object>> recentGames = allRecords.stream()
            .limit(5)
            .map(r -> {
                Map<String, Object> game = new HashMap<>();
                game.put("score", r.getScore());
                game.put("floor", r.getMaxDepth());
                game.put("result", r.isWin() ? "胜利" : "失败");
                game.put("endTime", r.getDate());
                return game;
            })
            .collect(Collectors.toList());
        data.put("recentGames", recentGames);

        // 注册时间和最后登录信息（公开信息）
        data.put("createdAt", player.getCreatedAt());
        data.put("lastLoginAt", player.getLastLoginAt());

        // 图鉴、日志、文档信息
        List<PlayerBestiary> bestiaryList = bestiaryRepository.findByPlayerId(player.getId());
        List<PlayerCatalog> catalogList = catalogRepository.findByPlayerId(player.getId());
        List<PlayerDocument> documentList = documentRepository.findByPlayerId(player.getId());

        // ========== 成就统计 ==========
        data.put("achievementTotal", 94);
        data.put("achievementCount", achievements != null ? achievements.size() : 0);

        // ========== 地牢指南统计 (冒险者指南 13页) ==========
        long adventurersGuideFound = documentList.stream()
            .filter(d -> d.getDocumentType().equals("ADVENTURERS_GUIDE") && d.isFound())
            .count();
        data.put("adventurersGuideTotal", 13);
        data.put("adventurersGuideFound", adventurersGuideFound);

        // ========== 炼金指南统计 (炼金指南 9页) ==========
        long alchemyGuideFound = documentList.stream()
            .filter(d -> d.getDocumentType().equals("ALCHEMY_GUIDE") && d.isFound())
            .count();
        data.put("alchemyGuideTotal", 9);
        data.put("alchemyGuideFound", alchemyGuideFound);

        // ========== 图鉴 - 装备统计 (165个) ==========
        // 装备分类：近战武器、护甲、附魔、刻印、投掷武器、魔杖、戒指、神器、饰品、杂项装备
        Set<String> equipmentTypes = Set.of(
            "MELEE_WEAPONS", "ARMOR", "ENCHANTMENTS", "GLYPHS", "THROWN_WEAPONS",
            "WANDS", "RINGS", "ARTIFACTS", "TRINKETS", "MISC_EQUIPMENT"
        );
        long equipmentSeen = catalogList.stream()
            .filter(c -> equipmentTypes.contains(c.getCatalogType()) && c.isSeen())
            .count();
        data.put("equipmentTotal", 165);
        data.put("equipmentSeen", equipmentSeen);

        // 装备子分类统计
        Map<String, Integer> equipmentSubtotals = Map.of(
            "MELEE_WEAPONS", 33,      // 近战武器
            "ARMOR", 11,              // 护甲
            "ENCHANTMENTS", 21,       // 附魔与诅咒
            "GLYPHS", 21,             // 刻印与诅咒
            "THROWN_WEAPONS", 16,     // 投掷武器
            "WANDS", 13,              // 法杖
            "RINGS", 12,              // 戒指
            "ARTIFACTS", 13,          // 神器
            "TRINKETS", 17,           // 饰品
            "MISC_EQUIPMENT", 8       // 杂项装备
        );
        Map<String, Object> equipmentDetails = new HashMap<>();
        equipmentSubtotals.forEach((type, total) -> {
            long seen = catalogList.stream()
                .filter(c -> c.getCatalogType().equals(type) && c.isSeen())
                .count();
            Map<String, Object> detail = new HashMap<>();
            detail.put("total", total);
            detail.put("seen", seen);
            equipmentDetails.put(type, detail);
        });
        data.put("equipmentDetails", equipmentDetails);

        // ========== 图鉴 - 消耗品统计 (161个) ==========
        Set<String> consumableTypes = Set.of(
            "POTIONS", "SEEDS", "SCROLLS", "STONES", "FOOD",
            "EXOTIC_POTIONS", "EXOTIC_SCROLLS", "BOMBS", "TIPPED_DARTS",
            "BREWS_ELIXIRS", "SPELLS", "MISC_CONSUMABLES"
        );
        long consumablesSeen = catalogList.stream()
            .filter(c -> consumableTypes.contains(c.getCatalogType()) && c.isSeen())
            .count();
        data.put("consumablesTotal", 161);
        data.put("consumablesSeen", consumablesSeen);

        // 消耗品子分类统计
        Map<String, Integer> consumableSubtotals = new HashMap<>();
        consumableSubtotals.put("POTIONS", 12);            // 药剂
        consumableSubtotals.put("SCROLLS", 12);            // 卷轴
        consumableSubtotals.put("SEEDS", 12);              // 种子
        consumableSubtotals.put("STONES", 12);             // 符石
        consumableSubtotals.put("FOOD", 12);               // 食物
        consumableSubtotals.put("EXOTIC_POTIONS", 12);     // 合剂
        consumableSubtotals.put("EXOTIC_SCROLLS", 12);     // 秘卷
        consumableSubtotals.put("BOMBS", 11);              // 炸弹
        consumableSubtotals.put("TIPPED_DARTS", 12);       // 涂药飞镖
        consumableSubtotals.put("BREWS_ELIXIRS", 14);      // 魔药与秘药
        consumableSubtotals.put("SPELLS", 11);             // 法术结晶
        consumableSubtotals.put("MISC_CONSUMABLES", 29);   // 杂项消耗品
        Map<String, Object> consumablesDetails = new HashMap<>();
        consumableSubtotals.forEach((type, total) -> {
            long seen = catalogList.stream()
                .filter(c -> c.getCatalogType().equals(type) && c.isSeen())
                .count();
            Map<String, Object> detail = new HashMap<>();
            detail.put("total", total);
            detail.put("seen", seen);
            consumablesDetails.put(type, detail);
        });
        data.put("consumablesDetails", consumablesDetails);

        // ========== 图鉴 - 单位图鉴统计 (143个) ==========
        long bestiarySeen = bestiaryList.stream().filter(PlayerBestiary::isSeen).count();
        data.put("bestiaryTotal", 143);  // 30+12+8+15+12+8+10+31+16 = 143
        data.put("bestiarySeen", bestiarySeen);

        // 单位图鉴子分类统计
        Map<String, Integer> bestiarySubtotals = new HashMap<>();
        bestiarySubtotals.put("REGIONAL", 30);      // 区域敌人
        bestiarySubtotals.put("BOSSES", 12);        // 区域Boss
        bestiarySubtotals.put("UNIVERSAL", 8);      // 全局敌人
        bestiarySubtotals.put("RARE", 15);          // 稀有敌人
        bestiarySubtotals.put("QUEST", 12);         // 任务敌人与Boss
        bestiarySubtotals.put("NEUTRAL", 8);        // 中立角色
        bestiarySubtotals.put("ALLY", 10);          // 盟友
        bestiarySubtotals.put("TRAP", 31);          // 陷阱
        bestiarySubtotals.put("PLANT", 16);         // 植物
        Map<String, Object> bestiaryDetails = new HashMap<>();
        bestiarySubtotals.forEach((type, total) -> {
            long seen = bestiaryList.stream()
                .filter(b -> b.getBestiaryType().equals(type) && b.isSeen())
                .count();
            Map<String, Object> detail = new HashMap<>();
            detail.put("total", total);
            detail.put("seen", seen);
            bestiaryDetails.put(type, detail);
        });
        data.put("bestiaryDetails", bestiaryDetails);

        // ========== 图鉴 - 背景故事统计 (36页) ==========
        // 背景故事文档：INTROS, SEWERS_GUARD, PRISON_WARDEN, CAVES_EXPLORER, CITY_WARLOCK, HALLS_KING
        // HALLS_KING共6页，其中第6页(KING_ATTRITION/attrition)是隐藏页，需要特殊条件触发
        // 注意：INTROS的第一页"Dungeon"默认解锁，不需要在数据库中存储
        Set<String> loreDocTypes = Set.of(
            "INTROS", "SEWERS_GUARD", "PRISON_WARDEN",
            "CAVES_EXPLORER", "CITY_WARLOCK", "HALLS_KING"
        );

        // 计算背景故事已发现数量，INTROS的"Dungeon"页面默认已发现
        long loreFound = documentList.stream()
            .filter(d -> loreDocTypes.contains(d.getDocumentType()) && d.isFound())
            .count();
        // 如果数据库中没有INTROS的"Dungeon"记录，默认算作已发现
        boolean hasIntrosDungeon = documentList.stream()
            .anyMatch(d -> "INTROS".equals(d.getDocumentType()) && "Dungeon".equals(d.getPageName()) && d.isFound());
        if (!hasIntrosDungeon) {
            loreFound += 1;  // 默认解锁INTROS的第一页
        }

        data.put("loreTotal", 36);  // 6+6+6+6+6+6 = 36 (包含HALLS_KING的隐藏页)
        data.put("loreFound", loreFound);

        // 背景故事子分类统计
        Map<String, Integer> loreSubtotals = new HashMap<>();
        loreSubtotals.put("INTROS", 6);           // 地牢区域介绍
        loreSubtotals.put("SEWERS_GUARD", 6);     // 巡逻队员的信件
        loreSubtotals.put("PRISON_WARDEN", 6);    // 监狱长日志
        loreSubtotals.put("CAVES_EXPLORER", 6);   // 探险者日志
        loreSubtotals.put("CITY_WARLOCK", 6);     // 矮人术士手记
        loreSubtotals.put("HALLS_KING", 6);       // ？？？录 (6页，第6页是隐藏页)
        Map<String, Object> loreDetails = new HashMap<>();
        loreSubtotals.forEach((type, total) -> {
            long found = documentList.stream()
                .filter(d -> d.getDocumentType().equals(type) && d.isFound())
                .count();
            // INTROS的"Dungeon"页面默认已发现
            if ("INTROS".equals(type)) {
                boolean hasDungeon = documentList.stream()
                    .anyMatch(d -> "INTROS".equals(d.getDocumentType()) && "Dungeon".equals(d.getPageName()) && d.isFound());
                if (!hasDungeon) {
                    found += 1;  // 默认解锁INTROS的第一页
                }
            }
            Map<String, Object> detail = new HashMap<>();
            detail.put("total", total);
            detail.put("found", found);
            loreDetails.put(type, detail);
        });
        data.put("loreDetails", loreDetails);

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
        data.put("adminCount", playerRepository.countByRole(UserRole.ADMIN));
        data.put("bannedCount", playerRepository.countByRole(UserRole.BANNED));
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

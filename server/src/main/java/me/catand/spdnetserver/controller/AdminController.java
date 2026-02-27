package me.catand.spdnetserver.controller;

import me.catand.spdnetserver.SocketService;
import me.catand.spdnetserver.controller.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private SocketService socketService;

    @GetMapping("/players")
    public ApiResponse<Map<String, Object>> getAllPlayers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String role,
        @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Player> players;

        if (search != null && !search.trim().isEmpty()) {
            players = playerRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (role != null && !role.trim().isEmpty()) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                players = playerRepository.findByRole(userRole, pageable);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("无效的角色类型");
            }
        } else {
            players = playerRepository.findAll(pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("players", players.getContent());
        data.put("totalElements", players.getTotalElements());
        data.put("totalPages", players.getTotalPages());
        data.put("currentPage", page);

        return ApiResponse.success("获取成功", data);
    }

    @PostMapping("/player/{id}/role")
    public ApiResponse<Void> setPlayerRole(
        @PathVariable Long id,
        @RequestBody Map<String, String> request
    ) {
        String roleStr = request.get("role");
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return ApiResponse.error("角色不能为空");
        }

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("无效的角色类型");
        }

        Player player = playerRepository.findById(id).orElse(null);
        if (player == null) {
            return ApiResponse.error("玩家不存在");
        }

        player.setRole(newRole);
        playerRepository.save(player);

        if (newRole == UserRole.BANNED) {
            socketService.kickPlayer(player.getName());
        }

        return ApiResponse.success("修改成功");
    }

    @DeleteMapping("/player/{id}")
    public ApiResponse<Void> deletePlayer(@PathVariable Long id) {
        Player player = playerRepository.findById(id).orElse(null);
        if (player == null) {
            return ApiResponse.error("玩家不存在");
        }

        socketService.kickPlayer(player.getName());
        gameRecordRepository.deleteByPlayer(player);
        playerRepository.delete(player);

        return ApiResponse.success("删除成功");
    }

    @GetMapping("/records")
    public ApiResponse<Map<String, Object>> getAllRecords(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Boolean win,
        @RequestParam(required = false) String playerName
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<GameRecord> records;

        if (playerName != null && !playerName.trim().isEmpty()) {
            Player player = playerRepository.findByName(playerName);
            if (player == null) {
                return ApiResponse.error("玩家不存在");
            }
            records = gameRecordRepository.findByPlayerOrderByScoreDesc(player, pageable);
        } else if (win != null) {
            if (win) {
                records = gameRecordRepository.findByWinTrue(pageable);
            } else {
                records = gameRecordRepository.findByWinFalse(pageable);
            }
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

    @DeleteMapping("/record/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        if (!gameRecordRepository.existsById(id)) {
            return ApiResponse.error("记录不存在");
        }
        gameRecordRepository.deleteById(id);
        return ApiResponse.success("删除成功");
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalPlayers", playerRepository.count());
        data.put("totalRecords", gameRecordRepository.count());
        data.put("onlineCount", socketService.getPlayerMap().size());
        data.put("winCount", gameRecordRepository.countByWinTrue());
        data.put("bannedCount", playerRepository.countByRole(UserRole.BANNED));
        data.put("adminCount", playerRepository.countByRole(UserRole.ADMIN));
        return ApiResponse.success("获取成功", data);
    }

    @PostMapping("/broadcast")
    public ApiResponse<Void> broadcastMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ApiResponse.error("消息内容不能为空");
        }
        socketService.broadcastMessage(message);
        return ApiResponse.success("广播发送成功");
    }
}

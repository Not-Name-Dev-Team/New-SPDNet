package me.catand.spdnetserver.controller;

import me.catand.spdnetserver.controller.dto.ApiResponse;
import me.catand.spdnetserver.controller.dto.PrefixOwnerDTO;
import me.catand.spdnetserver.entitys.PlayerPrefix;
import me.catand.spdnetserver.entitys.PlayerPrefixAssignment;
import me.catand.spdnetserver.repositories.PlayerPrefixAssignmentRepository;
import me.catand.spdnetserver.repositories.PlayerPrefixRepository;
import me.catand.spdnetserver.service.PlayerPrefixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SPDNet: 玩家前缀/称号管理控制器
 * 支持多前缀管理和玩家自选
 */
@RestController
@RequestMapping("/api/admin/prefixes")
@CrossOrigin(origins = "*")
public class PlayerPrefixController {

	@Autowired
	private PlayerPrefixService prefixService;

	@Autowired
	private PlayerPrefixRepository prefixRepository;

	@Autowired
	private PlayerPrefixAssignmentRepository assignmentRepository;

	// ==================== 前缀管理（管理员） ====================

	/**
	 * 获取所有前缀
	 */
	@GetMapping
	public ApiResponse<List<Map<String, Object>>> getAllPrefixes() {
		List<PlayerPrefix> prefixes = prefixService.getAllPrefixes();
		List<Map<String, Object>> result = prefixes.stream()
			.map(this::convertPrefixToMap)
			.collect(Collectors.toList());
		return ApiResponse.success("获取成功", result);
	}

	/**
	 * 创建前缀
	 */
	@PostMapping
	public ApiResponse<Map<String, Object>> createPrefix(@RequestBody Map<String, String> request) {
		String name = request.get("name");
		String displayText = request.get("displayText");
		String color = request.get("color");
		String backgroundColor = request.get("backgroundColor");
		String description = request.get("description");

		if (name == null || name.trim().isEmpty()) {
			return ApiResponse.error("前缀标识不能为空");
		}
		if (displayText == null || displayText.trim().isEmpty()) {
			return ApiResponse.error("显示文本不能为空");
		}

		try {
			PlayerPrefix prefix = prefixService.createPrefix(name, displayText, color, backgroundColor, description);
			return ApiResponse.success("创建成功", convertPrefixToMap(prefix));
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	/**
	 * 更新前缀
	 */
	@PutMapping("/{id}")
	public ApiResponse<Map<String, Object>> updatePrefix(
		@PathVariable Long id,
		@RequestBody Map<String, String> request) {
		String displayText = request.get("displayText");
		String color = request.get("color");
		String backgroundColor = request.get("backgroundColor");
		String description = request.get("description");

		if (displayText == null || displayText.trim().isEmpty()) {
			return ApiResponse.error("显示文本不能为空");
		}

		try {
			PlayerPrefix prefix = prefixService.updatePrefix(id, displayText, color, backgroundColor, description);
			return ApiResponse.success("更新成功", convertPrefixToMap(prefix));
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	/**
	 * 删除前缀
	 */
	@DeleteMapping("/{id}")
	public ApiResponse<Void> deletePrefix(@PathVariable Long id) {
		try {
			prefixService.deletePrefix(id);
			return ApiResponse.success("删除成功");
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	// ==================== 玩家前缀管理（管理员） ====================

	/**
	 * 获取玩家的所有前缀
	 */
	@GetMapping("/player/{playerName}")
	public ApiResponse<List<Map<String, Object>>> getPlayerPrefixes(@PathVariable String playerName) {
		try {
			List<PlayerPrefixAssignment> assignments = prefixService.getPlayerPrefixes(playerName);
			List<Map<String, Object>> result = assignments.stream()
				.map(this::convertAssignmentToMap)
				.collect(Collectors.toList());
			return ApiResponse.success("获取成功", result);
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	/**
	 * 给玩家分配前缀
	 */
	@PostMapping("/player/{playerName}/assign")
	public ApiResponse<Void> assignPrefixToPlayer(
		@PathVariable String playerName,
		@RequestBody Map<String, Long> request) {
		Long prefixId = request.get("prefixId");
		if (prefixId == null) {
			return ApiResponse.error("前缀ID不能为空");
		}

		try {
			prefixService.assignPrefixToPlayer(playerName, prefixId);
			return ApiResponse.success("分配成功");
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	/**
	 * 移除玩家的前缀
	 */
	@DeleteMapping("/player/{playerName}/remove")
	public ApiResponse<Void> removePrefixFromPlayer(
		@PathVariable String playerName,
		@RequestParam Long prefixId) {
		try {
			prefixService.removePrefixFromPlayer(playerName, prefixId);
			return ApiResponse.success("移除成功");
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	// ==================== 玩家自选前缀 ====================

	/**
	 * 获取当前登录玩家的所有前缀
	 * SPDNet: 使用@RequestParam接收playerName，因为项目使用localStorage存储用户信息
	 */
	@GetMapping("/my")
	public ApiResponse<List<Map<String, Object>>> getMyPrefixes(@RequestParam String playerName) {
		try {
			List<PlayerPrefixAssignment> assignments = prefixService.getPlayerPrefixes(playerName);
			List<Map<String, Object>> result = assignments.stream()
				.map(this::convertAssignmentToMap)
				.collect(Collectors.toList());
			return ApiResponse.success("获取成功", result);
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	/**
	 * 玩家设置激活的前缀
	 * SPDNet: 使用@RequestParam接收playerName，因为项目使用localStorage存储用户信息
	 */
	@PostMapping("/my/active")
	public ApiResponse<Void> setMyActivePrefix(
		@RequestParam String playerName,
		@RequestBody Map<String, Long> request) {
		Long assignmentId = request.get("assignmentId"); // null表示不激活任何前缀

		try {
			prefixService.setActivePrefix(playerName, assignmentId);
			return ApiResponse.success("设置成功");
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	/**
	 * 获取当前激活的前缀
	 * SPDNet: 使用@RequestParam接收playerName，因为项目使用localStorage存储用户信息
	 */
	@GetMapping("/my/active")
	public ApiResponse<Map<String, Object>> getMyActivePrefix(@RequestParam String playerName) {
		var activePrefix = prefixService.getActivePrefix(playerName);
		if (activePrefix.isPresent()) {
			return ApiResponse.success("获取成功", convertAssignmentToMap(activePrefix.get()));
		} else {
			return ApiResponse.success("当前没有激活的前缀", null);
		}
	}

	// ==================== 前缀详情（公开） ====================

	/**
	 * SPDNet: 获取前缀详细信息（公开接口）
	 */
	@GetMapping("/public/{id}")
	public ApiResponse<Map<String, Object>> getPrefixInfo(@PathVariable Long id) {
		Optional<PlayerPrefix> prefixOpt = prefixRepository.findById(id);
		if (prefixOpt.isEmpty()) {
			return ApiResponse.error("前缀不存在");
		}

		PlayerPrefix prefix = prefixOpt.get();
		Map<String, Object> data = convertPrefixToMap(prefix);

		// 获取拥有者数量
		long ownerCount = assignmentRepository.countByPrefix(prefix);
		data.put("ownerCount", ownerCount);

		return ApiResponse.success("获取成功", data);
	}

	/**
	 * SPDNet: 获取前缀的拥有者列表（公开接口）
	 */
	@GetMapping("/public/{id}/owners")
	public ApiResponse<Map<String, Object>> getPrefixOwners(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		Optional<PlayerPrefix> prefixOpt = prefixRepository.findById(id);
		if (prefixOpt.isEmpty()) {
			return ApiResponse.error("前缀不存在");
		}

		PlayerPrefix prefix = prefixOpt.get();
		Pageable pageable = PageRequest.of(page, size);

		// 获取该前缀的所有分配记录
		List<PlayerPrefixAssignment> assignments = assignmentRepository.findAll().stream()
				.filter(a -> a.getPrefix().getId().equals(id))
				.sorted((a1, a2) -> a2.getAssignedAt().compareTo(a1.getAssignedAt())) // 按分配时间倒序
				.collect(Collectors.toList());

		// 手动分页
		int total = assignments.size();
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), total);

		List<PlayerPrefixAssignment> pageContent = start < total
				? assignments.subList(start, end)
				: List.of();

		// 转换为DTO
		List<PrefixOwnerDTO> owners = pageContent.stream()
				.map(a -> new PrefixOwnerDTO(
						a.getId(),
						a.getPlayer().getName(),
						a.isActive(),
						a.getAssignedAt()
				))
				.collect(Collectors.toList());

		Map<String, Object> data = new HashMap<>();
		data.put("owners", owners);
		data.put("totalElements", total);
		data.put("totalPages", (int) Math.ceil((double) total / size));
		data.put("currentPage", page);

		return ApiResponse.success("获取成功", data);
	}

	// ==================== 辅助方法 ====================

	private Map<String, Object> convertPrefixToMap(PlayerPrefix prefix) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", prefix.getId());
		map.put("name", prefix.getName());
		map.put("displayText", prefix.getDisplayText());
		map.put("color", prefix.getColor());
		map.put("backgroundColor", prefix.getBackgroundColor());
		map.put("description", prefix.getDescription());
		map.put("createdAt", prefix.getCreatedAt());
		map.put("updatedAt", prefix.getUpdatedAt());
		return map;
	}

	private Map<String, Object> convertAssignmentToMap(PlayerPrefixAssignment assignment) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", assignment.getId());
		map.put("active", assignment.isActive());
		map.put("assignedAt", assignment.getAssignedAt());
		if (assignment.getPrefix() != null) {
			map.put("prefix", convertPrefixToMap(assignment.getPrefix()));
		}
		return map;
	}
}

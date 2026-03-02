package me.catand.spdnetserver.service;

import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.entitys.PlayerPrefix;
import me.catand.spdnetserver.entitys.PlayerPrefixAssignment;
import me.catand.spdnetserver.repositories.PlayerPrefixAssignmentRepository;
import me.catand.spdnetserver.repositories.PlayerPrefixRepository;
import me.catand.spdnetserver.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SPDNet: 玩家前缀/称号服务层
 * 支持一个玩家拥有多个前缀，并可选择使用哪一个
 */
@Service
public class PlayerPrefixService {

	@Autowired
	private PlayerPrefixRepository prefixRepository;

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private PlayerPrefixAssignmentRepository assignmentRepository;

	/**
	 * 获取所有前缀
	 */
	public List<PlayerPrefix> getAllPrefixes() {
		return prefixRepository.findAll();
	}

	/**
	 * 根据ID获取前缀
	 */
	public Optional<PlayerPrefix> getPrefixById(Long id) {
		return prefixRepository.findById(id);
	}

	/**
	 * 根据名称获取前缀
	 */
	public Optional<PlayerPrefix> getPrefixByName(String name) {
		return prefixRepository.findByName(name);
	}

	/**
	 * 创建前缀
	 */
	public PlayerPrefix createPrefix(String name, String displayText, String color, String backgroundColor, String description) {
		if (prefixRepository.existsByName(name)) {
			throw new IllegalArgumentException("前缀标识已存在");
		}

		PlayerPrefix prefix = new PlayerPrefix();
		prefix.setName(name);
		prefix.setDisplayText(displayText);
		prefix.setColor(color);
		prefix.setBackgroundColor(backgroundColor);
		prefix.setDescription(description);

		return prefixRepository.save(prefix);
	}

	/**
	 * 更新前缀
	 */
	public PlayerPrefix updatePrefix(Long id, String displayText, String color, String backgroundColor, String description) {
		PlayerPrefix prefix = prefixRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("前缀不存在"));

		prefix.setDisplayText(displayText);
		prefix.setColor(color);
		prefix.setBackgroundColor(backgroundColor);
		prefix.setDescription(description);

		return prefixRepository.save(prefix);
	}

	/**
	 * 删除前缀
	 */
	@Transactional
	public void deletePrefix(Long id) {
		PlayerPrefix prefix = prefixRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("前缀不存在"));

		// 删除所有与该前缀相关的分配记录
		List<PlayerPrefixAssignment> assignments = assignmentRepository.findAll().stream()
			.filter(a -> a.getPrefix().getId().equals(id))
			.collect(Collectors.toList());
		assignmentRepository.deleteAll(assignments);

		prefixRepository.delete(prefix);
	}

	// ==================== 玩家前缀管理 ====================

	/**
	 * 获取玩家的所有前缀
	 */
	public List<PlayerPrefixAssignment> getPlayerPrefixes(String playerName) {
		Player player = playerRepository.findByName(playerName);
		if (player == null) {
			throw new IllegalArgumentException("玩家不存在");
		}
		return assignmentRepository.findByPlayer(player);
	}

	/**
	 * 给玩家分配前缀（管理员操作）
	 */
	@Transactional
	public void assignPrefixToPlayer(String playerName, Long prefixId) {
		Player player = playerRepository.findByName(playerName);
		if (player == null) {
			throw new IllegalArgumentException("玩家不存在");
		}

		PlayerPrefix prefix = prefixRepository.findById(prefixId)
			.orElseThrow(() -> new IllegalArgumentException("前缀不存在"));

		// 检查是否已分配
		if (assignmentRepository.existsByPlayerAndPrefix(player, prefix)) {
			throw new IllegalArgumentException("该玩家已拥有此前缀");
		}

		PlayerPrefixAssignment assignment = new PlayerPrefixAssignment();
		assignment.setPlayer(player);
		assignment.setPrefix(prefix);
		assignment.setActive(false); // 默认不激活，玩家自己选择

		assignmentRepository.save(assignment);
	}

	/**
	 * 移除玩家的前缀（管理员操作）
	 */
	@Transactional
	public void removePrefixFromPlayer(String playerName, Long prefixId) {
		Player player = playerRepository.findByName(playerName);
		if (player == null) {
			throw new IllegalArgumentException("玩家不存在");
		}

		PlayerPrefix prefix = prefixRepository.findById(prefixId)
			.orElseThrow(() -> new IllegalArgumentException("前缀不存在"));

		assignmentRepository.deleteByPlayerAndPrefix(player, prefix);
	}

	/**
	 * 玩家选择激活哪个前缀
	 */
	@Transactional
	public void setActivePrefix(String playerName, Long assignmentId) {
		Player player = playerRepository.findByName(playerName);
		if (player == null) {
			throw new IllegalArgumentException("玩家不存在");
		}

		// 先取消所有激活状态
		assignmentRepository.deactivateAllPrefixesForPlayer(player.getId());

		// 如果assignmentId为null，则只是取消所有激活（不使用前缀）
		if (assignmentId != null) {
			PlayerPrefixAssignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("前缀分配记录不存在"));

			// 验证这个分配是否属于该玩家
			if (!assignment.getPlayer().getId().equals(player.getId())) {
				throw new IllegalArgumentException("无权操作此前缀");
			}

			assignment.setActive(true);
			assignmentRepository.save(assignment);
		}
	}

	/**
	 * 获取玩家当前激活的前缀
	 */
	public Optional<PlayerPrefixAssignment> getActivePrefix(String playerName) {
		Player player = playerRepository.findByName(playerName);
		if (player == null) {
			return Optional.empty();
		}
		return assignmentRepository.findActivePrefixByPlayerId(player.getId());
	}

	/**
	 * 获取玩家当前激活的前缀名称（用于网络传输）
	 */
	public String getActivePrefixName(String playerName) {
		Optional<PlayerPrefixAssignment> activePrefix = getActivePrefix(playerName);
		return activePrefix.map(assignment -> assignment.getPrefix().getName()).orElse(null);
	}

	/**
	 * 获取玩家当前激活的前缀完整信息（包含样式）
	 */
	public me.catand.spdnetserver.controller.dto.PlayerPrefixDTO getActivePrefixDTO(String playerName) {
		Optional<PlayerPrefixAssignment> activePrefix = getActivePrefix(playerName);
		if (activePrefix.isPresent()) {
			PlayerPrefix prefix = activePrefix.get().getPrefix();
			me.catand.spdnetserver.controller.dto.PlayerPrefixDTO dto = new me.catand.spdnetserver.controller.dto.PlayerPrefixDTO();
			dto.setId(prefix.getId());
			dto.setName(prefix.getName());
			dto.setDisplayText(prefix.getDisplayText());
			dto.setColor(prefix.getColor());
			dto.setBackgroundColor(prefix.getBackgroundColor());
			return dto;
		}
		return null;
	}

	/**
	 * 检查玩家是否拥有指定前缀
	 */
	public boolean playerHasPrefix(String playerName, Long prefixId) {
		Player player = playerRepository.findByName(playerName);
		if (player == null) {
			return false;
		}
		PlayerPrefix prefix = prefixRepository.findById(prefixId).orElse(null);
		if (prefix == null) {
			return false;
		}
		return assignmentRepository.existsByPlayerAndPrefix(player, prefix);
	}
}

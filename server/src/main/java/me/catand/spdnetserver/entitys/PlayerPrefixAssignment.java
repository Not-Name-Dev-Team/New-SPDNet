package me.catand.spdnetserver.entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SPDNet: 玩家前缀分配实体
 * 记录玩家拥有的前缀，支持一个玩家拥有多个前缀
 */
@Entity
@Table(name = "player_prefix_assignment")
@Getter
@Setter
public class PlayerPrefixAssignment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prefix_id", nullable = false)
	private PlayerPrefix prefix;

	// 是否是当前使用的前缀
	@Column(name = "is_active")
	private boolean active = false;

	// 分配时间
	@Column(name = "assigned_at")
	private LocalDateTime assignedAt;

	@PrePersist
	protected void onCreate() {
		assignedAt = LocalDateTime.now();
	}
}

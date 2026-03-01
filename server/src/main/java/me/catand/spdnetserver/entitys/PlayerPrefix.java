package me.catand.spdnetserver.entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SPDNet: 玩家前缀/称号系统
 * 用于存储和管理玩家的特殊称号前缀
 */
@Entity
@Getter
@Setter
public class PlayerPrefix {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name;

	@Column(nullable = false)
	private String displayText;

	// 前端显示用的颜色（CSS颜色值）
	private String color;

	// 前端显示用的背景色
	private String backgroundColor;

	// 描述说明
	private String description;

	// 创建时间
	private LocalDateTime createdAt;

	// 最后修改时间
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}

package me.catand.spdnetserver.entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SPDNet: 地牢留言(Ping)系统 - 留言实体（仅绑定 seed+depth，同层共享）。
 * 见 test/ping-design.md §6 数据模型。
 */
@Entity
@Getter
@Setter
@Table(name = "dungeon_notes",
		indexes = @Index(name = "idx_note_seed_depth", columnList = "seed,depth"))
public class DungeonNote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 所属地牢种子（毫秒时间戳，long），与 Status.seed 一致
	@Column(name = "seed")
	private long seed;

	// 所属楼层
	@Column(name = "depth")
	private int depth;

	// 留言格坐标（pos 不做服务端校验，观看端渲染点坐标越界跳过）
	@Column(name = "pos")
	private int pos;

	// 留言类型: PLAYER / MOB / ITEM / PLANT / TRAP / FLOOR（FLOOR 无 snapshot）
	@Column(name = "note_type")
	private String noteType;

	// 对象快照 Bundle 字符串（PLAYER 占位再回填；FLOOR 为 null）
	@Column(name = "snapshot", columnDefinition = "TEXT")
	private String snapshot;

	// 留言文本
	@Column(name = "message", columnDefinition = "TEXT")
	private String message;

	// 作者玩家名（用于"自己的留言"判定与删除鉴权）
	@Column(name = "author")
	private String author;

	// 作者游戏模式（Mode 枚举名: IRONMAN/FUN/DAILY），仅用于观看向作者名着色展示，不参与过滤
	@Column(name = "author_mode")
	private String authorMode;

	// 点赞计数（与 NoteLike 表同事务维护，读取 O(1)）
	@Column(name = "likes")
	private int likes;

	@Column(name = "create_time")
	private LocalDateTime createTime;
}
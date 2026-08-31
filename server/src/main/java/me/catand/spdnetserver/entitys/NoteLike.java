package me.catand.spdnetserver.entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * SPDNet: 地牢留言(Ping)系统 - 点赞记录实体。
 * (noteId, playerName) 唯一约束，保证每人每条只能点一次赞（重复即 toggle 取消）。
 * 见 test/ping-design.md §6 点赞表。
 */
@Entity
@Getter
@Setter
@Table(name = "note_likes",
		uniqueConstraints = @UniqueConstraint(name = "uk_note_like_player", columnNames = {"note_id", "player_name"}))
public class NoteLike {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 被点赞的留言 id
	@Column(name = "note_id")
	private Integer noteId;

	// 点赞玩家名
	@Column(name = "player_name")
	private String playerName;
}
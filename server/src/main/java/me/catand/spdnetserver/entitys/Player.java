package me.catand.spdnetserver.entitys;

import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.catand.spdnetserver.data.Status;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
/**
 * 玩家实体类
 * 没有@Transient的属性将会在数据库中被持久化
 * 没有@JSONField(serialize = false)的属性都会在playyerList中发送到其他玩家的客户端
 */
public class Player {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JSONField(serialize = false)
	private Long id;

	@Column(unique = true)
	private Long qq;

	@Column(unique = true)
	private String name;

	@Column(unique = true)
	@JSONField(serialize = false)
	private String key;

	private String power;

	@JSONField(serialize = false)
	private String email;

	// 先搁置 实现日期不另行通知
//	@Lob
//	@Transient
//	@JSONField(serialize = false)
//	private String cloudSaveData;

	@Transient
	// 所有基于当前运行中游戏的属性
	private Status status;

	// 等下次加 摸了
//	@Transient
//	// 都隐身了 其他玩家怎么可能得到isVisible为false的玩家呢 :P
//	@JSONField(serialize = false)
//	// 此属性基于单次连接
//	private boolean isVisible;

	@OneToMany(mappedBy = "player")
	@JSONField(serialize = false)
	private List<GameRecord> gameRecords;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "player_achievements", joinColumns = @JoinColumn(name = "player_id"))
	@Column(name = "achievement")
	@JSONField(serialize = false)
	private Set<String> achievements;
}

package me.catand.spdnetserver.entitys;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.catand.spdnetserver.data.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"gameRecords", "achievements", "hibernateLazyInitializer", "handler"})
public class Player {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private Long id;

	@Column(unique = true)
	private String name;

	@Column(unique = true)
	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private String email;

	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private String password;

	@Enumerated(EnumType.STRING)
	private UserRole role;

	@Transient
	private Status status;

	@OneToMany(mappedBy = "player")
	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private List<GameRecord> gameRecords;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "player_achievements", joinColumns = @JoinColumn(name = "player_id"))
	@Column(name = "achievement")
	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private Set<String> achievements;

	// 注册时间
	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private LocalDateTime createdAt;

	// 最后登录时间
	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private LocalDateTime lastLoginAt;

	// 最后登录IP
	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private String lastLoginIp;

	// SPDNet: 前缀系统 - 玩家拥有的前缀列表（不直接序列化，通过DTO返回）
	@OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
	@JsonIgnore
	@JSONField(serialize = false, deserialize = false)
	private List<PlayerPrefixAssignment> prefixAssignments;

	// SPDNet: 获取当前激活的前缀名称（用于网络传输）
	@Transient
	private String prefixName;
}

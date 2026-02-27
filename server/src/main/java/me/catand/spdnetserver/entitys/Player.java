package me.catand.spdnetserver.entitys;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.catand.spdnetserver.data.Status;

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
	private Long id;

	@Column(unique = true)
	private Long qq;

	@Column(unique = true)
	private String name;

	@Column(unique = true)
	@JsonIgnore
	private String key;

	private String power;

	@JsonIgnore
	private String email;

	@Transient
	private Status status;

	@OneToMany(mappedBy = "player")
	@JsonIgnore
	private List<GameRecord> gameRecords;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "player_achievements", joinColumns = @JoinColumn(name = "player_id"))
	@Column(name = "achievement")
	@JsonIgnore
	private Set<String> achievements;
}

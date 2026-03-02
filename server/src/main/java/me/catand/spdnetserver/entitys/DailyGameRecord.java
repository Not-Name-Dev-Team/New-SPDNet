package me.catand.spdnetserver.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"player", "gameRecord"})
@Table(name = "daily_game_record",
	uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "record_date", "group_index"}),
	indexes = {
		@Index(name = "idx_date_group", columnList = "record_date, group_index"),
		@Index(name = "idx_player_status", columnList = "player_id, status")
	})
public class DailyGameRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;

	@ManyToOne
	@JoinColumn(name = "player_id", nullable = false)
	@JsonIgnore
	private Player player;

	@Column(name = "record_date", nullable = false)
	private LocalDate recordDate;

	@Column(name = "group_index", nullable = false)
	private Integer groupIndex;

	@Column(nullable = false)
	private Long seed;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DailyRecordStatus status = DailyRecordStatus.CREATED;

	@OneToOne
	@JoinColumn(name = "game_record_id")
	@JsonIgnore
	private GameRecord gameRecord;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Transient
	private String playerName;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public void markCompleted() {
		this.status = DailyRecordStatus.COMPLETED;
		this.completedAt = LocalDateTime.now();
	}

	public enum DailyRecordStatus {
		CREATED,
		COMPLETED,
		ABANDONED
	}
}

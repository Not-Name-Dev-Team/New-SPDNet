package me.catand.spdnetserver.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.entitys.DailyGameRecord;
import me.catand.spdnetserver.entitys.GameRecord;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyChallengeRecordDTO {
	private Long id;
	private String playerName;
	private Integer groupIndex;
	private Long seed;
	private String status;
	private Integer score;
	private Boolean win;
	private String heroClass;
	private Integer depth;
	private Float duration;
	private Integer challenges;
	private String createdAt;
	private String completedAt;
	private Long gameRecordId;

	private PlayerPrefixDTO prefix;

	public static DailyChallengeRecordDTO fromEntity(DailyGameRecord record) {
		DailyChallengeRecordDTO dto = new DailyChallengeRecordDTO();
		dto.setId(record.getId());
		dto.setPlayerName(record.getPlayer() != null ? record.getPlayer().getName() : "Unknown");
		dto.setGroupIndex(record.getGroupIndex());
		dto.setSeed(record.getSeed());
		dto.setStatus(record.getStatus().name());

		GameRecord gameRecord = record.getGameRecord();
		if (gameRecord != null) {
			dto.setScore(gameRecord.getTotalScore());
			dto.setWin(gameRecord.isWin());
			dto.setHeroClass(gameRecord.getHeroClass());
			dto.setDepth(gameRecord.getDepth());
			dto.setDuration(gameRecord.getDuration());
			dto.setChallenges(gameRecord.getChallenges());
			dto.setGameRecordId(gameRecord.getId());
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (record.getCreatedAt() != null) {
			dto.setCreatedAt(record.getCreatedAt().format(formatter));
		}
		if (record.getCompletedAt() != null) {
			dto.setCompletedAt(record.getCompletedAt().format(formatter));
		}

		return dto;
	}
}

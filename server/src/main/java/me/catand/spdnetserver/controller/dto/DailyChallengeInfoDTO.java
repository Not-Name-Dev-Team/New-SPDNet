package me.catand.spdnetserver.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyChallengeInfoDTO {
	private Integer groupIndex;
	private String groupName;
	private String date;
	private Integer challengeCount;
	private List<String> challengeNames;
	private Integer totalParticipants;
	private Integer completedCount;
	private Integer winCount;
	private Double winRate;
	private Double completionRate;
}

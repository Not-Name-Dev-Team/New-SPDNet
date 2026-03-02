package me.catand.spdnetserver.data.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SAllowDailyChallenge extends Data {
	private Integer groupIndex;
	private Long seed;
	private String recordDate;
	private boolean hasExistingRecord;
	private Integer challenges;
}

package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CEnterDungeon extends Data {
	private Status status;
	private Integer dailyGroupIndex;
	private Long dailySeed;
	private String dailyRecordDate;

	public CEnterDungeon(Status status) {
		this.status = status;
		this.dailyGroupIndex = null;
		this.dailySeed = null;
		this.dailyRecordDate = null;
	}
}

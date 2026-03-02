package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;
import com.watabou.utils.Bundle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CGameEnd extends Data {
	private String record;
	private Integer dailyGroupIndex;
	private Long dailySeed;
	private String dailyRecordDate;

	public CGameEnd(String record) {
		this.record = record;
		this.dailyGroupIndex = null;
		this.dailySeed = null;
		this.dailyRecordDate = null;
	}
}

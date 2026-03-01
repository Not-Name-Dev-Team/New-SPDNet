package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SGameEnd extends Data {
	private String name;
	private String record;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SGameEnd(String name, String record) {
		this.name = name;
		this.record = record;
	}
}

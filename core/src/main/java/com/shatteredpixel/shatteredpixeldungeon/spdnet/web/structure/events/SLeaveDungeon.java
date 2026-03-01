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
public class SLeaveDungeon extends Data {
	private String name;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SLeaveDungeon(String name) {
		this.name = name;
	}
}

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
public class SExit extends Data {
	private String name;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SExit(String name) {
		this.name = name;
	}
}

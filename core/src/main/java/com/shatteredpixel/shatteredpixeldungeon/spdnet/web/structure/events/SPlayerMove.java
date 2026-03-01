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
public class SPlayerMove extends Data {
	private String name;
	private int pos;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SPlayerMove(String name, int pos) {
		this.name = name;
		this.pos = pos;
	}
}

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
public class SJoin extends Data {
	private String name;
	private String role;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SJoin(String name, String role) {
		this.name = name;
		this.role = role;
	}
}

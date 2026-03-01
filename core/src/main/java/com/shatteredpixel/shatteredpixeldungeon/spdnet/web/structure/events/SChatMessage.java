package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SChatMessage extends Data {
	private String name;
	private String message;
	private String time;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SChatMessage(String name, String message, String time) {
		this.name = name;
		this.message = message;
		this.time = time;
	}
}

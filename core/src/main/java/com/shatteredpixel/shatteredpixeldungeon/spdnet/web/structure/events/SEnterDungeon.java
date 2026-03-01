package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

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
public class SEnterDungeon extends Data {
	private String name;
	private Status status;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SEnterDungeon(String name, Status status) {
		this.name = name;
		this.status = status;
	}
}

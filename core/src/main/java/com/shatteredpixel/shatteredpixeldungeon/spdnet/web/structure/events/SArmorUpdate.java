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
public class SArmorUpdate extends Data {
	private String name;
	private int armorTier;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SArmorUpdate(String name, int armorTier) {
		this.name = name;
		this.armorTier = armorTier;
	}
}

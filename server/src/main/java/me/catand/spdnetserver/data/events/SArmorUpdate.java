package me.catand.spdnetserver.data.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

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

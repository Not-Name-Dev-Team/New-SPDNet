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
public class SAchievement extends Data {
	private String name;
	private String badgeEnumString;
	private boolean unique;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SAchievement(String name, String badgeEnumString, boolean unique) {
		this.name = name;
		this.badgeEnumString = badgeEnumString;
		this.unique = unique;
	}
}

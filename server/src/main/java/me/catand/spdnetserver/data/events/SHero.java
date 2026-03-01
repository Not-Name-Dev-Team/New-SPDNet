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
public class SHero extends Data {
	private String targetName;
	// json
	private String hero;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SHero(String targetName, String hero) {
		this.targetName = targetName;
		this.hero = hero;
	}
}

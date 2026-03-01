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
public class SFloatingText extends Data {
	private String name;
	private int color;
	private String text;
	private int icon;
	// 用于显示血条
	private int heroHP;
	private int heroShield;
	private int heroHT;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SFloatingText(String name, int color, String text, int icon, int heroHP, int heroShield, int heroHT) {
		this.name = name;
		this.color = color;
		this.text = text;
		this.icon = icon;
		this.heroHP = heroHP;
		this.heroShield = heroShield;
		this.heroHT = heroHT;
	}
}

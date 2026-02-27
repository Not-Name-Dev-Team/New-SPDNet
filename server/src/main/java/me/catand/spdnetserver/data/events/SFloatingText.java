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
}

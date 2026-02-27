package me.catand.spdnetserver.data.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CFloatingText extends Data {
	private int color;
	private String text;
	private int icon;
	// 用于显示血条
	private int heroHP;
	private int heroShield;
	private int heroHT;
}

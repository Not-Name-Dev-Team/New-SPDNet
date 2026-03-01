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
public class SAnkhUsed extends Data {
	private String name;
	private String cause;
	private int unusedBlessedAnkh;
	private int unusedUnblessedAnkh;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SAnkhUsed(String name, String cause, int unusedBlessedAnkh, int unusedUnblessedAnkh) {
		this.name = name;
		this.cause = cause;
		this.unusedBlessedAnkh = unusedBlessedAnkh;
		this.unusedUnblessedAnkh = unusedUnblessedAnkh;
	}
}

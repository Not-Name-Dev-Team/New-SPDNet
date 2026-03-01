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
public class SExit extends Data {
	private String name;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SExit(String name) {
		this.name = name;
	}
}

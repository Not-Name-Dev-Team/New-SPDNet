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
public class SJoin extends Data {
	private String name;
	private String role;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SJoin(String name, String role) {
		this.name = name;
		this.role = role;
	}
}

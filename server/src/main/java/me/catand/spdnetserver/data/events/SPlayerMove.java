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
public class SPlayerMove extends Data {
	private String name;
	private int pos;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SPlayerMove(String name, int pos) {
		this.name = name;
		this.pos = pos;
	}
}

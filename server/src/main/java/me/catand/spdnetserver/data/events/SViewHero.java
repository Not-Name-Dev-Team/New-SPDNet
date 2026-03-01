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
public class SViewHero extends Data {
	// 查看自己的目标玩家名字
	private String sourceName;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SViewHero(String sourceName) {
		this.sourceName = sourceName;
	}
}

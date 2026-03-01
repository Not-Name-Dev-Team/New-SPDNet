package me.catand.spdnetserver.data.events;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;
import me.catand.spdnetserver.entitys.GameRecord;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SGameEnd extends Data {
	private String name;
	private String record;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SGameEnd(String name, GameRecord record) {
		this.name = name;
		this.record = JSON.toJSONString(record);
	}

	public SGameEnd(String name, GameRecord record, String prefix) {
		this(name, record);
		this.prefix = prefix;
	}
}

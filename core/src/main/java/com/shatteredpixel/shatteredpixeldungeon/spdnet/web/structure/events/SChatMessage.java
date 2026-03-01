package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// SPDNet: 添加时间字段用于显示消息时间
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SChatMessage extends Data {
	private String name;
	private String message;
	private String time;

	// SPDNet: 保持向后兼容的无参构造函数
	public SChatMessage(String name, String message) {
		this.name = name;
		this.message = message;
		this.time = "";
	}
}

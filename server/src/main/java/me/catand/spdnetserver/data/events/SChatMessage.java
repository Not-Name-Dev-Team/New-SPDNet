package me.catand.spdnetserver.data.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;

@Getter
@Setter
@NoArgsConstructor
public class SChatMessage extends Data {
	private String name;
	private String message;
	private String time;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public SChatMessage(String name, String message, String time) {
		this.name = name;
		this.message = message;
		// 如果客户端没有提供时间，则使用服务端时间作为后备
		if (time == null || time.isEmpty()) {
			this.time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
		} else {
			this.time = time;
		}
	}

	// SPDNet: 前缀系统 - 带前缀的构造方法
	public SChatMessage(String name, String message, String time, String prefix) {
		this(name, message, time);
		this.prefix = prefix;
	}
}

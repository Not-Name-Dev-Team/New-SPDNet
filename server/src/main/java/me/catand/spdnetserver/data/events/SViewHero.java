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
	// SPDNet: 地牢留言(Ping)系统 - 是否处于留言取快照模式（true 时静默不弹"被查看"，仅序列化 hero 回 CHero）
	private boolean forNote;

	public SViewHero(String sourceName) {
		this.sourceName = sourceName;
	}

	// SPDNet: 向后兼容 - 查看链路现有两参调用点，forNote 默认 false
	public SViewHero(String sourceName, String prefix) {
		this.sourceName = sourceName;
		this.prefix = prefix;
	}
}

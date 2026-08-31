package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;
import com.watabou.utils.Bundle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CHero extends Data {
	private String sourceName;
	// json
	private String hero;
	// SPDNet: 地牢留言(Ping)系统 - 是否处于留言取快照模式（true 时服务端拦截落库为留言快照，不回传触发查看窗）
	private boolean forNote;

	public CHero(Hero hero) {
		Bundle bundle = new Bundle();
		hero.storeInBundle(bundle);
		this.hero = bundle.toString();
	}

	// SPDNet: 向后兼容 - 查看链路现有两参调用点，forNote 默认 false
	public CHero(String sourceName, String hero) {
		this.sourceName = sourceName;
		this.hero = hero;
	}
}

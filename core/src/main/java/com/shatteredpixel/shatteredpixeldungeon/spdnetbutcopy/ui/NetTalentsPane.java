package com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;

/**
 * SPDNet: TalentsPane的子类，用于查看其他玩家的天赋
 * 
 * 修改思路：
 * 通过覆盖 getHero() 方法返回目标玩家的 Hero 对象，
 * 从而复用父类的所有渲染逻辑，只需最小改动。
 * 
 * 与原类的关系：
 * - 继承 TalentsPane
 * - 覆盖 getHero() 方法
 * 
 * 用途：在 NetWndPlayerInfo 的天赋标签页中显示其他玩家的天赋。
 */
public class NetTalentsPane extends TalentsPane {

	private Hero hero;

	public NetTalentsPane(Hero hero, TalentButton.Mode mode) {
		// SPDNet: 使用新的三参数构造函数，传入 hero 避免父类构造函数中 getHero() 返回 null
		super(mode, hero.talents, hero);
		this.hero = hero;
	}

	// SPDNet: 覆盖getHero方法，返回目标玩家而非本地玩家
	@Override
	protected Hero getHero() {
		return hero;
	}
}

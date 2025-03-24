package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Status {
	private int challenges;
	private long seed;
	private int heroClass;
	private int gameMode;
	private int depth;
	/**
	 * 护甲等阶, 大多数情况下就是Armor的tier
	 * 但是有两种特殊情况:
	 * 没穿护甲的时候是0
	 * 穿职业护甲的时候是6(和板甲区分开)
	 */
	private int armorTier;
	private int pos;

	public Status(int challenges, long seed, int heroClass, Mode mode, int depth, Armor armor, int pos) {
		this.challenges = challenges;
		this.seed = seed;
		this.heroClass = heroClass;
		this.gameMode = mode.ordinal();
		this.depth = depth;
		if (armor == null) {
			armorTier = 0;
		} else if (armor instanceof ClassArmor) {
			armorTier = 6;
		} else {
			armorTier = armor.tier;
		}
		this.pos = pos;
	}

	public HeroClass getHeroClassEnum() {
		return HeroClass.values()[heroClass];
	}

	public Mode getGameModeEnum() {
		return Mode.values()[gameMode];
	}
}

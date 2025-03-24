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

	public CHero(Hero hero) {
		Bundle bundle = new Bundle();
		hero.storeInBundle(bundle);
		this.hero = bundle.toString();
	}
}

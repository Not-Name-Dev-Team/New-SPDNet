package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SAchievement extends Data {
	private String name;
	private String badgeEnumString;
	private boolean unique;

	public Badges.Badge getBadge() {
		return Badges.Badge.valueOf(badgeEnumString);
	}
}

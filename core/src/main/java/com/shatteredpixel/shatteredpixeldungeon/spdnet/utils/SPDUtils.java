package com.shatteredpixel.shatteredpixeldungeon.spdnet.utils;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;

public class SPDUtils {
	public static int activeChallenges(int challenges) {
		int chCount = 0;
		for (int ch : Challenges.MASKS) {
			if ((challenges & ch) != 0) chCount++;
		}
		return chCount;
	}
}

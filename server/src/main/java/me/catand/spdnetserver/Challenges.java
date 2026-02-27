package me.catand.spdnetserver;

public class Challenges {
	public static final int NO_FOOD				= 1;
	public static final int NO_ARMOR			= 2;
	public static final int NO_HEALING			= 4;
	public static final int NO_HERBALISM		= 8;
	public static final int SWARM_INTELLIGENCE	= 16;
	public static final int DARKNESS			= 32;
	public static final int NO_SCROLLS		    = 64;
	public static final int CHAMPION_ENEMIES	= 128;
	public static final int STRONGER_BOSSES 	= 256;

	public static final int MAX_VALUE           = 511;

	public static final String[] NAME_IDS = {
			"champion_enemies",
			"stronger_bosses",
			"no_food",
			"no_armor",
			"no_healing",
			"no_herbalism",
			"swarm_intelligence",
			"darkness",
			"no_scrolls"
	};

	public static final int[] MASKS = {
			CHAMPION_ENEMIES, STRONGER_BOSSES, NO_FOOD, NO_ARMOR, NO_HEALING, NO_HERBALISM, SWARM_INTELLIGENCE, DARKNESS, NO_SCROLLS
	};

	public static int countActiveChallenges(int challenges){
		int chCount = 0;
		for (int ch : Challenges.MASKS){
			if ((challenges & ch) != 0) chCount++;
		}
		return chCount;
	}
}
package com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.scene;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.utils.SPDUtils;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChallenges;

public class ChallengeButton extends StyledButton {
	public ChallengeButton() {
		super(Chrome.Type.WINDOW, "挑战未开启", 9);
		icon(Icons.get(Icons.CHALLENGE_GREY));
		width = 120;
		height = 20;
	}

	@Override
	protected void onClick() {
		ShatteredPixelDungeon.scene().addToFront(new WndChallenges(SPDSettings.challenges(), true));
	}

	@Override
	public void update() {
		super.update();
		if (SPDSettings.challenges() > 0) {
			text("当前挑战数量: " + SPDUtils.activeChallenges(SPDSettings.challenges()));
			icon(Icons.get(Icons.CHALLENGE_COLOR));
		} else {
			text("挑战未开启");
			icon(Icons.get(Icons.CHALLENGE_GREY));
		}
	}
}

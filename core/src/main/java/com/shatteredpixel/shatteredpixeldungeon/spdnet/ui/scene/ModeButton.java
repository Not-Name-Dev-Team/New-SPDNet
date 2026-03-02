package com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.scene;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.NetInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Game;

public class ModeButton extends StyledButton {

	public ModeButton(Mode mode) {
		super(Chrome.Type.RED_BUTTON, getDisplayText(mode), 9);
		icon(mode.getIcon());
		width = 70;
		height = 20;
	}

	private static String getDisplayText(Mode mode) {
		if (mode == Mode.DAILY && NetInProgress.isDailyChallenge()) {
			String suffix = getDailySuffix(NetInProgress.dailyGroupIndex);
			return mode.getName() + "-" + suffix;
		}
		return mode.getName();
	}

	private static String getDailySuffix(int groupIndex) {
		if (groupIndex == 0) {
			return "新手";
		} else if (groupIndex == 1) {
			return "高手";
		} else if (groupIndex == 2) {
			return "大师";
		}
		return "";
	}

	public void setMode(Mode mode) {
		text(getDisplayText(mode));
		icon(mode.getIcon());
	}

	@Override
	protected void onClick() {
		Game.runOnRenderThread(() -> {
			ShatteredPixelDungeon.scene().add(new ModeWindow());
		});
	}

	@Override
	public void update() {
		super.update();
		setMode(NetInProgress.mode);
	}
}

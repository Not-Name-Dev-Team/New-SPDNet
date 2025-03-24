package com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.scene;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.NetInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Game;

public class ModeButton extends StyledButton {

	public ModeButton(Mode mode) {
		super(Chrome.Type.RED_BUTTON, mode.getName(), 9);
		icon(mode.getIcon());
		width = 70;
		height = 20;
	}

	public void setMode(Mode mode) {
		text(mode.getName());
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

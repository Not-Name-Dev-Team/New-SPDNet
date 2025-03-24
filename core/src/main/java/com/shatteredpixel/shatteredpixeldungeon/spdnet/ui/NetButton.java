package com.shatteredpixel.shatteredpixeldungeon.spdnet.ui;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.windows.NetWindow;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;

public class NetButton extends IconButton {
	public static final int HEIGHT = 20;
	public static final int MIN_WIDTH = 21;

	public NetButton() {
		super(NetIcons.get(NetIcons.GLOBE));
		this.width = MIN_WIDTH;
		this.height = HEIGHT;
	}

	@Override
	protected void onClick() {
		super.onClick();
		NetWindow.showServerInfo();
	}

	@Override
	protected boolean onLongClick() {
		NetWindow.showKeyInput();
		return true;
	}

	@Override
	public synchronized void update() {
		super.update();
		if (Net.isConnected()) {
			this.icon().hardlight(0x22FF22);
		} else {
			this.icon().color(0xFF3333);
		}
	}
}

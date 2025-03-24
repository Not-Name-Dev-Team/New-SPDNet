package com.shatteredpixel.shatteredpixeldungeon.spdnet.ui;

import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;

public class BlueButton extends StyledButton {

	public BlueButton(String label) {
		this(label, 7);
	}

	public BlueButton(String label, int size) {
		super(SPDNetChrome.Type.BUTTON, label, size);
	}

}

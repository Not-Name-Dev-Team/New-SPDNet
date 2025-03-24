package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.BlueButton;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.LabeledText;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.NetIcons;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;

public class NetWndMotd extends NetWindow {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;

	private static final int MARGIN = 2;

	public NetWndMotd(String motd) {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = 0;
		//TODO 多语言
		IconTitle tfTitle = new IconTitle(NetIcons.get(NetIcons.NEWS), "欢迎登录");
		tfTitle.setRect(0, pos, width, 0);
		add(tfTitle);

		pos = tfTitle.bottom() + 2 * MARGIN;

		layoutBody(pos, motd);
	}

	private void layoutBody(float pos, String motd) {
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		RenderedTextBlock tfMesage = PixelScene.renderTextBlock(8);
		tfMesage.text(motd, width);
		tfMesage.setPos(0, pos);
		add(tfMesage);

		pos = tfMesage.bottom() + 2 * MARGIN;

		LabeledText seedText = new LabeledText("已登录", Net.name, 8, 8) {
			@Override
			protected void layout() {
				super.layout();
				text().hardlight(0x008000);
			}
		};
		seedText.setPos(0, pos);
		add(seedText);

		BlueButton playBtn = new BlueButton("好好好") {
			@Override
			protected void onClick() {
				super.onClick();
				NetWndMotd.this.destroy();
			}
		};

		playBtn.setRect(width - 30, pos, 30, 20);
		add(playBtn);

		pos = playBtn.bottom() + 2 * MARGIN;

		resize(width, (int) (pos - MARGIN));
	}

	protected boolean enabled(int index) {
		return true;
	}

	protected void onSelect(int index) {
	}
}

package com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.scene;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class DailyChallengeConfirmWindow extends Window {
	private static final int WIDTH = 120;
	private static final int MARGIN = 4;
	private static final int BUTTON_HEIGHT = 18;

	public DailyChallengeConfirmWindow(Runnable onConfirm) {
		super(WIDTH, 0, Chrome.get(Chrome.Type.WINDOW));

		int y = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock("警告", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2, y);
		add(title);
		y += title.height() + MARGIN;

		RenderedTextBlock message = PixelScene.renderTextBlock("你已经创建过该组别的挑战。\n\n将以普通模式游玩，成绩不会计入每日挑战排行榜。", 7);
		message.maxWidth(WIDTH - MARGIN * 2);
		message.setPos(MARGIN, y);
		add(message);
		y += message.height() + MARGIN * 2;

		int buttonWidth = (WIDTH - MARGIN * 3) / 2;

		StyledButton confirmBtn = new StyledButton(Chrome.Type.RED_BUTTON, "继续", 8) {
			@Override
			protected void onClick() {
				onConfirm.run();
				hide();
			}
		};
		confirmBtn.setRect(MARGIN, y, buttonWidth, BUTTON_HEIGHT);
		add(confirmBtn);

		StyledButton cancelBtn = new StyledButton(Chrome.Type.RED_BUTTON, "取消", 8) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		cancelBtn.setRect(MARGIN * 2 + buttonWidth, y, buttonWidth, BUTTON_HEIGHT);
		add(cancelBtn);
		y += BUTTON_HEIGHT + MARGIN;

		resize(WIDTH, y);
	}
}

package com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.scene;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.NetInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

import java.util.ArrayList;

public class DailyChallengeDetailWindow extends Window {
	private static final int WIDTH = 130;
	private static final int MARGIN = 4;
	private static final int BUTTON_HEIGHT = 18;
	private static final int CHECKBOX_HEIGHT = 16;

	private final int groupIndex;
	private final long seed;
	private final String recordDate;
	private final boolean hasExistingRecord;
	private final int challenges;

	public DailyChallengeDetailWindow(int groupIndex, long seed, String recordDate, boolean hasExistingRecord, int challenges) {
		super(WIDTH, 0, Chrome.get(Chrome.Type.WINDOW));
		this.groupIndex = groupIndex;
		this.seed = seed;
		this.recordDate = recordDate;
		this.hasExistingRecord = hasExistingRecord;
		this.challenges = challenges;

		createLayout();
	}

	private void createLayout() {
		int y = MARGIN;

		String groupName = getGroupName(groupIndex);
		RenderedTextBlock title = PixelScene.renderTextBlock(groupName, 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2, y);
		add(title);
		y += title.height() + MARGIN;

		RenderedTextBlock dateText = PixelScene.renderTextBlock("日期: " + recordDate, 8);
		dateText.setPos(MARGIN, y);
		add(dateText);
		y += dateText.height() + MARGIN * 2;

		RenderedTextBlock challengeTitle = PixelScene.renderTextBlock("挑战预览:", 8);
		challengeTitle.hardlight(TITLE_COLOR);
		challengeTitle.setPos(MARGIN, y);
		add(challengeTitle);
		y += challengeTitle.height() + MARGIN;

		ArrayList<CheckBox> checkBoxes = new ArrayList<>();
		for (int i = 0; i < Challenges.NAME_IDS.length; i++) {
			CheckBox cb = new CheckBox(Messages.get(Challenges.class, Challenges.NAME_IDS[i]));
			cb.checked((challenges & Challenges.MASKS[i]) != 0);
			cb.enable(false);
			cb.setRect(MARGIN, y, WIDTH - MARGIN * 2, CHECKBOX_HEIGHT);
			add(cb);
			checkBoxes.add(cb);
			y += CHECKBOX_HEIGHT;
		}

		y += MARGIN;

		if (hasExistingRecord) {
			RenderedTextBlock warningText = PixelScene.renderTextBlock("你已经创建过该组别的挑战！", 8);
			warningText.hardlight(0xFF6666);
			warningText.setPos(MARGIN, y);
			add(warningText);
			y += warningText.height() + MARGIN;
		}

		int buttonWidth = (WIDTH - MARGIN * 3) / 2;

		StyledButton confirmBtn = new StyledButton(Chrome.Type.RED_BUTTON, "确定", 8) {
			@Override
			protected void onClick() {
				if (hasExistingRecord) {
					Game.scene().add(new DailyChallengeConfirmWindow(() -> {
						confirmAsNormalMode();
					}));
				} else {
					confirmDailyChallenge();
				}
			}
		};
		confirmBtn.setRect(MARGIN, y, buttonWidth, BUTTON_HEIGHT);
		add(confirmBtn);

		StyledButton cancelBtn = new StyledButton(Chrome.Type.RED_BUTTON, "取消", 8) {
			@Override
			protected void onClick() {
				NetInProgress.resetDailyChallenge();
				hide();
			}
		};
		cancelBtn.setRect(MARGIN * 2 + buttonWidth, y, buttonWidth, BUTTON_HEIGHT);
		add(cancelBtn);
		y += BUTTON_HEIGHT + MARGIN;

		resize(WIDTH, y);
	}

	private void confirmDailyChallenge() {
		NetInProgress.mode = Mode.DAILY;
		NetInProgress.dailyGroupIndex = groupIndex;
		NetInProgress.seed = seed;
		NetInProgress.seedName = "dailyGroup" + groupIndex;
		NetInProgress.dailyRecordDate = recordDate;
		NetInProgress.dailyChallenges = challenges;
		hide();
	}

	private void confirmAsNormalMode() {
		NetInProgress.mode = Mode.FUN;
		NetInProgress.seed = seed;
		NetInProgress.seedName = "dailyGroup" + groupIndex + "_repeat";
		NetInProgress.resetDailyChallenge();
		hide();
	}

	private String getGroupName(int groupIndex) {
		if (groupIndex == 0) {
			return "新手(0-3挑)";
		} else if (groupIndex == 1) {
			return "高手(4-6挑)";
		} else if (groupIndex == 2) {
			return "大师(7-9挑)";
		} else {
			return "未知组别";
		}
	}
}

package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.scene.NetRankingsScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Sender;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CRequestLeaderboard;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.ColorBlock;

public class NetWndLeaderboardSelect extends Window {
	RenderedTextBlock title;
	ColorBlock sep1;
	OptionSlider optType;
	// ↓缺一个文本输入框
	RedButton btnSearch;
	ColorBlock sep2;
	OptionSlider optChallenge;
	OptionSlider optWinOnly;
	OptionSlider optMode;
	ColorBlock sep3;
	OptionSlider optSortCriteria;

	public NetWndLeaderboardSelect() {
		resize(120, 200);

		title = PixelScene.renderTextBlock("排行榜设置", 9);
		title.hardlight(TITLE_COLOR);
		add(title);

		sep1 = new ColorBlock(1, 1, 0xFF000000);
		add(sep1);

		btnSearch = new RedButton(NetRankingsScene.playerName == null ? "查看所有" : "查看: " + NetRankingsScene.playerName) {
			@Override
			protected void onClick() {
				if (optType == null || optType.getSelectedValue() == 1) {
					return;
				}
				ShatteredPixelDungeon.scene().add(new WndTextInput("输入你要查看的玩家名", null, NetRankingsScene.playerName, 30, false, "确定", "取消") {
					@Override
					public void onSelect(boolean positive, String text) {
						if (positive) {
							NetRankingsScene.playerName = text;
							btnSearch.text("查看: " + text);
						}
					}
				});
			}
		};
		add(btnSearch);

		optType = new OptionSlider("排行榜类型", "指定玩家", "所有", 0, 1) {
			@Override
			protected void onChange() {
				if (getSelectedValue() == 0) {
					NetRankingsScene.playerName = Net.name;
				} else {
					NetRankingsScene.playerName = null;
				}
				btnSearch.text((NetRankingsScene.playerName == null ? "查看所有" : "查看: " + NetRankingsScene.playerName));
			}
		};
		optType.setSelectedValue(NetRankingsScene.playerName == null ? 1 : 0);
		add(optType);

		sep2 = new ColorBlock(1, 1, 0xFF000000);
		add(sep2);

		optChallenge = new OptionSlider("挑战数量", "不筛选", "9挑", -1, 9) {
			@Override
			protected void onChange() {
				if (getSelectedValue() == -1) {
					NetRankingsScene.challengeCount = null;
				} else {
					NetRankingsScene.challengeCount = getSelectedValue();
				}
			}
		};
		optChallenge.setSelectedValue(NetRankingsScene.challengeCount == null ? -1 : NetRankingsScene.challengeCount);
		add(optChallenge);

		optWinOnly = new OptionSlider("只显示胜利", "不筛选", "是", -1, 1) {
			@Override
			protected void onChange() {
				if (getSelectedValue() == -1) {
					NetRankingsScene.winOnly = null;
				} else {
					NetRankingsScene.winOnly = getSelectedValue() == 1;
				}
			}
		};
		optWinOnly.setSelectedValue(NetRankingsScene.winOnly == null ? -1 : NetRankingsScene.winOnly ? 1 : 0);
		add(optWinOnly);

		optMode = new OptionSlider("游戏模式", "不筛选", "每日", -1, 2) {
			@Override
			protected void onChange() {
				if (getSelectedValue() == -1) {
					NetRankingsScene.gameMode = null;
				} else {
					NetRankingsScene.gameMode = String.valueOf(Mode.values()[getSelectedValue()]);
				}
			}
		};
		if (NetRankingsScene.gameMode == null) {
			optMode.setSelectedValue(-1);
		} else {
			Mode mode = Mode.valueOf(NetRankingsScene.gameMode);
			optMode.setSelectedValue(mode.ordinal());
		}
		add(optMode);

		sep3 = new ColorBlock(1, 1, 0xFF000000);
		add(sep3);

		optSortCriteria = new OptionSlider("排序方式", "最近通关", "通关时间", 0, 2) {
			@Override
			protected void onChange() {
				switch (getSelectedValue()) {
					case 0:
						NetRankingsScene.sortCriteria = "id";
						break;
					case 1:
						NetRankingsScene.sortCriteria = "score";
						break;
					case 2:
						NetRankingsScene.sortCriteria = "duration";
						break;
				}
			}
		};
		if (NetRankingsScene.sortCriteria == null) {
			optSortCriteria.setSelectedValue(0);
		} else {
			switch (NetRankingsScene.sortCriteria) {
				case "id":
					optSortCriteria.setSelectedValue(0);
					break;
				case "score":
					optSortCriteria.setSelectedValue(1);
					break;
				case "duration":
					optSortCriteria.setSelectedValue(2);
					break;
			}
		}
		add(optSortCriteria);

		layout();
	}

	private void layout() {
		title.setRect((width - title.width()) / 2, 2, width, 10);
		sep1.size(width, 1);
		sep1.y = title.bottom() + 5;
		btnSearch.setRect(0, sep1.y + 2, width, 20);
		optType.setRect(0, btnSearch.bottom() + 2, width, 20);
		sep2.size(width, 1);
		sep2.y = optType.bottom() + 2;
		optChallenge.setRect(0, sep2.y + 2, width, 20);
		optWinOnly.setRect(0, optChallenge.bottom() + 2, width, 20);
		optMode.setRect(0, optWinOnly.bottom() + 2, width, 20);
		sep3.size(width, 1);
		sep3.y = optMode.bottom() + 2;
		optSortCriteria.setRect(0, sep3.y + 2, width, 20);
	}

	@Override
	public void hide() {
		super.hide();
		Sender.sendRequestLeaderboard(new CRequestLeaderboard(
				NetRankingsScene.playerName,
				NetRankingsScene.challengeCount,
				NetRankingsScene.winOnly,
				NetRankingsScene.gameMode,
				NetRankingsScene.sortCriteria,
				1, 10));
	}
}


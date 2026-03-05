package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.scene.NetRankingsScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Sender;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CRequestLeaderboard;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.ColorBlock;

public class NetWndLeaderboardSelect extends Window {
	private static final int WIDTH = 130;
	private static final int GAP = 2;
	private static final int BTN_HEIGHT = 18;

	RenderedTextBlock title;
	ColorBlock sep1;
	RedButton btnPlayerType;
	RedButton btnPlayerName;
	ColorBlock sep2;
	RedButton btnChallenge;
	CheckBox chkWinOnly;
	RedButton btnGameMode;
	ColorBlock sep3;
	CheckBox chkBannedOnly;
	RedButton btnSortCriteria;

	public NetWndLeaderboardSelect() {
		int currentHeight = 0;

		title = PixelScene.renderTextBlock("排行榜设置", 9);
		title.hardlight(TITLE_COLOR);
		add(title);
		currentHeight += 12;

		sep1 = new ColorBlock(WIDTH, 1, 0xFF000000);
		add(sep1);
		currentHeight += 3;

		btnPlayerType = new RedButton(getPlayerTypeText()) {
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new WndOptions("排行榜类型", "选择要查看的排行榜类型", "我的记录", "所有玩家") {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							NetRankingsScene.playerName = Net.name;
						} else {
							NetRankingsScene.playerName = null;
						}
						btnPlayerType.text(getPlayerTypeText());
						updatePlayerNameButton();
					}
				});
			}
		};
		add(btnPlayerType);
		currentHeight += BTN_HEIGHT + GAP;

		btnPlayerName = new RedButton(getPlayerNameText()) {
			@Override
			protected void onClick() {
				if (NetRankingsScene.playerName != null) {
					return;
				}
				ShatteredPixelDungeon.scene().addToFront(new WndTextInput("输入玩家名", null, NetRankingsScene.playerName, 30, false, "确定", "取消") {
					@Override
					public void onSelect(boolean positive, String text) {
						if (positive && text != null && !text.trim().isEmpty()) {
							NetRankingsScene.playerName = text.trim();
							btnPlayerName.text(getPlayerNameText());
						}
					}
				});
			}
		};
		btnPlayerName.enable(NetRankingsScene.playerName == null);
		add(btnPlayerName);
		currentHeight += BTN_HEIGHT + GAP;

		sep2 = new ColorBlock(WIDTH, 1, 0xFF000000);
		add(sep2);
		currentHeight += 3;

		btnChallenge = new RedButton(getChallengeText()) {
			@Override
			protected void onClick() {
				String[] options = new String[11];
				options[0] = "不筛选";
				for (int i = 0; i <= 9; i++) {
					options[i + 1] = i + "挑战";
				}
				ShatteredPixelDungeon.scene().addToFront(new WndOptions("挑战数量", "选择挑战数量筛选条件", options) {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							NetRankingsScene.challengeCount = null;
						} else {
							NetRankingsScene.challengeCount = index - 1;
						}
						btnChallenge.text(getChallengeText());
					}
				});
			}
		};
		add(btnChallenge);
		currentHeight += BTN_HEIGHT + GAP;

		chkWinOnly = new CheckBox("只显示胜利") {
			@Override
			protected void onClick() {
				super.onClick();
				NetRankingsScene.winOnly = checked() ? true : null;
			}
		};
		chkWinOnly.checked(NetRankingsScene.winOnly != null && NetRankingsScene.winOnly);
		add(chkWinOnly);
		currentHeight += BTN_HEIGHT + GAP;

		btnGameMode = new RedButton(getGameModeText()) {
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new WndOptions("游戏模式", "选择游戏模式筛选条件", "不筛选", "标准模式", "每日挑战") {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							NetRankingsScene.gameMode = null;
						} else {
							NetRankingsScene.gameMode = String.valueOf(Mode.values()[index - 1]);
						}
						btnGameMode.text(getGameModeText());
					}
				});
			}
		};
		add(btnGameMode);
		currentHeight += BTN_HEIGHT + GAP;

		// SPDNet: 添加只显示被封禁玩家选项
		chkBannedOnly = new CheckBox("只显示被封禁玩家") {
			@Override
			protected void onClick() {
				super.onClick();
				NetRankingsScene.bannedOnly = checked() ? true : null;
			}
		};
		chkBannedOnly.checked(NetRankingsScene.bannedOnly != null && NetRankingsScene.bannedOnly);
		add(chkBannedOnly);
		currentHeight += BTN_HEIGHT + GAP;

		sep3 = new ColorBlock(WIDTH, 1, 0xFF000000);
		add(sep3);
		currentHeight += 3;

		btnSortCriteria = new RedButton(getSortCriteriaText()) {
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new WndOptions("排序方式", "选择排序方式", "最近通关", "分数最高", "通关时间最短") {
					@Override
					protected void onSelect(int index) {
						switch (index) {
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
						btnSortCriteria.text(getSortCriteriaText());
					}
				});
			}
		};
		add(btnSortCriteria);
		currentHeight += BTN_HEIGHT;

		resize(WIDTH, currentHeight);
		layout();
	}

	private String getPlayerTypeText() {
		return NetRankingsScene.playerName == null ? "排行榜: 所有玩家" : "排行榜: 我的记录";
	}

	private String getPlayerNameText() {
		if (NetRankingsScene.playerName == null) {
			return "搜索玩家: 点击输入";
		} else if (NetRankingsScene.playerName.equals(Net.name)) {
			return "当前: 我 (" + Net.name + ")";
		} else {
			return "当前: " + NetRankingsScene.playerName;
		}
	}

	private void updatePlayerNameButton() {
		btnPlayerName.enable(NetRankingsScene.playerName == null);
		btnPlayerName.text(getPlayerNameText());
	}

	private String getChallengeText() {
		if (NetRankingsScene.challengeCount == null) {
			return "挑战数量: 不筛选";
		} else {
			return "挑战数量: " + NetRankingsScene.challengeCount + "挑战";
		}
	}

	private String getGameModeText() {
		if (NetRankingsScene.gameMode == null) {
			return "游戏模式: 不筛选";
		} else {
			Mode mode = Mode.valueOf(NetRankingsScene.gameMode);
			return "游戏模式: " + mode.getName();
		}
	}

	private String getSortCriteriaText() {
		if (NetRankingsScene.sortCriteria == null || NetRankingsScene.sortCriteria.equals("id")) {
			return "排序: 最近通关";
		} else if (NetRankingsScene.sortCriteria.equals("score")) {
			return "排序: 分数最高";
		} else {
			return "排序: 通关时间最短";
		}
	}

	private void layout() {
		float y = 0;

		title.setPos((width - title.width()) / 2, y);
		y = title.bottom() + 3;

		sep1.y = y;
		y += 3;

		btnPlayerType.setRect(0, y, width, BTN_HEIGHT);
		y = btnPlayerType.bottom() + GAP;

		btnPlayerName.setRect(0, y, width, BTN_HEIGHT);
		y = btnPlayerName.bottom() + GAP;

		sep2.y = y;
		y += 3;

		btnChallenge.setRect(0, y, width, BTN_HEIGHT);
		y = btnChallenge.bottom() + GAP;

		chkWinOnly.setRect(0, y, width, BTN_HEIGHT);
		y = chkWinOnly.bottom() + GAP;

		btnGameMode.setRect(0, y, width, BTN_HEIGHT);
		y = btnGameMode.bottom() + GAP;

		// SPDNet: 只显示被封禁玩家复选框位置
		chkBannedOnly.setRect(0, y, width, BTN_HEIGHT);
		y = chkBannedOnly.bottom() + GAP;

		sep3.y = y;
		y += 3;

		btnSortCriteria.setRect(0, y, width, BTN_HEIGHT);
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
				1, 10, NetRankingsScene.bannedOnly));
	}
}

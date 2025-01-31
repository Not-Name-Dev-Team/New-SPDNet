package com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.scene;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.GameRecord;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Sender;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CRequestLeaderboard;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.windows.NetWndLeaderboardSelect;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDailies;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndVictoryCongrats;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.GameMath;

import java.util.ArrayList;

//RankingsScene
public class NetRankingsScene extends PixelScene {

	private static final float ROW_HEIGHT_MAX = 20;
	private static final float ROW_HEIGHT_MIN = 12;

	private static final float MAX_ROW_WIDTH = 160;

	private static final float GAP = 4;

	private Archs archs;
	public static String playerName;
	public static Integer challengeCount;
	public static Boolean winOnly;
	public static String gameMode;
	public static String sortCriteria;
	private int amountPerPage;
	private int totalPages;
	private int currentPage;
	private int totalElements;
	private ArrayList<GameRecord> records = new ArrayList<>();
	private Group rows;
	private RenderedTextBlock noRec;

	IconTitle title;
	RenderedTextBlock select;
	RenderedTextBlock label;

	@Override
	public void create() {

		super.create();

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		uiCamera.visible = false;

		Sender.sendRequestLeaderboard(new CRequestLeaderboard(playerName, challengeCount, winOnly, gameMode, sortCriteria, 1, 10));

		int w = Camera.main.width;
		int h = Camera.main.height;

		archs = new Archs();
		archs.setSize(w, h);
		add(archs);

		Rankings.INSTANCE.load();

		title = new IconTitle( Icons.RANKINGS.get(), "当前显示: 总排行榜");
		title.setSize(200, 0);
		title.setPos(
				(w - title.reqWidth()) / 2f,
				(20 - title.height()) / 2f
		);
		align(title);
		add(title);

		select = PixelScene.renderTextBlock("当前筛选: 无", 9);
		select.hardlight(Window.TITLE_COLOR);
		select.setPos(
				(w - select.width()) / 2f,
				title.bottom() + 4
		);
		align(select);
		add(select);

		label = PixelScene.renderTextBlock("显示第" + currentPage + "页 共有" + totalPages + "页 共有" + totalElements + "条记录", 8);
		label.hardlight(0xCCCCCC);
		label.setHightlighting(true, Window.SHPX_COLOR);
		label.setPos(
				(w - label.width()) / 2,
				h - label.height() - 2 * GAP
		);
		align(label);
		add(label);

		updateLayout();

		IconButton btn = new IconButton(Icons.RANKINGS.get()) {
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new NetWndLeaderboardSelect());
			}
		};
		btn.setRect(0, 0, 20, 20);
		add(btn);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos(Camera.main.width - btnExit.width(), 0);
		add(btnExit);

		int left = 0;

		if (Rankings.INSTANCE.latestDaily != null) {
			IconButton btnDailies = new IconButton(Icons.CALENDAR.get()) {
				@Override
				protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront(new WndDailies());
				}

				@Override
				protected void onPointerUp() {
					icon.hardlight(0.5f, 1f, 2f);
				}
			};
			btnDailies.icon().hardlight(0.5f, 1f, 2f);
			btnDailies.setRect( left, 0, 16, 20 );
			left += 16;
			add(btnDailies);
		}

		if (Dungeon.daily){
			addToFront(new WndDailies());
		} else if (Badges.isUnlocked(Badges.Badge.VICTORY) && !SPDSettings.victoryNagged()) {
			SPDSettings.victoryNagged(true);
			add(new WndVictoryCongrats());
		}

		fadeIn();
	}

	@Override
	public void destroy() {
		super.destroy();
		//so that opening daily records does not trigger WndDailies opening on future visits
		Dungeon.daily = Dungeon.dailyReplay = false;
	}

	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchNoFade(TitleScene.class);
	}

	public void setRankings(int totalPages, int currentPage, int totalElements, ArrayList<GameRecord> records) {
		this.totalPages = totalPages;
		this.currentPage = currentPage;
		this.totalElements = totalElements;
		this.records = records;
		ShatteredPixelDungeon.runOnRenderThread(this::updateLayout);
	}

	public void updateLayout() {
		int w = Camera.main.width;
		int h = Camera.main.height;

		if (playerName == null) {
			title.label("当前显示: 总排行榜");
		} else {
			title.label("当前显示: " + playerName + " 的排行榜");
		}
		title.setPos(
				(w - title.width()) / 2f,
				(20 - title.height()) / 2f
		);
		ArrayList<String> selects = new ArrayList<>();
		if (!(challengeCount == null)) {
			selects.add(challengeCount + "挑");
		}
		if (!(winOnly == null)) {
			selects.add(winOnly ? "胜利" : "未胜利");
		}
		if (!(gameMode == null)) {
			Mode mode = Mode.valueOf(gameMode);
			selects.add(mode.getName());
		}
		String selectText = "";
		if (selects.isEmpty()) {
			selectText = "无";
		} else {
			for (String text : selects) {
				selectText = selectText + text + ", ";
			}
			selectText = selectText.substring(0, selectText.length() - 2);
		}
		select.text("当前筛选: " + selectText);
		select.setPos(
				(w - select.width()) / 2f,
				title.bottom() + 4
		);

		if (rows != null) {
			rows.destroy();
		}
		if (noRec != null) {
			noRec.destroy();
		}
		if (!records.isEmpty()) {
			rows = new Group();
			add(rows);

			//attempts to give each record as much space as possible, ideally as much space as portrait mode
			float rowHeight = GameMath.gate(ROW_HEIGHT_MIN, (uiCamera.height - 26) / records.size(), ROW_HEIGHT_MAX);

			float left = (w - Math.min(MAX_ROW_WIDTH, w)) / 2 + GAP;
			float top = (h - rowHeight * records.size()) / 2;

			int pos = 0;

			for (GameRecord rec : records) {
				NetRecordButton row = new NetRecordButton(pos, pos == Rankings.INSTANCE.lastRecord, rec);
				float offset = 0;
				if (rowHeight <= 14) {
					offset = (pos % 2 == 1) ? 5 : -5;
				}
				row.setRect(left + offset, top + pos * rowHeight, w - left * 2, rowHeight);
				rows.add(row);

				pos++;
			}

			if (Rankings.INSTANCE.totalNumber >= Rankings.TABLE_SIZE) {
				label.text("显示第" + currentPage + "页 共有" + totalPages + "页 共有" + totalElements + "条记录");
			}
		} else {
			noRec = PixelScene.renderTextBlock("没找到记录", 8);
			noRec.hardlight(0xCCCCCC);
			noRec.setPos(
					(w - noRec.width()) / 2,
					(h - noRec.height()) / 2
			);
			align(noRec);
			add(noRec);
		}
	}
}

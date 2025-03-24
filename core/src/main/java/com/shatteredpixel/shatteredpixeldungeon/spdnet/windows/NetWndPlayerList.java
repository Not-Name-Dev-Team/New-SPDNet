package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.NetIcons;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Sender;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Player;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CViewHero;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.Map;
import java.util.Set;

public class NetWndPlayerList extends NetWindow {
	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	private static final int HEIGHT = 120;

	private static final int VGAP = 32;
	private static final int HGAP = 3;

	public static class Power {
		public static final String ADMIN = "admin";
		public static final String NORMAL = "normal";
		public static final String BOT = "bot";
	}

	public NetWndPlayerList() {
		super(PixelScene.landscape() ? WIDTH_L : WIDTH_P, HEIGHT);

		float y = 2;

		RenderedTextBlock titleLbl = PixelScene.renderTextBlock("在线玩家", 7);
		add(titleLbl);
		titleLbl.setPos(VGAP / 2, y + 2);

		Image icon = NetIcons.get(NetIcons.NEWS);
		icon.scale.set(0.8f);
		IconButton chatBtn = new IconButton(icon) {
			@Override
			protected void onClick() {
				super.onClick();
				NetWindow.dev("正在开发，敬请期待");
			}
		};
		add(chatBtn);
		chatBtn.setSize(10, 10);
		chatBtn.setPos(width - chatBtn.width() - (10), y);

		ColorBlock sep = new ColorBlock(1, 1, 0xFF000000);
		sep.size(width - (VGAP / 2), 1);
		sep.x = VGAP / 2;
		sep.y = chatBtn.bottom() + 2;
		add(sep);

		y += sep.y + HGAP;

		ScrollPane list = new ScrollPane(new Component());
		add(list);

		Component content = list.content();
		content.clear();

		list.scrollTo(0, 0);

		float ypos = 0;

		Player[] players = Net.playerList.values().toArray(new Player[0]);
		for (int i = 0; i < Net.playerList.size(); i++) {
			float xpos = VGAP;

			Player player = players[i];

			PlayerEntry playerRank = new PlayerEntry(player, i + 1) {
				@Override
				protected void onClick() {
					if (player.getStatus() != null) {
						Sender.sendViewHero(new CViewHero(player.getName()));
					}
				}
			};
			playerRank.setSize(width, 12);
			playerRank.setPos(xpos, ypos);

			content.add(playerRank);

			ypos = playerRank.bottom();

		}

		content.setRect(0, list.top(), width, ypos);
		list.setRect(0, y, width, HEIGHT - 20);

		y += list.height();

		resize(width, (int) y);
	}

	public static class PlayerEntry extends Button {
		private int order;
		private RenderedTextBlock nick;

		private boolean enabled;


		public PlayerEntry(Player player, int order) {
			this.order = order;
			this.enabled = player.getStatus() != null;

			int color = getRoleColor(player.getPower());
			nick = PixelScene.renderTextBlock(player.getName(), 8);
			nick.hardlight(color);
			add(nick);
		}

		@Override
		protected void layout() {
			super.layout();
			nick.setPos(VGAP, y + 2);
			nick.alpha(enabled ? 1.0f : 0.3f);
		}

	}

	public static HeroClass playerClassToHeroClass(int playerClass) {
		switch (playerClass) {
			case 0:
			default:
				return HeroClass.WARRIOR;
			case 1:
				return HeroClass.MAGE;
			case 2:
				return HeroClass.ROGUE;
			case 3:
				return HeroClass.HUNTRESS;
		}
	}

	public static int getRoleColor(String power) {
		switch (power) {
			case Power.BOT:
				return 0xFFFF00;
			case Power.ADMIN:
				return 0x00FF00;
			case Power.NORMAL:
				return 0xFFFFFF;
		}
		return 0xFFFFFF;
	}

	protected boolean enabled(int index) {
		return true;
	}

	protected void onSelect(int index) {
	}


}

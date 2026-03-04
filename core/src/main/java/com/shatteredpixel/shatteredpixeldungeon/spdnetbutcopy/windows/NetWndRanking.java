package com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.GameRecord;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBadge;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndRanking;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndScoreBreakdown;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTabbed;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundle;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * SPDNet: WndRanking 的复制版本，用于显示网络游戏记录
 * 原类: WndRanking
 * 
 * 与原类的差异（用于上游合并追踪）:
 * ================================================
 * - 使用 GameRecord 替代 Rankings.Record 以支持网络数据
 * - 从在线排行榜获取游戏记录并显示
 * - 第183行: RankingTab.select() 方法覆盖
 * 
 * 用途: 在网络排行榜中点击记录后显示详细信息。
 */
public class NetWndRanking extends WndTabbed {

	private static final int WIDTH			= 115;
	private static final int HEIGHT			= 144;

	private static NetWndRanking INSTANCE;

	private GameRecord record;

	public NetWndRanking( final GameRecord rec ) {

		super();
		resize( WIDTH, HEIGHT );

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;

		this.record = rec;

		try {
			Badges.loadGlobal();
			//	Rankings.INSTANCE.loadGameData( rec );
			loadGameData();
			createControls();
		} catch ( Exception e ) {
			Game.reportException( new RuntimeException("Rankings Display Failed!",e));
			Dungeon.hero = null;
			createControls();
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}

	private void createControls() {

		if (Dungeon.hero != null) {
			Icons[] icons =
					{Icons.RANKINGS, Icons.TALENT, Icons.BACKPACK_LRG, Icons.BADGES, Icons.CHALLENGE_COLOR};
			Group[] pages =
					{new StatsTab(), new TalentsTab(), new ItemsTab(), new BadgesTab(), null};

			if (Dungeon.challenges != 0) pages[4] = new ChallengesTab();

			for (int i = 0; i < pages.length; i++) {

				if (pages[i] == null) {
					break;
				}

				add(pages[i]);

				Tab tab = new RankingTab(icons[i], pages[i]);
				add(tab);
			}

			layoutTabs();

			select(0);
		} else {
			StatsTab tab = new StatsTab();
			add(tab);

		}
	}

	public void loadGameData() {
		Actor.clear();
		Dungeon.hero = null;
		Dungeon.level = null;
		Generator.fullReset();
		Notes.reset();
		Dungeon.quickslot.reset();
		com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton.reset();
		Toolbar.swappedQuickslots = false;

		Bundle handler = Bundle.fromString(record.getHandlers());
		Scroll.restore(handler);
		Potion.restore(handler);
		Ring.restore(handler);

		// SPDNet: 不加载其他玩家的本地成就数据
		// 原代码: Badges.loadLocal(Bundle.fromString(record.getBadges()));
		// 原因: 加载其他玩家的成就会覆盖 local 变量，如果此时触发 validateXxx() 方法，
		// 会调用 unlock() 将成就发送到服务器，导致错误解锁成就并存入数据库

		Dungeon.hero = record.getHero();
		Dungeon.hero.belongings.identify();

		Statistics.restoreFromGameRecord(record);

		Dungeon.challenges = record.getChallenges();

		Dungeon.initialVersion = record.getGameVersion();

		if (Dungeon.initialVersion <= ShatteredPixelDungeon.v1_2_3) {
			Statistics.gameWon = record.isWin();
		}

		Dungeon.seed = record.getSeed();
		Dungeon.customSeedText = record.getCustomSeed();
		Dungeon.daily = record.isDaily();
		Dungeon.dailyReplay = record.isDailyReplay();
	}

	private class RankingTab extends IconTab {

		private Group page;

		public RankingTab( Icons icon, Group page ) {
			super( Icons.get(icon) );
			this.page = page;
		}

		@Override
		protected void select( boolean value ) {
			super.select( value );
			if (page != null) {
				page.visible = page.active = selected;
			}
		}
	}

	private class StatsTab extends Group {

		private int GAP	= 4;

		public StatsTab() {
			super();

			String heroClass = record.getHeroClass();
			if (Dungeon.hero != null){
				heroClass = Dungeon.hero.className();
			}

			IconTitle title = new IconTitle();
			title.icon( HeroSprite.avatar( HeroClass.valueOf(record.getHeroClass()), record.getTier()) );
			title.label( Messages.get(WndRanking.StatsTab.class, "title", record.getLevel(), heroClass).toUpperCase(Locale.ENGLISH));
			title.color(Window.TITLE_COLOR);
			title.setRect( 0, 0, WIDTH, 0 );
			add( title );

			if (Dungeon.hero != null && Dungeon.seed != -1){
				GAP--;
			}

			float pos = title.bottom() + 1;

			RenderedTextBlock date = PixelScene.renderTextBlock(record.getDate(), 7);
			date.hardlight(0xCCCCCC);
			date.setPos(0, pos);
			add(date);

			RenderedTextBlock version = PixelScene.renderTextBlock(record.getVersion(), 7);
			version.hardlight(0xCCCCCC);
			version.setPos(WIDTH-version.width(), pos);
			add(version);

			pos = date.bottom()+5;

			NumberFormat num = NumberFormat.getInstance(Messages.locale());

			if (Dungeon.hero == null){
				pos = statSlot( this, Messages.get(WndRanking.StatsTab.class, "score"), num.format(record.getScore()), pos );
				pos += GAP;

				Image errorIcon = Icons.WARNING.get();
				errorIcon.y = pos;
				add(errorIcon);

				RenderedTextBlock errorText = PixelScene.renderTextBlock(Messages.get(NetWndRanking.StatsTab.class, "error"), 6);
				errorText.maxWidth((int)(WIDTH-errorIcon.width()-GAP));
				errorText.setPos(errorIcon.width()+GAP, pos + (errorIcon.height()-errorText.height())/2);
				add(errorText);

			} else {

				pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "score"), num.format(Statistics.totalScore), pos);

				IconButton scoreInfo = new IconButton(Icons.get(Icons.INFO)) {
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().addToFront(new WndScoreBreakdown());
					}
				};
				scoreInfo.setSize(16, 16);
				scoreInfo.setPos(WIDTH - scoreInfo.width(), pos - 10 - GAP);
				add(scoreInfo);

				pos += GAP;

				int strBonus = Dungeon.hero.STR() - Dungeon.hero.STR;
				if (strBonus > 0)
					pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "str"), Dungeon.hero.STR + " + " + strBonus, pos);
				else if (strBonus < 0)
					pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "str"), Dungeon.hero.STR + " - " + -strBonus, pos);
				else
					pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "str"), Integer.toString(Dungeon.hero.STR), pos);
				pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "duration"), num.format((int) Statistics.duration), pos);
				if (Statistics.highestAscent == 0) {
					pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "depth"), num.format(Statistics.deepestFloor), pos);
				} else {
					pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "ascent"), num.format(Statistics.highestAscent), pos);
				}
				if (Dungeon.seed != -1) {
					if (Dungeon.daily) {
						if (Dungeon.dailyReplay) {
							pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "replay_for"), "_" + Dungeon.customSeedText + "_", pos);
						} else {
							pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "daily_for"), "_" + Dungeon.customSeedText + "_", pos);
						}
					} else if (!Dungeon.customSeedText.isEmpty()) {
						pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "custom_seed"), "_" + Dungeon.customSeedText + "_", pos);
					} else {
						pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "seed"), DungeonSeed.convertToCode(Dungeon.seed), pos);
					}
				} else {
					pos += GAP + 5;
				}

				pos += GAP;

				pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "enemies"), num.format(Statistics.enemiesSlain), pos);
				pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "gold"), num.format(Statistics.goldCollected), pos);
				pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "food"), num.format(Statistics.foodEaten), pos);
				pos = statSlot(this, Messages.get(WndRanking.StatsTab.class, "alchemy"), num.format(Statistics.itemsCrafted), pos);
			}

			int buttontop = HEIGHT - 16;

//			// 手动设置排行榜中的种子相关信息可见
//			if (Dungeon.hero != null && Dungeon.seed != -1 && !Dungeon.daily &&
//					(true || Badges.isUnlocked(Badges.Badge.VICTORY))){
//				final Image icon = Icons.get(Icons.SEED);
//				RedButton btnSeed = new RedButton(Messages.get(this, "copy_seed")){
//					@Override
//					protected void onClick() {
//						super.onClick();
//						ShatteredPixelDungeon.scene().addToFront(new WndOptions(new Image(icon),
//								Messages.get(WndRanking.StatsTab.this, "copy_seed"),
//								Messages.get(WndRanking.StatsTab.this, "copy_seed_desc"),
//								Messages.get(WndRanking.StatsTab.this, "copy_seed_copy"),
//								Messages.get(WndRanking.StatsTab.this, "copy_seed_cancel")){
//							@Override
//							protected void onSelect(int index) {
//								super.onSelect(index);
//								if (index == 0){
//									SPDSettings.customSeed(DungeonSeed.convertToCode(Dungeon.seed));
//									icon.hardlight(1f, 1.5f, 0.67f);
//								}
//							}
//						});
//					}
//				};
//				if (DungeonSeed.convertFromText(SPDSettings.customSeed()) == Dungeon.seed){
//					icon.hardlight(1f, 1.5f, 0.67f);
//				}
//				btnSeed.icon(icon);
//				btnSeed.setRect(0, buttontop, 115, 16);
//				add(btnSeed);
//			}

		}

		private float statSlot( Group parent, String label, String value, float pos ) {

			int size = 7;
			RenderedTextBlock txt;
			do {
				txt = PixelScene.renderTextBlock( label, size );
				size--;
			} while (txt.width() >= WIDTH * 0.55f);
			txt.setPos(0, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			parent.add( txt );

			size = 7;
			do {
				txt = PixelScene.renderTextBlock( value, size );
				size--;
			} while (txt.width() >= WIDTH * 0.45f);
			txt.setPos(WIDTH * 0.55f, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			parent.add( txt );

			return pos + GAP + txt.height();
		}
	}

	private class TalentsTab extends Group{

		public TalentsTab(){
			super();

			camera = NetWndRanking.this.camera;

			int tiers = 1;
			if (Dungeon.hero.lvl >= 6) tiers++;
			if (Dungeon.hero.lvl >= 12 && Dungeon.hero.subClass != HeroSubClass.NONE) tiers++;
			if (Dungeon.hero.lvl >= 20 && Dungeon.hero.armorAbility != null) tiers++;
			while (Dungeon.hero.talents.size() > tiers){
				Dungeon.hero.talents.remove(Dungeon.hero.talents.size()-1);
			}

			TalentsPane p = new TalentsPane(TalentButton.Mode.INFO);
			add(p);
			p.setPos(0, 0);
			p.setSize(WIDTH, HEIGHT);
			p.setPos(0, 0);

		}

	}

	private class ItemsTab extends Group {

		private float pos;

		public ItemsTab() {
			super();

			Belongings stuff = Dungeon.hero.belongings;
			if (stuff.weapon != null) {
				addItem( stuff.weapon );
			}
			if (stuff.armor != null) {
				addItem( stuff.armor );
			}
			if (stuff.artifact != null) {
				addItem( stuff.artifact );
			}
			if (stuff.misc != null) {
				addItem( stuff.misc );
			}
			if (stuff.ring != null) {
				addItem( stuff.ring );
			}

			pos = 0;

			int slotsActive = 0;
			for (int i = 0; i < QuickSlot.SIZE; i++){
				if (Dungeon.quickslot.isNonePlaceholder(i)){
					slotsActive++;
				}
			}

			Trinket trinket = stuff.getItem(Trinket.class);
			if (trinket != null){
				slotsActive++;
			}

			float slotWidth = Math.min(28, ((WIDTH - slotsActive + 1) / (float)slotsActive));

			for (int i = -1; i < QuickSlot.SIZE; i++){
				Item item = null;
				if (i == -1){
					item = trinket;
				} else if (Dungeon.quickslot.isNonePlaceholder(i)) {
					item = Dungeon.quickslot.getItem(i);
				}
				if (item != null){
					QuickSlotButton slot = new QuickSlotButton(item);

					slot.setRect( pos, 120, slotWidth, 23 );
					PixelScene.align(slot);

					add(slot);

					pos += slotWidth + 1;

				}
			}
		}

		private void addItem( Item item ) {
			ItemButton slot = new ItemButton( item );
			slot.setRect( 0, pos, width, ItemButton.HEIGHT );
			add( slot );

			pos += slot.height() + 1;
		}
	}

	private class BadgesTab extends Group {

		public BadgesTab() {
			super();

			camera = NetWndRanking.this.camera;

			// SPDNet: 从 record 中解析成就数据，不修改 Badges.local
			HashSet<Badges.Badge> recordBadges = Badges.restore(Bundle.fromString(record.getBadges()));
			List<Badges.Badge> filteredBadges = filterLocalBadges(recordBadges);

			Component badges;
			if (filteredBadges.size() <= 8){
				badges = new NetBadgesList(filteredBadges);
			} else {
				badges = new NetBadgesGrid(filteredBadges);
			}
			add(badges);
			badges.setSize( WIDTH, HEIGHT );
		}

		// SPDNet: 过滤本地成就（排除 HIDDEN 类型）
		private List<Badges.Badge> filterLocalBadges(HashSet<Badges.Badge> badges) {
			List<Badges.Badge> result = new ArrayList<>();
			for (Badges.Badge badge : badges) {
				if (badge.type != Badges.BadgeType.HIDDEN) {
					result.add(badge);
				}
			}
			// 应用徽章替换规则（只显示最高级）
			return Badges.filterReplacedBadges(result);
		}
	}

	// SPDNet: 用于显示其他玩家成就的列表组件
	private class NetBadgesList extends ScrollPane {

		private ArrayList<ListItem> items = new ArrayList<>();

		public NetBadgesList(List<Badges.Badge> badges) {
			super(new Component());

			for (Badges.Badge badge : badges) {
				ListItem item = new ListItem(badge);
				content.add(item);
				items.add(item);
			}
		}

		@Override
		protected void layout() {
			float pos = 0;
			for (ListItem item : items) {
				item.setRect(0, pos, width, ListItem.HEIGHT);
				pos += ListItem.HEIGHT;
			}
			content.setSize(width, pos);
			super.layout();
		}

		private class ListItem extends Component {
			private static final float HEIGHT = 18;
			private Badges.Badge badge;
			private Image icon;
			private RenderedTextBlock label;

			public ListItem(Badges.Badge badge) {
				super();
				this.badge = badge;
				icon.copy(BadgeBanner.image(badge.image));
				label.text(badge.title());
			}

			@Override
			protected void createChildren() {
				icon = new Image();
				add(icon);
				label = PixelScene.renderTextBlock(6);
				add(label);
			}

			@Override
			protected void layout() {
				icon.x = x;
				icon.y = y + (height - icon.height) / 2;
				PixelScene.align(icon);
				label.setPos(icon.x + icon.width + 2, y + (height - label.height()) / 2);
				PixelScene.align(label);
			}
		}
	}

	// SPDNet: 用于显示其他玩家成就的网格组件
	private class NetBadgesGrid extends Component {
		ArrayList<BadgeButton> badgeButtons;

		public NetBadgesGrid(List<Badges.Badge> badges) {
			super();
			badgeButtons = new ArrayList<>();

			for (Badges.Badge badge : badges) {
				BadgeButton button = new BadgeButton(badge, true);
				add(button);
				badgeButtons.add(button);
			}
		}

		@Override
		protected void layout() {
			super.layout();
			int columns = (int) Math.max(1, Math.sqrt(badgeButtons.size()));
			float buttonSize = Math.min(width / columns, 20);
			float spacing = (width - buttonSize * columns) / (columns + 1);

			int row = 0, col = 0;
			for (BadgeButton button : badgeButtons) {
				button.setRect(x + spacing + col * (buttonSize + spacing), y + spacing + row * (buttonSize + spacing), buttonSize, buttonSize);
				col++;
				if (col >= columns) {
					col = 0;
					row++;
				}
			}
		}
	}

	// SPDNet: 成就按钮组件
	private class BadgeButton extends Button {
		private Badges.Badge badge;
		private boolean unlocked;
		private Image icon;

		public BadgeButton(Badges.Badge badge, boolean unlocked) {
			super();
			this.badge = badge;
			this.unlocked = unlocked;
			icon.copy(BadgeBanner.image(badge.image));
			if (!unlocked) {
				icon.brightness(0.2f);
			}
		}

		@Override
		protected void createChildren() {
			icon = new Image();
			add(icon);
			super.createChildren();
		}

		@Override
		protected void layout() {
			super.layout();
			icon.x = x + (width - icon.width) / 2;
			icon.y = y + (height - icon.height) / 2;
			PixelScene.align(icon);
		}

		@Override
		protected void onClick() {
			Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
			Game.scene().addToFront(new WndBadge(badge, true));
		}
	}

	private class ChallengesTab extends Group{

		public ChallengesTab(){
			super();

			camera = NetWndRanking.this.camera;

			float pos = 0;

			for (int i=0; i < Challenges.NAME_IDS.length; i++) {

				final String challenge = Challenges.NAME_IDS[i];

				CheckBox cb = new CheckBox( Messages.titleCase(Messages.get(Challenges.class, challenge)) );
				cb.checked( (Dungeon.challenges & Challenges.MASKS[i]) != 0 );
				cb.active = false;

				if (i > 0) {
					pos += 1;
				}
				cb.setRect( 0, pos, WIDTH-16, 15 );

				add( cb );

				IconButton info = new IconButton(Icons.get(Icons.INFO)){
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().add(
								new WndMessage(Messages.get(Challenges.class, challenge+"_desc"))
						);
					}
				};
				info.setRect(cb.right(), pos, 16, 15);
				add(info);

				pos = cb.bottom();
			}
		}

	}

	private class ItemButton extends Button {

		public static final int HEIGHT	= 23;

		private Item item;

		private ItemSlot slot;
		private ColorBlock bg;
		private RenderedTextBlock name;

		public ItemButton( Item item ) {

			super();

			this.item = item;

			slot.item( item );
			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.3f;
				bg.ga = -0.15f;
				bg.ba = -0.15f;
			} else if (!item.isIdentified()) {
				if ((item instanceof EquipableItem || item instanceof Wand) && item.cursedKnown){
					bg.ba = +0.3f;
					bg.ra = -0.1f;
				} else {
					bg.ra = +0.35f;
					bg.ba = +0.35f;
				}
			}
		}

		@Override
		protected void createChildren() {

			bg = new ColorBlock( 28, HEIGHT, 0x9953564D );
			add( bg );

			slot = new ItemSlot();
			add( slot );

			name = PixelScene.renderTextBlock( 7 );
			add( name );

			super.createChildren();
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			slot.setRect( x, y, 28, HEIGHT );
			PixelScene.align(slot);

			name.maxWidth((int)(width - slot.width() - 2));
			name.text(Messages.titleCase(item.name()));
			name.setPos(
					slot.right()+2,
					y + (height - name.height()) / 2
			);
			PixelScene.align(name);

			super.layout();
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
		}

		protected void onPointerUp() {
			bg.brightness( 1.0f );
		}

		@Override
		protected void onClick() {
			Game.scene().add( new WndInfoItem( item ) );
		}
	}

	private class QuickSlotButton extends ItemSlot{

		private Item item;
		private ColorBlock bg;

		QuickSlotButton(Item item){
			super(item);
			this.item = item;

			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.2f;
				bg.ga = -0.1f;
			} else if (!item.isIdentified()) {
				bg.ra = 0.1f;
				bg.ba = 0.1f;
			}
		}

		@Override
		protected void createChildren() {
			bg = new ColorBlock( 1, 1, 0x9953564D );
			add( bg );

			super.createChildren();
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			bg.size( width(), height() );

			super.layout();
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
		}

		protected void onPointerUp() {
			bg.brightness( 1.0f );
		}

		@Override
		protected void onClick() {
			Game.scene().add(new WndInfoItem(item));
		}
	}
}

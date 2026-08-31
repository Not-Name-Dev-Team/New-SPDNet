package com.shatteredpixel.shatteredpixeldungeon.spdnet.windows;

import com.badlogic.gdx.graphics.Color;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.BlueButton;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.NetNote;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.NetNoteStore;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Sender;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors.NetHero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CNoteId;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.windows.NetWndPlayerInfo;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoPlant;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTrap;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;

import java.util.List;

/**
 * SPDNet: 地牢留言(Ping)系统 - 该格留言列表窗（§4 阶段D.5）。
 * 按创建序(§6 id 递增)列出该格所有留言，每条：
 *   对象图标(快照还原绘制) + 名字 + 文字 + 作者(authorMode 着色) + 点赞数；
 *   自己的 → 删除(sendNoteDelete)；他人的 → 点赞/取消点赞(sendNoteLike toggle)。
 * 快照还原为只读副本、在渲染线程开窗(由 GameScene.examineObject 触发)，不注册进场景 group。
 */
public class NetWndNoteList extends NetWindow {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 150;
	// SPDNet: 行内三行文字(名字/作者/时间) + 右侧上下两个按钮，行高需容纳
	private static final int ROW_HEIGHT = 34;
	private static final int MAX_ROWS = 5;

	private static final int VGAP = 2;

	// SPDNet: 图标槽位。文字从 x+13 开始，图标从 x+1 开始，因此图标最大宽 13-1-1=11，
	// 高度限制在行内两条文字的高度范围，保证不与文字/相邻行重叠。
	private static final float ICON_MAX_W = 11;
	private static final float ICON_MAX_H = 16;

	private final int cell;

	private ScrollPane list;
	private Component content;

	public NetWndNoteList(int cell) {
		super(PixelScene.landscape() ? WIDTH_L : WIDTH_P, 0);
		this.cell = cell;

		float y = 2;

		RenderedTextBlock title = PixelScene.renderTextBlock("本格留言(" + NetNoteStore.notesAt(cell).size() + "条)", 7);
		title.hardlight(Color.WHITE.toIntBits());
		add(title);
		title.setPos(VGAP, y + 2);
		y = title.bottom() + VGAP + 2;

		list = new ScrollPane(new Component());
		add(list);
		content = list.content();

		list.setPos(0, y);

		rebuild();

		int rows = Math.min(NetNoteStore.notesAt(cell).size(), MAX_ROWS);
		resize(width, (int) y + rows * ROW_HEIGHT + 6);

		// SPDNet(修复右下偏移)：ScrollPane.layout() 用 camera().cameraToScreen() 定位其内容相机，
		// 依赖窗口相机校准。首初次布局发生在 resize() 之前(相机未就绪)，内容相机被设到错误的屏幕坐标
		// 并保持陈旧，导致所有留言行漂到窗口外右下。这里在 resize 后重触发列表布局，
		// 让内容相机按就绪的窗口相机重算（与 NetWndLeaveNote 的 body.setRect 同理）。
		list.setRect(0, list.top(), width, rows * ROW_HEIGHT);
	}

	// SPDNet: 依据 NetNoteStore 当前数据重建列表（删除后 / 点赞刷新时调用）
	private void rebuild() {
		content.clear();

		float ypos = 0;
		List<NetNote> notes = NetNoteStore.notesAt(cell);
		for (NetNote note : notes) {
			if (note == null) {
				continue; // SPDNet: 防御空条目，避免 noteIcon 空指针
			}
			NoteEntry entry = new NoteEntry(note);
			entry.setRect(0, ypos, width, ROW_HEIGHT);
			entry.setCallback(this::rebuild);
			content.add(entry);
			ypos = entry.bottom() + VGAP;
		}

		// 内容总高度 = 全部行高；ScrollPane 视口高度 = 可见行数(最多 MAX_ROWS)，
		// 超出时才能显示滚动条并可滚动（否则多余留言会被窗口底部裁掉且滚不到）。
		content.setRect(0, 0, width, ypos);
		list.setRect(0, list.top(), width, Math.min(notes.size(), MAX_ROWS) * ROW_HEIGHT);
		list.scrollTo(0, 0);
	}

	// SPDNet: 留言行：图标 + 名字/文字 + 作者 + 时间 + 操作按钮(详情/删除-点赞)
	public static class NoteEntry extends Component {

		private final NetNote note;
		private Runnable refresh;

		private Image icon;
		private RenderedTextBlock titleText;
		private RenderedTextBlock authorText;
		private RenderedTextBlock timeText;
		private BlueButton detail;
		private BlueButton action;

		NoteEntry(NetNote note) {
			// super() 最先被隐式调用，会触发 createChildren()；因此在 createChildren 里
			// 不能依赖本构造体（super 之后）才赋值的字段。改为把 note 的子组件构建
			// 全部放到构造体里（此时字段已就绪），createChildren 留空。
			this.note = note;

			icon = noteIcon(note);
			if (icon == null) {
				icon = WndInfoCell.cellImage(note.pos);
			}

			String name = noteName(note);
			String msg = note.message == null ? "" : note.message;
			titleText = PixelScene.renderTextBlock(name + (msg.isEmpty() ? "" : "： " + msg), 7);
			titleText.maxWidth((int) (width - 26 - 30));

			boolean mine = NetNoteStore.isMine(note);
			// SPDNet: 列表不显示游戏模式(去掉 authorMode 标签)，只显示作者 + 点赞数
			authorText = PixelScene.renderTextBlock(
					(mine ? "我" : note.author) + "　♥ " + note.likes,
					6);
			authorText.hardlight(authorColor());

			// SPDNet: 单独一行显示留言时间(含年月日 + 时分秒)；字号 5 并收紧最大宽度，
			// 保证单行放下且不与右侧按钮重叠
			timeText = PixelScene.renderTextBlock(dateLabel(note), 5);
			timeText.maxWidth((int) (width - 40));
			timeText.hardlight(Color.WHITE.toIntBits());

			// SPDNet: "详情"按钮 → 复现游戏内放大镜查看该对象的 info 窗口
			detail = new BlueButton("详情", 6) {
				@Override
				protected void onClick() {
					showDetail(note);
				}
			};

			action = new BlueButton(actionLabel(), 6) {
				@Override
				protected void onClick() {
					if (mine) {
						Sender.sendNoteDelete(new CNoteId(note.id));
						NetNoteStore.remove(note.id);
					} else {
						boolean nowLiked = !NetNoteStore.isMyLiked(note.id);
						NetNoteStore.toggleMyLiked(note.id, nowLiked);
						note.likes = Math.max(0, note.likes + (nowLiked ? 1 : -1));
						Sender.sendNoteLike(new CNoteId(note.id));
					}
					if (refresh != null) {
						refresh.run();
					}
				}
			};

			add(icon);
			add(titleText);
			add(authorText);
			add(timeText);
			add(detail);
			add(action);
		}

		void setCallback(Runnable refresh) {
			this.refresh = refresh;
		}

		@Override
		protected void createChildren() {
			// SPDNet: 空实现。子组件在构造函数里构建（见构造体注释），
			// 因为 createChildren 在 super() 时被调用，那时 note 字段尚未赋值。
			super.createChildren();
		}

		@Override
		protected void layout() {
			super.layout();

			// SPDNet: 图标等比缩放限制在左侧槽位内(ICON_MAX_W/H)，避免放大后越过文字列(x+13)
			// 或相邻行与之重叠（原固定 scale 2f 会把 16px 图标放大到 32px 导致全部重叠）
			if (icon.width > 0 && icon.height > 0) {
				icon.scale.set(Math.min(ICON_MAX_W / icon.width, ICON_MAX_H / icon.height));
			} else {
				icon.scale.set(1f);
			}
			icon.x = this.x + 1;
			icon.y = this.y + 2;
			icon.visible = true;
			PixelScene.align(icon);

			titleText.maxWidth((int) (width - 26 - 30));
			titleText.setPos(this.x + 13, this.y);
			PixelScene.align(titleText);

			authorText.setPos(this.x + 13, titleText.bottom() - 1);
			PixelScene.align(authorText);

			timeText.maxWidth((int) (this.width - 40));
			timeText.setPos(this.x + 13, authorText.bottom() - 1);
			PixelScene.align(timeText);

			// SPDNet: 右侧两个按钮上下堆叠："详情"(上) + "删除/点赞"(下)
			detail.setRect(this.x + width - 25, this.y + 1, 24, 13);
			action.setRect(this.x + width - 25, this.y + 16, 24, 13);
		}

		private String actionLabel() {
			if (NetNoteStore.isMine(note)) {
				return "删";
			}
			return NetNoteStore.isMyLiked(note.id) ? "消赞" : "赞";
		}

		private int authorColor() {
			if ("FUN".equals(note.authorMode)) {
				return 0xFF66DD66;
			} else if ("DAILY".equals(note.authorMode)) {
				return 0xFF8899CC;
			}
			return Color.WHITE.toIntBits();
		}
	}

	// ================= 快照还原（只读副本，渲染线程） =================

	// SPDNet: 还原留言对象图标。PLAYER 快照是原始 hero bundle（viewHero 链路），
	// 其余实体是 NetPing.toSnapshot 包装的 {note:bundlable}。
	private static Image noteIcon(NetNote note) {
		if (note.snapshot == null) {
			return null;
		}
		try {
			switch (note.noteType) {
				case "PLAYER": {
					Bundle b = Bundle.fromString(note.snapshot);
					if (b == null) {
						return null;
					}
					NetHero hero = new NetHero(note.author);
					hero.restoreFromBundleOverride(b);
					return HeroSprite.avatar(hero.heroClass, hero.tier());
				}
				case "MOB": {
					Bundlable o = restoreBundle(note.snapshot);
					if (o instanceof Mob) {
						Mob mob = (Mob) o;
						mob.pos = note.pos;
						return mob.sprite();
					}
					return null;
				}
				case "ITEM": {
					Bundlable o = restoreBundle(note.snapshot);
					if (o instanceof Heap) {
						Heap heap = (Heap) o;
						heap.pos = note.pos;
						return new ItemSprite(heap);
					}
					return null;
				}
				case "PLANT": {
					Bundlable o = restoreBundle(note.snapshot);
					if (o instanceof Plant) {
						Plant plant = (Plant) o;
						plant.pos = note.pos;
						// SPDNet(阶段E): 用对象自身 image 直接画植物图标，而非该格基础地形 tile。
						// 原 tile(pos, map[pos]) 依赖现场 level 的 plants 映射，还原副本不在其中会画错/缺失。
						return TerrainFeaturesTilemap.getPlantVisual(plant);
					}
					return null;
				}
				case "TRAP": {
					Bundlable o = restoreBundle(note.snapshot);
					if (o instanceof Trap) {
						Trap trap = (Trap) o;
						trap.pos = note.pos;
						// SPDNet(阶段E): 用对象自身 color/shape 直接画陷阱图标并尊重 visible/active，
						// 而非该格基础地形 tile（陷阱不改地面，原 tile 必画错）。
						return TerrainFeaturesTilemap.getTrapVisual(trap);
					}
					return null;
				}
				default:
					return null;
			}
		} catch (Throwable t) {
			// 快照还原失败只影响单个图标，不影响整窗（服务端对快照来源不做强校验）
			return null;
		}
	}

	private static String noteName(NetNote note) {
		try {
			switch (note.noteType) {
				case "PLAYER": {
					Bundle b = note.snapshot == null ? null : Bundle.fromString(note.snapshot);
					if (b != null) {
						NetHero hero = new NetHero(note.author);
					hero.restoreFromBundleOverride(b);
					// SPDNet: 显示打ping玩家的名字，而非其职业名。name 即玩家名。
					return hero.name != null && !hero.name.isEmpty() ? hero.name : Messages.titleCase(hero.className());
					}
					return "玩家";
				}
				case "MOB": {
					Bundlable o = restoreBundle(note.snapshot);
					return o instanceof Mob ? Messages.titleCase(((Mob) o).name()) : "怪物";
				}
				case "ITEM": {
					Bundlable o = restoreBundle(note.snapshot);
					return o instanceof Heap ? Messages.titleCase(((Heap) o).title()) : "物品堆";
				}
				case "PLANT": {
					Bundlable o = restoreBundle(note.snapshot);
					return o instanceof Plant ? Messages.titleCase(((Plant) o).name()) : "植物";
				}
				case "TRAP": {
					Bundlable o = restoreBundle(note.snapshot);
					return o instanceof Trap ? Messages.titleCase(((Trap) o).name()) : "陷阱";
				}
				case "FLOOR":
				default:
					return "这块地板";
			}
		} catch (Throwable t) {
			return "留言";
		}
	}

	// SPDNet: 留言时间展示(含年月日 + 时分秒)。服务端 createTime 为 LocalDateTime 的 ISO 串(无时区)，
	// 兼容解析失败时回退原始串。用两位年份保持单行，避免与右侧按钮重叠。
	private static String dateLabel(NetNote note) {
		if (note.createTime == null || note.createTime.isEmpty()) {
			return "";
		}
		try {
			java.time.LocalDateTime t = java.time.LocalDateTime.parse(note.createTime);
			return t.format(java.time.format.DateTimeFormatter.ofPattern("yy年M月d日 HH:mm:ss"));
		} catch (Throwable e) {
			return note.createTime;
		}
	}

	// SPDNet: "详情"按钮 → 复现游戏内放大镜(examine)查看该对象的 info 窗口，等效于就地查看。
	// PLAYER → NetWndPlayerInfo；MOB/ITEM/PLANT/TRAP → 对应 WndInfo*；FLOOR/兜底 → WndInfoCell。
	// 还原失败时回退该格 info，不崩整窗。
	private static void showDetail(NetNote note) {
		if (note == null) {
			return;
		}
		try {
			switch (note.noteType) {
				case "PLAYER": {
					Bundle b = note.snapshot == null ? null : Bundle.fromString(note.snapshot);
					if (b == null) {
						GameScene.show(new WndInfoCell(note.pos));
						return;
					}
					NetHero hero = new NetHero(note.author);
					hero.restoreFromBundleOverride(b);
					// SPDNet: noGift=true —— 玩家仅是一个快照，非在线实体，禁止赠送物品
					GameScene.show(new NetWndPlayerInfo(note.author, hero, true));
					return;
				}
				case "MOB": {
					Bundlable o = restoreBundle(note.snapshot);
					if (o instanceof Mob) {
						GameScene.show(new WndInfoMob((Mob) o));
						return;
					}
					break;
				}
				case "ITEM": {
					Bundlable o = restoreBundle(note.snapshot);
					if (o instanceof Heap) {
						GameScene.show(new WndInfoItem((Heap) o));
						return;
					}
					break;
				}
				case "PLANT": {
					Bundlable o = restoreBundle(note.snapshot);
					if (o instanceof Plant) {
						GameScene.show(new WndInfoPlant((Plant) o));
						return;
					}
					break;
				}
				case "TRAP": {
					Bundlable o = restoreBundle(note.snapshot);
					if (o instanceof Trap) {
						GameScene.show(new WndInfoTrap((Trap) o));
						return;
					}
					break;
				}
				case "FLOOR":
				default:
					GameScene.show(new WndInfoCell(note.pos));
					return;
			}
		} catch (Throwable t) {
			// 快照还原失败只影响单个详情，回退该格 info
		}
		GameScene.show(new WndInfoCell(note.pos));
	}

	// SPDNet: 解开 NetPing.toSnapshot 的 {note:bundlable} 包装
	private static Bundlable restoreBundle(String snapshot) {
		Bundle b = Bundle.fromString(snapshot);
		return b == null ? null : b.get("note");
	}
}
package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors.NetHero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.windows.NetWndLeaveNote;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;

import java.util.ArrayList;
import java.util.Locale;

/**
 * SPDNet: 地牢留言(Ping)系统 - "ping 目标"创建流程编排。
 * 见 test/ping-design.md §2 / 阶段C：
 * 聊天窗点「ping 目标」→ 关闭聊天窗 → GameScene.selectCell 瞄准式选格
 * → 收集该格候选(NetHero/Mob/Heap/Plant/Trap) + "自己" + "这块地板"
 * → WndOptions 选择对象 → 打开 NetWndLeaveNote 写下留言。
 */
public class NetPing {

	// SPDNet: 从聊天窗「ping 目标」按钮进入，发起一次瞄准式选格
	public static void startPingTarget() {
		if (Dungeon.hero == null) {
			return;
		}
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell != null) {
					// SPDNet: 恢复默认选格监听，避免留言流程结束后残留本次监听导致误触发
					GameScene.ready();
					openCandidates(cell);
				}
			}

			@Override
			public String prompt() {
				return "选择要 ping 的目标所在格";
			}
		});
	}

	// SPDNet: 收集该格可留言对象，弹出对象选择 WndOptions
	private static void openCandidates(int cell) {
		if (cell < 0 || cell >= Dungeon.level.length()) {
			return;
		}
		ArrayList<NetNoteTarget> targets = new ArrayList<>();
		ArrayList<String> names = new ArrayList<>();

		// 自己：该格为本机英雄所在的格 → 使 §7 的 self-ping 可达
		if (Dungeon.hero.pos == cell) {
			targets.add(playerTarget(cell, Dungeon.hero));
			names.add(Messages.titleCase(Dungeon.hero.className()).toUpperCase(Locale.ENGLISH) + "（自己）");
		}

		// 其他在线玩家
		NetHero netHero = NetHero.findPlayerAtCell(cell);
		if (netHero != null) {
			targets.add(playerTarget(cell, netHero));
			names.add(netHero.name);
		}

		// 怪物（仅视野内可见格；自己不是 Mob 不会重复加入）
		if (Dungeon.level.heroFOV[cell]) {
			Char ch = Actor.findChar(cell);
			if (ch instanceof Mob) {
				Mob mob = (Mob) ch;
				targets.add(new NetNoteTarget(cell, "MOB", Messages.titleCase(mob.name()), mob.sprite(), toSnapshot(mob), null));
				names.add(Messages.titleCase(mob.name()));
			}
		}

		// 物品堆（ITEM）
		Heap heap = Dungeon.level.heaps.get(cell);
		if (heap != null && heap.seen && !heap.isEmpty()) {
			targets.add(new NetNoteTarget(cell, "ITEM", Messages.titleCase(heap.title()), new ItemSprite(heap), toSnapshot(heap), null));
			names.add(Messages.titleCase(heap.title()));
		}

		// 植物
		Plant plant = Dungeon.level.plants.get(cell);
		if (plant != null) {
			targets.add(new NetNoteTarget(cell, "PLANT", Messages.titleCase(plant.name()), TerrainFeaturesTilemap.tile(cell, Dungeon.level.map[cell]), toSnapshot(plant), null));
			names.add(Messages.titleCase(plant.name()));
		}

		// 陷阱
		Trap trap = Dungeon.level.traps.get(cell);
		if (trap != null && trap.visible) {
			targets.add(new NetNoteTarget(cell, "TRAP", Messages.titleCase(trap.name()), TerrainFeaturesTilemap.tile(cell, Dungeon.level.map[cell]), toSnapshot(trap), null));
			names.add(Messages.titleCase(trap.name()));
		}

		// 这块地板（非实体，只存坐标+文本）
		targets.add(new NetNoteTarget(cell, "FLOOR", "这块地板", WndInfoCell.cellImage(cell), null, null));
		names.add("这块地板");

		NetNoteTarget[] arr = targets.toArray(new NetNoteTarget[0]);
		GameScene.show(new WndOptions(Icons.get(Icons.INFO),
				"选择要留言的对象",
				"该格上有以下对象（选择后写下留言）",
				names.toArray(new String[0])) {
			@Override
			protected void onSelect(int index) {
				openLeaveNote(arr[index]);
			}
		});
	}

	// SPDNet: PLAYER 类型目标（自己或他人），服务端主动索取快照，客户端不提交 snapshot
	private static NetNoteTarget playerTarget(int cell, Hero h) {
		String targetName = (h instanceof NetHero) ? ((NetHero) h).name : Net.name;
		int tier = (h instanceof NetHero) ? ((NetHero) h).tier : h.tier();
		Image icon = HeroSprite.avatar(h.heroClass, tier);
		return new NetNoteTarget(cell, "PLAYER", Messages.titleCase(h.className()), icon, null, targetName);
	}

	private static void openLeaveNote(NetNoteTarget target) {
		GameScene.show(new NetWndLeaveNote(target));
	}

	// SPDNet: 现场对象快照（Bundle 保留具体类名，供观看向正确还原绘制）；
	// 纯串行化，不触碰全局 Dungeon.level / 场景 group，无还原副作用。
	private static String toSnapshot(Bundlable obj) {
		if (obj == null) {
			return null;
		}
		Bundle b = new Bundle();
		b.put("note", obj);
		return b.toString();
	}
}
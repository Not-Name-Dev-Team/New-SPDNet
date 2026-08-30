package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.utils.SPDUtils;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Player;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Status;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.Map;
import java.util.Set;

/**
 * 用于在当前客户端呈现的其他玩家类
 */
public class NetHero extends Hero {

	{
		alignment = Alignment.NEUTRAL;
	}

	public String name;
	public int tier;
	public int shield;
	public int challenge;

	public NetHero(String name) {
		super();
		this.name = name;
	}

	/**
	 * SPDNet: 在"将全局 Dungeon.hero 临时替换为 target"的作用域内执行 body，结束后自动恢复。
	 * 上游大量渲染/还原管线(如 buff.icon()、物品 activate()、Buff.attach())直接读取全局
	 * Dungeon.hero，而不是把 hero 作为参数传入。为让这些管线在处理其他玩家的数据时读到正确的
	 * 英雄(而非本机 hero/主菜单时的 null)，需要在使用前短暂替换全局 hero。
	 * 这是统一的作用域入口，供 NetHero.restoreFromBundleOverride 与 NetWndPlayerInfo 等查看
	 * 其他玩家的组件复用，避免各处手写 try/finally 造成遗漏或线程不一致。
	 */
	public static void withGlobalHero( Hero target, Runnable body ) {
		Hero originalHero;
		// 使用 synchronized 保持与游戏线程读取 Dungeon.hero 的互斥(参考 NetWndPlayerInfo 既有做法)
		synchronized (Dungeon.class) {
			originalHero = Dungeon.hero;
			Dungeon.hero = target;
		}
		try {
			body.run();
		} finally {
			synchronized (Dungeon.class) {
				Dungeon.hero = originalHero;
			}
		}
	}

	public static NetHero findPlayerAtCell(int cell) {
		for (NetHero player : Dungeon.level.players) {
			if (player.pos == cell) {
				return player;
			}
		}
		return null;
	}

	/**
	 * 不存储
	 */
	@Override
	public void storeInBundle(Bundle bundle) {
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
	}

	/**
	 * 父类方法调用方法, 用于在当前客户端呈现其他玩家
	 *
	 * @param bundle
	 */
	public void restoreFromBundleOverride(Bundle bundle) {
		// SPDNet: 修复快捷栏被覆盖bug
		// 设置 skipQuickslotUpdate 标志，防止恢复其他玩家英雄数据时修改当前玩家的快捷栏
		boolean wasSkipQuickslotUpdate = Belongings.skipQuickslotUpdate;
		Belongings.skipQuickslotUpdate = true;
		try {
			// SPDNet: 还原期间也需把全局 Dungeon.hero 暂时指向本英雄，与显示阶段保持一致。
			// 还原路径(物品 activate / buff attach 等)同样会读取全局 Dungeon.hero，
			// 若不替换，"查看其他玩家"时(尤其在主菜单 Dungeon.hero 为 null)会读到本机 hero
			// 或空值导致数据错乱/崩溃。
			withGlobalHero(this, () -> super.restoreFromBundle(bundle));
		} finally {
			// SPDNet: 确保即使还原异常也能恢复标志，避免 static 标志泄漏到后续逻辑
			Belongings.skipQuickslotUpdate = wasSkipQuickslotUpdate;
		}
	}

	@Override
	public boolean act() {
		return true;
	}

	/**
	 * 使用十字架
	 *
	 * @param isBlessed 当前爆的这个十字架是否被祝福
	 */
	public void useAnkh(boolean isBlessed, int blessedAnkhLeft, int unblessedAnkhLeft) {
		curAction = null;
		interrupt();
		SpellSprite.show(this, SpellSprite.ANKH);
		Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
	}

	// 死了啦, 都是你害的啦
	@Override
	public void die(Object cause) {
		destroy();
		// 掉楼好像是会使用转圈消失的动画, 这段代码在哪呢?
		if (cause != Chasm.class) sprite.die();
		Game.runOnRenderThread(() -> Sample.INSTANCE.play(Assets.Sounds.DEATH));
	}

	@Override
	public void move(int newPos, boolean travelling) {
		// 简简单单改个位
		sprite.interruptMotion();
		sprite.move(pos, newPos);
		pos = newPos;
	}

	@Override
	public void destroy() {
		super.destroy();
		Dungeon.level.players.remove(this);
		if (sprite != null){
			this.sprite.killAndErase();
		}
	}

	/**
	 * 把当前在线玩家与当前楼层同步
	 */
	public static void syncWithCurrentLevel() {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			Set<Map.Entry<String, Player>> entries = Net.playerList.entrySet();
			for (Map.Entry<String, Player> entry : entries) {
				addPlayerToDungeon(entry.getValue());
			}
		}
	}

	public static void addPlayerToDungeon(Player player) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			Status status = player.getStatus();
			if (status == null) {
				return;
			}
			// 防止重复添加
			if (NetHero.getPlayerFromDungeon(player.getName()) != null) {
				removePlayerFromDungeon(player.getName());
			}
			if (status.getSeed() == Dungeon.seed && status.getDepth() == Dungeon.depth) {
				NetHero hero = new NetHero(player.getName());
				hero.heroClass = status.getHeroClassEnum();
				hero.tier = status.getArmorTier();
				hero.pos = status.getPos();
				hero.challenge = SPDUtils.activeChallenges(status.getChallenges());
				GameScene.addPlayer(hero);
			}
		}
	}

	public static void removePlayerFromDungeon(String name) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			NetHero hero = getPlayerFromDungeon(name);
			if (hero != null) {
				hero.destroy();
			}
		}
	}

	public static NetHero getPlayerFromDungeon(String name) {
		if (!(ShatteredPixelDungeon.scene() instanceof GameScene) || Dungeon.level == null) {
			return null;
		}
		for (NetHero player : Dungeon.level.players) {
			if (player.name.equals(name)) {
				return player;
			}
		}
		return null;
	}

	@Override
	public int shielding() {
		return shield;
	}
}

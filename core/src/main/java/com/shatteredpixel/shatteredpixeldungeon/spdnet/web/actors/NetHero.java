package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
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
		super.restoreFromBundle(bundle);
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

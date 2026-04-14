/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * SPDNet修改说明（用于上游合并追踪）
 * ================================================
 *
 * 修改目的：支持查看其他玩家的天赋面板
 *
 * 修改思路：
 * 1. 添加 getHero() 方法，默认返回 Dungeon.hero
 * 2. 所有原本使用 Dungeon.hero 的地方改为调用 getHero()
 * 3. 子类（如NetTalentsPane）可覆盖 getHero() 返回不同的 Hero 对象
 * 4. 跳过徽章检查（添加 && false），始终显示所有天赋层数
 *
 * 修改点：
 * - 第147-150行：添加 getHero() 方法
 * - 第85-95行：跳过徽章检查（INFO模式）
 * - 第97-106行：使用 getHero() 替代 Dungeon.hero（UPGRADE模式）
 * - 第114行：传入 getHero() 给 TalentTierPane
 * - 第201-206行：TalentTierPane 构造函数添加 hero 参数
 * - 第244-247行：TalentTierPane.setupStars() 使用 hero 参数
 * - 第245-253行：random按钮onSelect中使用 hero 替代 Dungeon.hero
 * - 第263行：random按钮update中使用 hero 替代 Dungeon.hero
 *
 * 合并注意：
 * - 保留 getHero() 方法及其所有调用
 * - 保留 && false 的徽章检查跳过
 * - 如果上游修改了 Dungeon.hero 相关逻辑，需同步修改为 getHero() 或 hero 字段
 */
public class TalentsPane extends ScrollPane {

	ArrayList<TalentTierPane> panes = new ArrayList<>();
	ArrayList<ColorBlock> separators = new ArrayList<>();

	ColorBlock sep;
	ColorBlock blocker;
	RenderedTextBlock blockText;

	public TalentsPane( TalentButton.Mode mode ) {
		this( mode, Dungeon.hero.talents );
	}

	public TalentsPane( TalentButton.Mode mode, ArrayList<LinkedHashMap<Talent, Integer>> talents ) {
		this(mode, talents, null);
	}

	// SPDNet: 添加带 hero 参数的构造函数，修复查看其他玩家时 NPE 问题
	// 当 hero 为 null 时使用 getHero() 获取默认 hero
	public TalentsPane( TalentButton.Mode mode, ArrayList<LinkedHashMap<Talent, Integer>> talents, Hero hero ) {
		super(new Component());

		// SPDNet: 优先使用传入的 hero 参数，避免子类构造函数中 getHero() 返回 null
		Hero heroToUse = hero != null ? hero : getHero();

		Ratmogrify.useRatroicEnergy = heroToUse != null && heroToUse.armorAbility instanceof Ratmogrify;

		int tiersAvailable = 1;

		if (mode == TalentButton.Mode.INFO){
			// SPDNet: 添加 && false 跳过徽章检查，始终显示所有天赋层数
			if (false && !Badges.isUnlocked(Badges.Badge.LEVEL_REACHED_1)){
				tiersAvailable = 1;
			} else if (false && (!Badges.isUnlocked(Badges.Badge.LEVEL_REACHED_2) || !Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_2))){
				tiersAvailable = 2;
			} else if (false && !Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4)){
				tiersAvailable = 3;
			} else {
				tiersAvailable = Talent.MAX_TALENT_TIERS;
			}
		} else {
			// SPDNet: 使用 heroToUse 替代 getHero()，避免 NPE
			while (tiersAvailable < Talent.MAX_TALENT_TIERS
					&& heroToUse.lvl+1 >= Talent.tierLevelThresholds[tiersAvailable+1]){
				tiersAvailable++;
			}
			if (tiersAvailable > 2 && heroToUse.subClass == HeroSubClass.NONE){
				tiersAvailable = 2;
			} else if (tiersAvailable > 3 && heroToUse.armorAbility == null){
				tiersAvailable = 3;
			}
		}

		tiersAvailable = Math.min(tiersAvailable, talents.size());

		for (int i = 0; i < Math.min(tiersAvailable, talents.size()); i++){
			if (talents.get(i).isEmpty()) continue;

			// SPDNet: 使用 heroToUse 替代 getHero()，确保传入正确的 hero 对象
			TalentTierPane pane = new TalentTierPane(talents.get(i), i+1, mode, heroToUse);
			panes.add(pane);
			content.add(pane);

			ColorBlock sep = new ColorBlock(0, 1, 0xFF000000);
			separators.add(sep);
			content.add(sep);
		}

		sep = new ColorBlock(0, 1, 0xFF000000);
		content.add(sep);

		blocker = new ColorBlock(0, 0, 0xFF222222);
		content.add(blocker);

		if (tiersAvailable == 1) {
			blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier2"), 6);
			content.add(blockText);
		} else if (tiersAvailable == 2) {
			blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier3"), 6);
			content.add(blockText);
		} else if (tiersAvailable == 3) {
			blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier4"), 6);
			content.add(blockText);
		} else {
			blockText = null;
		}

		for (int i = panes.size()-1; i >= 0; i--){
			content.bringToFront(panes.get(i));
		}
	}

	// SPDNet: 添加获取Hero的方法，子类可覆盖以支持查看其他玩家
	protected Hero getHero() {
		return Dungeon.hero;
	}

	@Override
	protected void layout() {
		super.layout();

		float top = 0;
		for (int i = 0; i < panes.size(); i++){
			top += 2;
			panes.get(i).setRect(x, top, width, 0);
			top = panes.get(i).bottom();

			separators.get(i).x = 0;
			separators.get(i).y = top + 2;
			separators.get(i).size(width, 1);

			top += 3;

		}

		float bottom;
		if (blockText != null) {
			bottom = Math.max(height, top + 20);

			blocker.x = 0;
			blocker.y = top;
			blocker.size(width, bottom - top);

			blockText.maxWidth((int) width);
			blockText.align(RenderedTextBlock.CENTER_ALIGN);
			blockText.setPos((width - blockText.width()) / 2f, blocker.y + (bottom - blocker.y - blockText.height()) / 2);
		} else {
			bottom = Math.max(height, top);

			blocker.visible = false;
		}

		content.setSize(width, bottom);
	}

	public static class TalentTierPane extends Component {

		private int tier;
		// SPDNet: 添加hero字段
		private Hero hero;

		public RenderedTextBlock title;
		ArrayList<TalentButton> buttons;

		ArrayList<Image> stars = new ArrayList<>();
		IconButton random;

		public TalentTierPane(LinkedHashMap<Talent, Integer> talents, int tier, TalentButton.Mode mode){
			this(talents, tier, mode, Dungeon.hero);
		}

		// SPDNet: 添加带hero参数的构造函数
		public TalentTierPane(LinkedHashMap<Talent, Integer> talents, int tier, TalentButton.Mode mode, Hero hero){
			super();

			this.tier = tier;
			this.hero = hero;

			title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(TalentsPane.class, "tier", tier)), 9);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			if (mode == TalentButton.Mode.UPGRADE) {
				setupStars();
				if (this.hero.talentPointsAvailable(tier) > 0){

					random = new IconButton(Icons.SHUFFLE.get()){
						@Override
						protected void onClick() {
							super.onClick();
							GameScene.show(new WndOptions(
									Icons.SHUFFLE.get(),
									Messages.get(TalentsPane.class, "random_title"),
									Messages.get(TalentsPane.class, "random_sure"),
									Messages.get(TalentsPane.class, "random_yes"),
									Messages.get(TalentsPane.class, "random_one"),
									Messages.get(TalentsPane.class, "random_no")) {
								@Override
								protected void onSelect(int index) {
									super.onSelect(index);
									//safety check to ensure previous UI is still there
									if (TalentTierPane.this.parent == null){
										return;
									}
									if (index == 0 || index == 1){
										// SPDNet: 使用 hero 替代 Dungeon.hero
										while (hero.talentPointsAvailable(tier) > 0){
											TalentButton button = Random.element(buttons);
											if (hero.pointsInTalent(button.talent) < button.talent.maxPoints()){
												button.upgradeTalent();
												if (index == 1){
													break;
												}
											}
										};
										setupStars();
										TalentTierPane.this.layout();
									}
								}
							});
						};

						@Override
						public void update() {
							// SPDNet: 使用 hero 替代 Dungeon.hero
							if (hero.lvl >= 3 && Statistics.qualifiedForRandomVictoryBadge){
								icon.tint(1, 1, 1, (float)Math.abs(Math.cos(1.5f*Math.PI*Game.timeTotal)/2f));
							}
							super.update();
						}
					};
					add(random);
				}
			}

			buttons = new ArrayList<>();
			for (Talent talent : talents.keySet()){
				TalentButton btn = new TalentButton(tier, talent, talents.get(talent), mode){
					@Override
					public void upgradeTalent() {
						super.upgradeTalent();
						if (parent != null) {
							setupStars();
							TalentTierPane.this.layout();
						}
					}
				};
				buttons.add(btn);
				add(btn);
			}

		}

		private void setupStars(){
			if (!stars.isEmpty()){
				for (Image im : stars){
					im.killAndErase();
				}
				stars.clear();
			}

			// SPDNet: 使用hero字段替代Dungeon.hero
			int totStars = Talent.tierLevelThresholds[tier+1] - Talent.tierLevelThresholds[tier] + hero.bonusTalentPoints(tier);
			int openStars = hero.talentPointsAvailable(tier);
			int usedStars = hero.talentPointsSpent(tier);
			for (int i = 0; i < totStars; i++){
				Image im = new Speck().image(Speck.STAR);
				stars.add(im);
				add(im);
				if (i >= openStars && i < (openStars + usedStars)){
					im.tint(0.75f, 0.75f, 0.75f, 0.9f);
				} else if (i >= (openStars + usedStars)){
					im.tint(0f, 0f, 0f, 0.9f);
				}
			}

			if (random != null && openStars == 0){
				random.killAndErase();
				random.destroy();
				random = null;
			}
		}

		@Override
		protected void layout() {
			super.layout();

			int regStars = Talent.tierLevelThresholds[tier+1] - Talent.tierLevelThresholds[tier];

			float titleWidth = title.width();
			titleWidth += 2 + Math.min(stars.size(), regStars)*6;
			title.setPos(x + (width - titleWidth)/2f, y);

			float left = title.right() + 2;

			float starTop = title.top();
			if (regStars < stars.size()) starTop -= 2;

			for (Image star : stars){
				star.x = left;
				star.y = starTop;
				PixelScene.align(star);
				left += 6;
				regStars--;
				if (regStars == 0){
					starTop += 6;
					left = title.right() + 2;
				}
			}

			if (random != null){
				random.setRect(width - 16, y-2, 16, 14);
			}

			float gap = (width - buttons.size()*TalentButton.WIDTH)/(buttons.size()+1);
			left = x + gap;
			for (TalentButton btn : buttons){
				btn.setPos(left, title.bottom() + 4);
				PixelScene.align(btn);
				left += btn.width() + gap;
			}

			height = buttons.get(0).bottom() - y;

		}

	}
}

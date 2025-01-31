package com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.scene;

import static com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.align;
import static com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.GameRecord;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.windows.NetWndRanking;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;

//RankingsScene下的Record类
public class NetRecordButton extends Button {

	private static final float GAP = 4;

	private static final int[] TEXT_WIN = {0xFFFF88, 0xB2B25F};
	private static final int[] TEXT_LOSE = {0xDDDDDD, 0x888888};
	private static final int FLARE_WIN = 0x888866;
	private static final int FLARE_LOSE = 0x666666;

	private GameRecord rec;

	protected Image shield;
	private Flare flare;
	private BitmapText position;
	private RenderedTextBlock desc;
	private Image steps;
	private BitmapText depth;
	private Image classIcon;
	private BitmapText level;

	public NetRecordButton(int pos, boolean latest, GameRecord rec) {
		super();

		this.rec = rec;

		if (latest) {
			flare = new Flare(6, 24);
			flare.angularSpeed = 90;
			flare.color(rec.isWin() ? FLARE_WIN : FLARE_LOSE);
			addToBack(flare);
		}

		if (pos != Rankings.TABLE_SIZE-1) {
			position.text(Integer.toString(pos + 1));
		} else
			position.text(" ");
		position.measure();

		desc.text( rec.getPlayerName() + " " + rec.desc() );

		int odd = pos % 2;

		if (rec.isWin()) {
			shield.copy( new ItemSprite(ItemSpriteSheet.AMULET, null) );
			position.hardlight( TEXT_WIN[odd] );
			desc.hardlight( TEXT_WIN[odd] );
			depth.hardlight( TEXT_WIN[odd] );
			level.hardlight( TEXT_WIN[odd] );
		} else {
			position.hardlight( TEXT_LOSE[odd] );
			desc.hardlight( TEXT_LOSE[odd] );
			depth.hardlight( TEXT_LOSE[odd] );
			level.hardlight( TEXT_LOSE[odd] );

			if (rec.getDepth() != 0){
				depth.text(Integer.toString(rec.getDepth()) );
				depth.measure();
				steps.copy(Icons.STAIRS.get());

				add(steps);
				add(depth);
			}

			if (rec.isAscending()){
				shield.copy(new ItemSprite(ItemSpriteSheet.AMULET, null));
				shield.hardlight(0.4f, 0.4f, 0.7f);
			}

		}

		if (rec.isDaily()){
			shield.copy( Icons.get(Icons.CALENDAR) );
			shield.hardlight(0.5f, 1f, 2f);
		} else if (!rec.getCustomSeed().isEmpty()){
			shield.copy( Icons.get(Icons.SEED) );
			shield.hardlight(1f, 1.5f, 0.67f);
		}

		if (rec.getLevel() != 0){
			level.text( Integer.toString(rec.getLevel()) );
			level.measure();
			add(level);
		}

		classIcon.copy( Icons.get( HeroClass.valueOf(rec.getHeroClass())) );
		if (HeroClass.valueOf(rec.getHeroClass()) == HeroClass.ROGUE){
			//cloak of shadows needs to be brightened a bit
			classIcon.brightness(2f);
		}
	}

	@Override
	protected void createChildren() {

		super.createChildren();

		shield = new Image(new ItemSprite( ItemSpriteSheet.TOMB, null ));
		add( shield );

		position = new BitmapText( PixelScene.pixelFont);
		add( position );

		desc = renderTextBlock( 7 );
		add( desc );

		depth = new BitmapText( PixelScene.pixelFont);

		steps = new Image();

		classIcon = new Image();
		add( classIcon );

		level = new BitmapText( PixelScene.pixelFont);
	}

	@Override
	protected void layout() {

		super.layout();

		shield.x = x + (16 - shield.width) / 2f;
		shield.y = y + (height - shield.height) / 2f;
		align(shield);

		position.x = shield.x + (shield.width - position.width()) / 2f;
		position.y = shield.y + (shield.height - position.height()) / 2f + 1;
		align(position);

		if (flare != null) {
			flare.point( shield.center() );
		}

		classIcon.x = x + width - 16 + (16 - classIcon.width())/2f;
		classIcon.y = shield.y + (16 - classIcon.height())/2f;
		align(classIcon);

		level.x = classIcon.x + (classIcon.width - level.width()) / 2f;
		level.y = classIcon.y + (classIcon.height - level.height()) / 2f + 1;
		align(level);

		steps.x = x + width - 32 + (16 - steps.width())/2f;
		steps.y = shield.y + (16 - steps.height())/2f;
		align(steps);

		depth.x = steps.x + (steps.width - depth.width()) / 2f;
		depth.y = steps.y + (steps.height - depth.height()) / 2f + 1;
		align(depth);

		desc.maxWidth((int)(steps.x - (x + 16 + GAP)));
		desc.setPos(x + 16 + GAP, shield.y + (shield.height - desc.height()) / 2f + 1);
		align(desc);
	}

	@Override
	protected void onClick() {
		parent.add( new NetWndRanking( rec ) );
	}
}

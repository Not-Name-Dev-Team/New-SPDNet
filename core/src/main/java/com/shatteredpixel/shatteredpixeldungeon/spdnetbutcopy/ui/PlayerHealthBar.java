package com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors.NetHero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CharHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.RenderedText;
import com.watabou.utils.PointF;

//HealthBar
public class PlayerHealthBar extends CharHealthIndicator {

	private static final int COLOR_BG	= 0xFFCC0000;
	private static final int COLOR_HP	= 0xFF00EE00;
	private static final int COLOR_SHLD = 0xFFBBEEBB;

	private static final int HEIGHT = 5;

	private ColorBlock Bg;
	private ColorBlock Shld;
	private ColorBlock Hp;
	private Image challengeIcon;
	private RenderedText challengeText;
	private RenderedText playerName;

	private float health;
	private float shield;
	private NetHero target;
	public int challenges;

	public PlayerHealthBar(NetHero player) {
		super(player);
		target = player;
		challenges = player.challenge;
	}

	@Override
	protected void createChildren() {
		Bg = new ColorBlock( 1, 1, COLOR_BG );
		add( Bg );

		Shld = new ColorBlock( 1, 1, COLOR_SHLD );
		add( Shld );

		Hp = new ColorBlock( 1, 1, COLOR_HP );
		add( Hp );

		challengeIcon = Icons.get(Icons.CHAL_COUNT);
		add(challengeIcon);

		challengeText = new RenderedText("", 20);
		add(challengeText);

		playerName = new RenderedText("", 20);
		add(playerName);

		height = HEIGHT;
	}

	@Override
	protected void layout() {

		Bg.x = Shld.x = Hp.x = x;
		Bg.y = Shld.y = Hp.y = y;

		Bg.size( width, height );

		//logic here rounds up to the nearest pixel
		float pixelWidth = width;
		if (camera() != null) pixelWidth *= camera().zoom;

		if (playerName.text().isEmpty()) {
			playerName.visible = false;
		} else {
			playerName.visible = true;
			playerName.x = ((x + width / 2) - (playerName.width() / 2));
			playerName.scale = new PointF(0.25f, 0.25f);
			playerName.y = y - 4;
		}

		if (challengeText.text().isEmpty() || challengeText.text().equals("0挑")) {
			challengeIcon.visible = false;
			challengeText.visible = false;
		} else {
			challengeIcon.visible = true;
			challengeText.visible = true;
			challengeIcon.x = x - 2;
			challengeIcon.y = y - 10;
			challengeIcon.scale = new PointF(0.8f, 0.8f);
			challengeText.scale = new PointF(0.25f, 0.25f);
			challengeText.x = challengeIcon.x + challengeIcon.width() + 1;
			challengeText.y = y - 9;
		}

		Shld.size( width * (float)Math.ceil(shield * pixelWidth)/pixelWidth, 1 );
		Hp.size( width * (float)Math.ceil(health * pixelWidth)/pixelWidth, 1 );
	}

	public void level( float value ) {
		level( value, 0f );
	}

	public void level( float health, float shield ){
		this.health = health;
		this.shield = shield;
		layout();
	}

	@Override
	public void update() {
		super.update();

		if (target != null && target.isAlive() && target.isActive() && target.sprite.visible) {
			CharSprite sprite = target.sprite;
			width = sprite.width();
			x = sprite.x;
			y = sprite.y - 3;
			challengeText.text(target.challenge + "挑");
			playerName.text(target.name);
			level(target);
			visible = true;
		} else {
			visible = false;
		}
	}

	public void level(Char c){
		float health = c.HP;
		float shield = c.shielding();
		float max = Math.max(health+shield, c.HT);

		level(health/max, (health+shield)/max);
	}

	public void setTarget(NetHero ch) {
		if (ch != null && ch.isAlive() && ch.isActive()) {
			target = ch;
		} else {
			target = null;
		}
	}
}

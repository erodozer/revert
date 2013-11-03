package com.kgp.game;

// FireBallSprite.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* A fireball starts at the lower right hand side of the panel,
 and travels straight across to the left (at varying speeds).
 If it hits 'jack', it explodes (with a suitable explosion sound).

 A fireball that has left the left hand side, or exploded, is
 reused.
 */

import com.kgp.core.JackPanel;
import com.kgp.imaging.ImagesLoader;

public class FireBallSprite extends Projectile {
	// the ball's x- and y- step values are STEP +/- STEP_OFFSET
	private static final int STEP = -10; // moving left
	private static final int STEP_OFFSET = 2;

	public FireBallSprite(int w, int h, ImagesLoader imsLd, JackPanel jp, JumperSprite j) {
		super(w, h, imsLd, jp, j);
		
		initPosition();
	}

	/**
	 * adjust the fireball's position and its movement left
	 */
	protected void initPosition() {
		this.setImage("fireball");
		int h = getPHeight() / 2 + ((int) (getPHeight() * Math.random()) / 2);
		// along the lower half of the rhs edge
		if (h + getHeight() > getPHeight())
			h -= getHeight(); // so all on screen

		setPosition(getPWidth(), h);
		setVelocity(STEP + getRandRange(STEP_OFFSET), 0); // move left
	}

	/**
	 * random number generator between -x and x
	 * 
	 * @param x
	 * @return
	 */
	private int getRandRange(int x) {
		return ((int) (2 * x * Math.random())) - x;
	}

	public void updateSprite() {
		super.updateSprite();
		if (isOffScreen())
		{
			this.initPosition();	
		}
		else if (hasHitJack())
		{
			jp.showExplosion(getXPosn(), getYPosn() + getHeight() / 2);
			this.initPosition();
		}
	}

	public boolean isActive() {
		return true;
	}

} // end of FireBallSprite class

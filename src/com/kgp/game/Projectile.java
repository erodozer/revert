package com.kgp.game;

// FireBallSprite.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* A fireball starts at the lower right hand side of the panel,
 and travels straight across to the left (at varying speeds).
 If it hits 'jack', it explodes (with a suitable explosion sound).

 A fireball that has left the left hand side, or exploded, is
 reused.
 */

import java.awt.*;

import com.kgp.core.JackPanel;
import com.kgp.imaging.ImagesLoader;
import com.kgp.imaging.Sprite;

public abstract class Projectile extends Sprite {

	protected JackPanel jp;
	protected JumperSprite jack;

	public Projectile(int w, int h, ImagesLoader imsLd, JackPanel jp,
			JumperSprite j) {
		super(0, 0, w, h, imsLd, "fireball");
		// the ball is positioned in the middle at the panel's rhs
		this.jp = jp;
		jack = j;
	}

	protected abstract void initPosition();


	/**
	 * If the ball has hit jack, tell JackPanel (which will display an explosion
	 * and play a clip), and begin again.
	 */
	protected boolean hasHitJack()
	{
		Rectangle jackBox = jack.getMyRectangle();
		//jackBox.grow(-jackBox.width / 3, 0); // make jack's bounded box thinner

		return jackBox.intersects(getMyRectangle());
	} 
	/**
	 * Check if the projectile has gone outside the panel's boundaries
	 * @return
	 */
	protected boolean isOffScreen() {
		return (getXPosn() + getWidth() <= 0 && getXVelocity() <= 0)
			|| (getXPosn() + getWidth() > getPWidth() && getXVelocity() > 0)
			|| (getYPosn() + getHeight() <= 0 && getYVelocity() <= 0)
			|| (getYPosn() + getHeight() > getPHeight() && getYVelocity() > 0);
	}

	@Override
	public boolean isActive() {
		return !isOffScreen() || !hasHitJack();
	}

}

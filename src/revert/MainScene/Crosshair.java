package revert.MainScene;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import revert.Entities.Player;

import com.kgp.imaging.ImagesLoader;
import com.kgp.imaging.Sprite;

public class Crosshair extends Sprite {

	/**
	 * Distance from the sprite that the crosshair should draw
	 */
	public static final int DIST = 80;
	
	/**
	 * Sprite to lock the crosshair relative to
	 */
	private Player parent;
	
	/**
	 * Angle in relation to the parent sprite that the crosshair is rotated to
	 */
	private float angle;
	
	/**
	 * Creates a crosshair linked to a sprite
	 * @param w - panel width
	 * @param h - panel height
	 * @param s - sprite to link to
	 * @param imsLd - image asset loader
	 */
	public Crosshair(int w, int h, Player s, ImagesLoader imsLd) {
		super(s.getXPosn(), s.getYPosn(), w, h, imsLd, "crosshair");
		
		this.parent = s;

		this.velocity.x = 0;
		this.velocity.y = 0;
	}
	
	/**
	 * Updates the crosshair to always be relative to the player
	 */
	public void updateSprite()
	{
		this.position.x = (int)(this.parent.getXPosn() + this.parent.getAim().a);
		this.position.y = (int)(this.parent.getYPosn() + this.parent.getAim().b);
		this.angle = (int)this.parent.getAim().angle();
		super.updateSprite();
	}
	
}

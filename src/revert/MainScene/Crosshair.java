package revert.MainScene;

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
		this.position.x = (int)(this.parent.getCenterXPosn() + this.parent.getAim().x - this.getWidth()/2);
		this.position.y = (int)(this.parent.getCenterYPosn() + this.parent.getAim().y - this.getHeight()/2);
		//this.angle = (int)this.parent.getAim().angle();
		super.updateSprite();
	}
	
}

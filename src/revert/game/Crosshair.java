package revert.game;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

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
	private Sprite parent;
	
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
	public Crosshair(int w, int h, Sprite s, ImagesLoader imsLd) {
		super(s.getXPosn(), s.getYPosn(), w, h, imsLd, "crosshair");
		
		this.parent = s;

		this.velocity.x = 0;
		this.velocity.y = 0;
		
		this.position.x = (int)(this.parent.getXPosn() + Math.cos(angle) * DIST);
		this.position.y = (int)(this.parent.getYPosn() + Math.sin(angle) * DIST);
	}
	
	/**
	 * Updates the crosshair to always be relative to the player
	 */
	public void updateSprite()
	{
		this.position.x = (int)(this.parent.getXPosn() + Math.sin(angle) * DIST);
		this.position.y = (int)(this.parent.getYPosn() + Math.cos(angle) * DIST);
		
		super.updateSprite();
	}

	/**
	 * Sets the position of the crosshair along the vector to the specified point
	 * @param x
	 * @param y
	 */
	public void setAngle(Point2D mouse, AffineTransform matrix)
	{
		Point2D loc = matrix.transform(parent.getPosn(), null);
	
		//find the angle between the vectors
		double yDst = mouse.getY() - loc.getY();
		double xDst = mouse.getX() - loc.getX();
		
		this.angle = (float)Math.atan2(xDst, yDst);
	}
	
}

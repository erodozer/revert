package com.kgp.imaging;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Observable;

import com.kgp.core.Game;
import com.kgp.util.Vector2;

/**
 *  Sprite.java
 *  
 *  A Sprite has a position, velocity (in terms of steps),
 *  an image, and can be deactivated.
 *  The sprite's image is managed with an ImagesLoader object,
 *  and an ImagesPlayer object for looping.
 *  The images stored until the image 'name' can be looped
 *  through by calling loopImage(), which uses an
 *  ImagesPlayer object.
 *  
 *  @author Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
 */
public class Sprite extends Observable {
	// default dimensions when there is no image
	private static final int SIZE = 12;

	// image-related
	protected ImagesLoader imsLoader;
	protected String imageName;
	private BufferedImage image;
	
	// for playing a loop of images
	protected ImagesPlayer player;

	private boolean isActive = true;
	// a sprite is updated and drawn only when it is active

	protected Rectangle myRect;
	
	/**
	 * Amount the sprite will move per update cycle
	 */
	protected Vector2 velocity;
	/**
	 * Real position of the sprite in the world
	 */
	protected Vector2 position;
	/**
	 * Offset used for rendering
	 */
	protected Vector2 offset;
	
	/**
	 * Orientation of the sprite
	 */
	protected float angle;
	/**
	 * Rotational velocity of the sprite
	 */
	protected float rotation;
	
	/**
	 *  image dimensions
	 */
	protected Dimension dimensions;
	
	protected Dimension pDimensions;

	protected AffineTransform trans = AffineTransform.getTranslateInstance(0, 0);
	protected boolean flipX = false;
	
	protected float duration;
	
	public Sprite(float x, float y, int w, int h, ImagesLoader imsLd, String name) {
		this.position = new Vector2();
		this.velocity = new Vector2();
		this.offset = new Vector2();

		this.pDimensions = new Dimension(w, h);

		this.imsLoader = imsLd;
		
		// the sprite's default image is 'name'
		setImage(name);
		setPosition(x, y);
		setVelocity(w, h);
	}

	/**
	 * assign the name image to the sprite
	 * @param name
	 */
	public void setImage(String name)
	{
		imageName = name;
		image = imsLoader.getImage(imageName);
		if (image == null) {
			System.out.println("No sprite image for " + imageName);
			dimensions = new Dimension(SIZE, SIZE);
		} else {
			dimensions = new Dimension(image.getWidth(), image.getHeight());
		}
		//create bounding box
		this.myRect = new Rectangle(0, 0, dimensions.width, dimensions.height);
		// no image loop playing
		player = null;
	}
	
	/**
	 * assign the image to the sprite and make it loop if specified
	 * @param name
	 * @param loop
	 */
	public void setImage(String name, boolean loop)
	{
		this.setImage(name);
		if (loop)
		{
			this.loopImage(duration);
		}
	}
	
	public void setImage(String name, boolean animated, boolean loop)
	{
		this.setImage(name);
		if (animated)
		{
			if (loop)
				this.loopImage(duration);
			else
				this.playImage(duration);
		}
	}

	/**
	 * Switch on loop playing
	 * 
	 * @param animPeriod
	 *            - period length in ms (from the enclosing panel)
	 * @param seqDuration
	 *            - The total time for the loop to play the sequence
	 */
	public void loopImage(float seqDuration) {
		if (imsLoader.numImages(imageName) > 1) {
			player = null; // to encourage garbage collection of previous player
			player = new ImagesPlayer(imageName, Game.getDeltaTime(), seqDuration, true, imsLoader);
		} else {
			System.out.println(imageName + " is not a sequence of images");
		}
	}
	
	/**
	 * Animate through a spritesheet once
	 * 
	 * @param animPeriod
	 *            - period length in ms (from the enclosing panel)
	 * @param seqDuration
	 *            - The total time for the loop to play the sequence
	 */
	public void playImage(float seqDuration) {
		if (imsLoader.numImages(imageName) > 1) {
			player = null; // to encourage garbage collection of previous player
			player = new ImagesPlayer(imageName, Game.getDeltaTime(), seqDuration, false, imsLoader);
		} else {
			System.out.println(imageName + " is not a sequence of images");
		}
	}

	public void stopLooping() {
		if (player != null)
			player.stop();
	}

	public int getWidth() {
		return this.dimensions.width;
	}

	public int getHeight() {
		return this.dimensions.height;
	}

	public int getPWidth() {
		return this.pDimensions.width;
	}

	public int getPHeight() {
		return this.pDimensions.height;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean a) {
		isActive = a;
	}

	/**
	 * Forcefully sets the actual position of the sprite
	 */
	public void setPosition(float x, float y) {
		this.position.x = x;
		this.position.y = y;
	}

	/**
	 * Shifts the sprites position relatively from its current position
	 * @param xDist
	 * @param yDist
	 */
	public void translate(float xDist, float yDist) {
		this.position.translate(xDist, yDist);
	}

	public float getXPosn() {
		return this.position.x;
	}

	public float getYPosn() {
		return this.position.y;
	}
	
	public float getRealXPosn() {
		return this.position.x + this.offset.x;
	}
	
	public float getRealYPosn() {
		return this.position.y + this.offset.y;
	}
	
	/**
	 * The sprite's position
	 */
	public Vector2 getPosn() {
		return this.position.clone();
	}

	/**
	 * Forcefully sets the sprite's movement velocity
	 * @param x - horizontal shift value
	 * @param y - vertical shift value
	 */
	public void setVelocity(float dx, float dy) {
		this.velocity.x = dx;
		this.velocity.y = dy;
	}

	public float getXVelocity() {
		return this.velocity.x;
	}

	public float getYVelocity() {
		return this.velocity.y;
	}

	/**
	 * get the angle at which this sprite is rotated
	 * @return float
	 */
	public float getOrientation() {
		return this.angle;
	}
	
	public void setOrientation(float angle) {
		this.angle = angle;
	}
	
	public void setRotationalRate(float rate) {
		this.rotation = rate;
	}
	
	public float getRetationalRate(){
		return this.rotation;
	}
	
	/**
	 * @return the bounding box of the sprite
	 */
	public Rectangle getMyRectangle() {
		//set the rectangle's position only when requested
		myRect.x = (int) (this.position.x + this.offset.x);
		myRect.y = (int) (this.position.y + this.offset.y);
		myRect.width = this.dimensions.width;
		myRect.height = this.dimensions.height;
		
		return myRect;
	}
	
	/**
	 * Perform standard actions for the sprite on each cycle
	 */
	public void updateSprite()
	{
		if (isActive()) {
			this.position.translate(this.velocity.x, this.velocity.y);
			this.angle += this.rotation;
			
			// update the animation
			if (this.player != null)
			{
				this.player.updateTick();
			}
		}
		
		trans.setToTranslation((int)this.position.x + this.offset.x, (int)this.position.y + this.offset.y);
		trans.rotate(this.angle, this.getWidth()/2, this.getHeight()/2);
		if (flipX)
		{
			trans.translate(this.getWidth(), 0);
			trans.scale(-1.0, 1.0);
		}
	}

	/**
	 * Draws the sprite to the graphics context
	 * @param g
	 */
	public void drawSprite(Graphics2D g) {
		if (isActive()) {
			// if the sprite has no image, draw a yellow circle instead
			if (image == null) { 
				g.setColor(Color.yellow);
				g.fillOval((int)this.position.x, (int)this.position.y, SIZE, SIZE);
				g.setColor(Color.black);
			} 
			else {
				if (player != null)
					image = player.getCurrentImage();
				g.drawImage(image, trans, null);
			}
		}
	}

}

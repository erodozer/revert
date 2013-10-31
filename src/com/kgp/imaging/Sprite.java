package com.kgp.imaging;


import java.awt.*;

import java.awt.image.*;

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
public class Sprite {
	// default step sizes (how far to move in each update)
	private static final int XSTEP = 5;
	private static final int YSTEP = 5;

	// default dimensions when there is no image
	private static final int SIZE = 12;

	// image-related
	private ImagesLoader imsLoader;
	private String imageName;
	private BufferedImage image;
	
	// for playing a loop of images
	protected ImagesPlayer player;
	protected boolean isLooping;

	// panel dimensions
	private int pWidth, pHeight;

	private boolean isActive = true;
	// a sprite is updated and drawn only when it is active

	protected Rectangle myRect;
	
	/**
	 * Amount the sprite will move per update cycle
	 */
	protected Point velocity;
	/**
	 * Real position of the sprite in the world
	 */
	protected Point position;
	/**
	 *  image dimensions
	 */
	protected Point dimensions;
	
	protected Point pDimensions;

	public Sprite(int x, int y, int w, int h, ImagesLoader imsLd, String name) {
		this.position = new Point(x, y);

		this.velocity = new Point(XSTEP, YSTEP);

		this.pDimensions = new Point(w, h);

		this.imsLoader = imsLd;
		
		// the sprite's default image is 'name'
		setImage(name); 
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
			dimensions = new Point(SIZE, SIZE);
		} else {
			dimensions = new Point(image.getWidth(), image.getHeight());
		}
		//create bounding box
		this.myRect = new Rectangle(0, 0, dimensions.x, dimensions.y);
		
		// no image loop playing
		player = null;
		isLooping = false;
	}

	/**
	 * Switch on loop playing
	 * 
	 * @param animPeriod
	 *            - period length in ms (from the enclosing panel)
	 * @param seqDuration
	 *            - The total time for the loop to play the sequence
	 */
	public void loopImage(int animPeriod, double seqDuration) {
		if (imsLoader.numImages(imageName) > 1) {
			player = null; // to encourage garbage collection of previous player
			player = new ImagesPlayer(imageName, animPeriod, seqDuration, true,
					imsLoader);
			isLooping = true;
		} else {
			System.out.println(imageName + " is not a sequence of images");
		}
	}

	public void stopLooping() {
		if (isLooping) {
			player.stop();
			isLooping = false;
		}
	}

	public int getWidth() {
		return dimensions.x;
	}

	public int getHeight() {
		return dimensions.y;
	}

	public int getPWidth() {
		return this.pDimensions.x;
	}

	public int getPHeight() {
		return this.pDimensions.y;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean a) {
		isActive = a;
	}

	/**
	 * Forcefully sets the actual position of the sprite
	 * @param x
	 * @param y
	 */
	public void setPosition(int x, int y) {
		this.position.x = x;
		this.position.y = y;
	}

	/**
	 * Shifts the sprites position relatively from its current position
	 * @param xDist
	 * @param yDist
	 */
	public void translate(int xDist, int yDist) {
		this.position.translate(xDist, yDist);
	}

	public int getXPosn() {
		return this.position.x;
	}

	public int getYPosn() {
		return this.position.y;
	}

	/**
	 * Forcefully sets the sprite's movement velocity
	 * @param x - horizontal shift value
	 * @param y - vertical shift value
	 */
	public void setVelocity(int dx, int dy) {
		this.velocity.x = dx;
		this.velocity.y = dy;
	}

	public int getXVelocity() {
		return this.velocity.x;
	}

	public int getYVelocity() {
		return this.velocity.y;
	}

	/**
	 * @return the bounding box of the sprite
	 */
	public Rectangle getMyRectangle() {
		//set the rectangle's position only when requested
		myRect.x = this.position.x;
		myRect.y = this.position.y;
		
		return myRect;
	}

	/**
	 * Perform standard actions for the sprite on each cycle
	 */
	public void updateSprite()
	{
		if (isActive()) {
			this.position.translate(this.velocity.x, this.velocity.y);
			
			// update the animation
			if (isLooping)
			{
				player.updateTick();
			}
		}
	}

	/**
	 * Draws the sprite to the graphics context
	 * @param g
	 */
	public void drawSprite(Graphics g) {
		if (isActive()) {
			// if the sprite has no image, draw a yellow circle instead
			if (image == null) { 
				g.setColor(Color.yellow);
				g.fillOval(this.position.x, this.position.y, SIZE, SIZE);
				g.setColor(Color.black);
			} 
			else {
				if (isLooping)
					image = player.getCurrentImage();
				g.drawImage(image, this.position.x, this.position.y, null);
			}
		}
	}

}

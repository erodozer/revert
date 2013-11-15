package com.kgp.level;

// Ribbon.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* A ribbon manages an image which is wider than the game panel's
 width: width >= pWidth

 When a sprite is instructed to move left or right, the 
 sprite doesn't actually move, instead the ribbon moves in
 the _opposite_direction (right or left). The amount of movement 
 is specified in moveSize.

 The image is wrapped around the panel, so at a given moment
 the tail of the image, followed by its head may be visible 
 in the panel.

 A collection of ribbons are managed by a RibbonsManager object.
 */

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import revert.Entities.Actor.Movement;

public class Ribbon {
	private BufferedImage im;
	private float width; // the width of the image (>= pWidth)
	private float height;
	
	private float wScale, hScale;
	
	private int pWidth, pHeight; // dimensions of display panel

	private int moveSize; // size of the image move (in pixels)

	private int head;
	
	AffineTransform self;
	AffineTransform pre;
	AffineTransform post;
	

	/*
	 * The x-coord in the panel where the start of the image (its head) should
	 * be drawn. It can range between -width to width (exclusive), so can have a
	 * value beyond the confines of the panel (0-pWidth).
	 * 
	 * As xImHead varies, the on-screen ribbon will usually be a combination of
	 * its tail followed by its head.
	 */

	public Ribbon(int w, int h, BufferedImage im, int moveSz) {
		pWidth = w;
		pHeight = h;

		this.im = im;
		width = im.getWidth(); // no need to store the height
		if (width < pWidth)
			System.out.println("Ribbon width < panel width");

		head = 0;

		this.moveSize = moveSz;
		
		self = AffineTransform.getTranslateInstance(0, 0);
		pre = AffineTransform.getTranslateInstance(-width, 0);
		post = AffineTransform.getTranslateInstance(width, 0);
		
		height = Math.max(im.getHeight(), pHeight);
		width *= pHeight/height;
		wScale = pHeight/height;
		hScale = pHeight/height;
	} // end of Ribbon()

	/*
	 * Increment the xImHead value depending on the movement flags. It can range
	 * between -width to width (exclusive), which is the width of the image.
	 */
	public void update(Movement m)
	{

		//backgrounds move inversely from the player
		if (m == Movement.Right)
		{
			head -= moveSize;
		}
		else if (m == Movement.Left)
		{
			head += moveSize;
		}
		head %= width;
		
		self.setToTranslation(head, 0);
		pre.setToTranslation(head-width, 0);
		post.setToTranslation(head+width, 0);
		
		self.scale(wScale, hScale);
		pre.scale(wScale, hScale);
		post.scale(wScale, hScale);

		// System.out.println("xImHead is " + xImHead);
	} // end of update()

	public void display(Graphics2D g)
	/*
	 * Consider 5 cases: 
	 *   head > 0, when xImHead == 0, draw only the im head when xImHead >
	 * 0, draw the im tail and im head, or only the im tail. when xImHead < 0,
	 * draw the im tail, or the im tail and im head
	 * 
	 * xImHead can range between -width to width (exclusive)
	 */
	{
		//image right of the left edge
		if (head > 0)
		{
			//image past right edge, but the image is longer than the panel
			if (head > pWidth && head-width < 0)
			{
				g.drawImage(im, pre, null);
			}
			else
			{
				g.drawImage(im, self, null);
				g.drawImage(im, pre, null);
			}	
		}
		//image's tail is left of the panel's right edge
		else if (head+width < pWidth)
		{
			g.drawImage(im, self, null);
			g.drawImage(im, post, null);
		}
		//panel is contained within the image
		else
		{
			g.drawImage(im, self, null);
		}
	}

} // end of Ribbon

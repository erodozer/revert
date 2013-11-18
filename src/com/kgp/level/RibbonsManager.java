package com.kgp.level;

// RibbonsManager.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* RibbonsManager manages many ribbons (wraparound images 
 used for the game's background). 

 Ribbons 'further back' move slower than ones nearer the
 foreground of the game, creating a parallax distance effect.

 When a sprite is instructed to move left or right, the 
 sprite doesn't actually move, instead the ribbons move in
 the _opposite_direction (right or left).

 */

import java.awt.Graphics2D;
import java.util.ArrayList;

import revert.Entities.Actor.Movement;

import com.kgp.imaging.ImagesLoader;

public class RibbonsManager {
	/*
	 * Background ribbons images, and their movement factors. Ribbons 'further
	 * back' are specified first in ribImages[], and have smaller move factors
	 * so they will move slower.
	 */

	private ArrayList<Ribbon> ribbons;
	private float moveSize;

	private ImagesLoader imsLd;
	
	private int pWidth, pHeight;
	
	// standard distance for a ribbon to 'move' each tick

	public RibbonsManager(int w, int h, float f, ImagesLoader imsLd) {
		moveSize = f;
		// the basic move size is the same as the bricks ribbon

		pWidth = w;
		pHeight = h;
		this.imsLd = imsLd;
		
		ribbons = new ArrayList<Ribbon>();
	}
	
	public void add(String imgName, float mv)
	{
		ribbons.add(new Ribbon(pWidth, pHeight, imsLd.getImage(imgName), mv*moveSize));
	}

	public void update(Movement m) {
		for (int i = 0; i < ribbons.size(); i++)
			ribbons.get(i).update(m);
	}

	/*
	 * The display order is important. Display ribbons from the back to the
	 * front of the scene.
	 */
	public void display(Graphics2D g)
	{
		for (int i = 0; i < ribbons.size(); i++)
			ribbons.get(i).display(g);
	}

}


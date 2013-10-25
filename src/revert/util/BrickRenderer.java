package revert.util;

import java.awt.Graphics;
import java.awt.Point;

/**
 * Abstracted out Brick Renderer allows for constructing a visual world from
 * a Brick Manager
 * @author Nicholas Hydock
 *
 */
public interface BrickRenderer {

	/**
	 * Determines how to load the bricks from a file
	 * @param filename
	 */
	public void loadBricksFile(String filename);
	
	/**
	 * Translate world coordinates to map coordinates in this renderer's scale
	 * @param xWorld
	 * @param yWorld
	 * @return
	 */
	public Point worldToMap(int xWorld, int yWorld);
	
	/**
	 * Draws the bricks to the graphics context
	 * @param g
	 * @param xStart - left most brick to start drawing from
	 * @param xEnd - right most brick in range
	 * @param xBrick - 
	 */
	void drawBricks(Graphics g, int xStart, int xEnd, int xBrick);
	
	public void display(Graphics g);
}

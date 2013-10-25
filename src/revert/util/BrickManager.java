package revert.util;

import java.awt.Point;

/**
 * Interface for determining ways of managing bricks for a level
 * @author nh0311
 *
 */
public interface BrickManager {
	
	/**
	 * Determines how to load the bricks from a file
	 * @param filename
	 */
	public void loadBricksFile(String filename);
	
	/**
	 * Checks if a world coordinate is inside a brick
	 * @param xWorld
	 * @param yWorld
	 * @return boolean
	 */
	public boolean insideBrick(int xWorld, int yWorld);
	
	/**
	 * Finds the nearest floor location to fall to
	 * @param xWorld
	 * @param yWorld
	 */
	public Point floor(int xWorld, int yWorld);
	
	/**
	 * Finds the distance between the location and the floor
	 * @param xWorld
	 * @param yWorld
	 * @return
	 */
	public int distToFloor(int xWorld, int yWorld);
}

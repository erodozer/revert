package revert.util;

import java.awt.Graphics2D;
import java.awt.Point;

/**
 * Interface for determining ways of managing bricks for a level.
 * 
 * Based on the code from KGP
 * 
 * @author Nicholas Hydock 2013
 */
public abstract class BrickManager {
	
	/**
	 * Mask in column row order
	 */
	protected boolean[][] collisionMask;
	/**
	 * Bricks with tile id
	 */
	protected int[][] bricks;
	protected int numCols;
	protected int numRows;
	
	protected int yOffset;
	
	protected Point[] spawnPoints;
	
	/**
	 * Checks if a world coordinate is inside a brick
	 * @param xWorld
	 * @param yWorld
	 * @return boolean
	 */
	public boolean insideBrick(int x, int y, boolean real){
		
		Point loc;
		if (real){
			loc = worldToMap(x, y);
		}
		else{
			loc = new Point(x, y);
		}
		
		if (loc.x > -1 && loc.y > -1 && loc.x < numCols && loc.y < numRows)
			return collisionMask[loc.x][loc.y];
		return false;
	}
	
	/**
	 * convert world coord (x,y) to a map index tuple
	 * @return
	 */
	public Point worldToMap(float xWorld, float yWorld)
	{
		//System.out.println("World: " + xWorld + ", " + yWorld);

		int mapX = (int)(xWorld / (float)this.getBrickWidth());
		if (mapX < 0)
		{
			mapX += numCols;
		}
		else if (mapX >= numCols)
		{
			mapX -= numCols;
		}

		int mapY;
		if (yWorld < yOffset)
		{
			mapY = -1;
		}
		else if (yWorld > this.getRealHeight())
		{
			mapY = numRows - 1;
		}
		else
		{
			yWorld -= yOffset;
			mapY = (int)(yWorld/(float)this.getBrickHeight());
		}

		//System.out.println("Map: " + mapX + ", " + mapY);
		return new Point(mapX, mapY);
	}
	
	/**
	 * Translates a map point into a world coordinate
	 * @param p
	 * @return
	 */
	public Point mapToWorld(Point p)
	{
		Point world = new Point();
		world.x = p.x * this.getBrickWidth();
		world.y = p.y * this.getBrickHeight();
		world.y += this.yOffset;
		return world;
	}
	
	/**
	 * Finds the nearest floor location to fall to
	 * @param xWorld
	 * @param yWorld
	 * @param real - world coordinates or map coordinates
	 */
	public int findFloor(int x, int y, boolean real)
	{
		Point loc;
		if (real)
		{
			loc = worldToMap(x, y);
		}
		else
		{
			loc = new Point(x, y);
		}
		
		boolean b;
		for (int i = loc.y; i < numCols && loc.y == -1; i++) {
			b = collisionMask[loc.x][i];
			if (b)
				loc.y = i; // reduce locY (i.e. move up)
		}
		return loc.y * this.getBrickHeight() + yOffset;
		
	}

	/**
	 * @return the pixel width of a brick
	 */
	abstract public int getBrickWidth();
	/**
	 * @return the pixel height of a brick
	 */
	abstract public int getBrickHeight();
	
	/**
	 * @return the width of the tile map in tiles
	 */
	final public int getWidth() {
		return numCols;
	}
	
	/**
	 * @return the height of the tile map in tiles
	 */
	final public int getHeight() {
		return numRows;
	}
	
	/**
	 * @return the pixel width of the entire map
	 */
	final public int getMapHeight(){
		return numRows * this.getBrickHeight();
	}
	
	/**
	 * @return this pixel height of the entire map (only tiles counted, does not include panel salvage)
	 */
	final public int getMapWidth(){
		return numCols * this.getBrickWidth();
	}
	
	final public int getRealHeight() {
		return getMapHeight() + yOffset;
	}

	/**
	 * Used when the player is jumping.
	 * 
	 * Checks to see if their next step will be within a brick, and if it is 
	 * calculate the distance been them and the bottom of the brick so they can 
	 * bounce their head off properly.
	 */
	public int checkBrickBase(float xWorld, float yWorld, int step)
	{
		Point map = worldToMap(xWorld, yWorld - step);
		if (this.brickExists(map)) {
			Point world = mapToWorld(map);
			world.y += this.getBrickHeight();
			int distance = (int)(world.y - yWorld);
			
			return distance;
		}
		return step;
	}

	/**
	 * Used when the player is falling.
	 * 
	 * Checks to see if their next step will be within a brick, and if it is
	 * calculate the distance been them and the top of the brick so they can land properly.
	 */
	public int checkBrickTop(float xWorld, float yWorld, int step)
	{
		Point map = worldToMap(xWorld, yWorld + step);
		//System.out.println(map.y);
		if (this.brickExists(map))
		{
			Point world = mapToWorld(map);
			int distance = (int)(world.y - yWorld);
			return distance;	
		}
		return step;
	}
	
	/**
	 * @param loc - brick location in the map
	 * @return boolean indicating if a brick is located within this map
	 */
	public boolean brickExists(Point loc) {
		if (loc.x < 0 || loc.y < 0)
			return false;
		
		return this.collisionMask[loc.x][loc.y];
	}
	
	/**
	 * Renders the tilemap
	 * @param g - graphics context
	 */
	public abstract void display(Graphics2D g);
	
	final public Point[] getSpawnPoints()
	{
		return spawnPoints;
	}
}

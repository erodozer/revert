package revert.util;

import java.awt.Graphics2D;

import com.kgp.util.Vector2;

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

	protected Vector2[] spawnPoints;

	protected Vector2 currentPos = new Vector2();

	/**
	 * Checks if a world coordinate is inside a brick
	 * 
	 * @param xWorld
	 * @param yWorld
	 * @return boolean
	 */
	public boolean insideBrick(int x, int y, boolean real) {

		Vector2 loc;
		if (real) {
			loc = worldToMap(x, y);
		}
		else {
			loc = new Vector2(x, y);
		}

		if (loc.x > -1 && loc.y > -1 && loc.x < numCols && loc.y < numRows)
			return collisionMask[(int) loc.x][(int) loc.y];
		return false;
	}

	/**
	 * convert world coord (x,y) to a map index tuple
	 * 
	 * @return
	 */
	public Vector2 worldToMap(float xWorld, float yWorld) {
		// System.out.println("World: " + xWorld + ", " + yWorld);

		int mapX = (int) (xWorld / (float) this.getBrickWidth());
		if (mapX < 0) {
			mapX += numCols;
		}
		else if (mapX >= numCols) {
			mapX -= numCols;
		}

		int mapY;
		if (yWorld < yOffset) {
			mapY = -1;
		}
		else if (yWorld > this.getRealHeight()) {
			mapY = numRows - 1;
		}
		else {
			yWorld -= yOffset;
			mapY = (int) (yWorld / (float) this.getBrickHeight());
		}

		// System.out.println("Map: " + mapX + ", " + mapY);
		return new Vector2(mapX, mapY);
	}

	/**
	 * Translates a map point into a world coordinate
	 * 
	 * @param map
	 * @return
	 */
	public Vector2 mapToWorld(Vector2 map) {
		Vector2 world = new Vector2();
		world.x = map.x * this.getBrickWidth();
		world.y = map.y * this.getBrickHeight();
		world.y += yOffset;
		return world;
	}

	/**
	 * Finds the nearest floor location to fall to
	 * 
	 * @param xWorld
	 * @param yWorld
	 * @param real - world coordinates or map coordinates
	 */
	public float findFloor(int x, int y, boolean real) {
		Vector2 loc;
		if (real) {
			loc = worldToMap(x, y);
		}
		else {
			loc = new Vector2(x, y);
		}

		boolean b;
		for (int i = (int) loc.y; i < numCols && loc.y == -1; i++) {
			b = collisionMask[(int) loc.x][i];
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
	 * @return this pixel height of the entire map (only tiles counted, does not
	 *         include panel salvage)
	 */
	final public int getMapHeight() {
		return numRows * this.getBrickHeight();
	}

	/**
	 * @return the pixel width of the entire map
	 */
	final public int getMapWidth() {
		return numCols * this.getBrickWidth();
	}

	/**
	 * @return the pixel height of the image plus panel salvage
	 */
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
	public float checkBrickBase(float xWorld, float yWorld, float vertStep) {
		Vector2 map = worldToMap(xWorld, yWorld - vertStep);
		if (this.brickExists(map)) {
			Vector2 world = mapToWorld(map);
			world.y += this.getBrickHeight();
			int distance = (int) (yWorld - world.y);

			return distance;
		}
		return vertStep;
	}

	/**
	 * Used when the player is falling.
	 * 
	 * Checks to see if their next step will be within a brick, and if it is
	 * calculate the distance been them and the top of the brick so they can
	 * land properly.
	 */
	public float checkBrickTop(float xWorld, float yWorld, float vertStep) {
		Vector2 map = worldToMap(xWorld, yWorld + vertStep);
		// System.out.println(map.y);
		if (this.brickExists(map)) {
			System.out.println(map);
			Vector2 world = mapToWorld(map);
			int distance = (int) (yWorld - world.y);
			return distance;
		}
		return vertStep;
	}

	/**
	 * @param (map - brick location in the map
	 * @return boolean indicating if a brick is located within this map
	 */
	public boolean brickExists(Vector2 map) {
		if (map.x < 0 || map.y < 0)
			return false;

		return this.collisionMask[(int)map.x][(int)map.y];
	}

	/**
	 * Renders the tilemap
	 * 
	 * @param g - graphics context
	 */
	public abstract void display(Graphics2D g);

	final public Vector2[] getSpawnPoints() {
		return spawnPoints;
	}

	abstract protected void update();

	final public void update(Vector2 posn) {
		currentPos = posn.clone();
		update();
	}

	final public void update(float x, float y) {
		currentPos.x = x;
		currentPos.y = y;
		update();
	}
}

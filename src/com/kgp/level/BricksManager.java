package com.kgp.level;

// BricksManager.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* Loads and manages a wraparound bricks map. 
 It also deals with various collision detection tests from
 JumperSprite.

 A 'bricks map' is read in from a configuration file, and used
 to make an ArrayList of Brick objects, bricksList. 

 The collection of bricks defines a bricks map, which is moved
 and drawn in the same way as an image ribbon in a Ribbon object.

 The bricks map movement step is fixed by moveSize, which
 is some fraction of the width of a brick image.

 ----
 JumperSprite uses BricksManager for collision detection.
 As it rises/falls it must curtail the movement if it will
 enter a brick; checkBrickBase() and checkBrickTop() are used
 for testing this.

 When JumperSprite moves left/right, it must first check that it will
 not move into a brick. It uses insideBrick() for this test.

 When JumperSprite does move, it may move off into space -- there
 is no brick below it. It tests this using insideBrick() also.
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import revert.util.BrickManager;
import revert.util.BrickRenderer;

import com.kgp.imaging.ImagesLoader;

public class BricksManager implements BrickManager, BrickRenderer {
	private final static String BRICKS_DIR = "Levels/";
	private final static int MAX_BRICKS_LINES = 15;
	// maximum number of lines (rows) of bricks in the scene

	private final static double MOVE_FACTOR = 0.25;
	// modifies how fast the bricks map moves; smaller is slower

	private int pWidth, pHeight; // dimensions of display panel
	private int width, height; // max dimensions of bricks map
								// width > pWidth
	private int realHeight;
	private int yOffset;

	private int imWidth, imHeight; // dimensions of a brick image

	// number of bricks in the x- and y- dimensions
	private int numCols, numRows;

	private int moveSize;
	// size of the map move (in pixels) in each tick
	// moveSize == the width of a brick image * MOVE_FACTOR
	/* The ribbons and 'jack' use the same moveSize value. */

	private int xRange;
	private int left;
	private int right;
	
	//number of columns that can be visible at one time in the view
	private int visCols;
	
	private int[][] bricks;

	private ImagesLoader imsLoader;
	private ArrayList<BufferedImage> brickImages = null;

	public BricksManager(int w, int h, String fnm, ImagesLoader il) {
		pWidth = w;
		pHeight = h;
		imsLoader = il;

		bricks = loadBricksFile(fnm);
		
		for (int i = 0; i < bricks.length; i++)
		{
			for (int x = 0; x < bricks[i].length; x++)
			{
				if (bricks[i][x] > 0)
					System.out.print(bricks[i][x]);
				else
					System.out.print(" ");
			}
			System.out.println();
		}
		
		visCols = w/imWidth;
		
		System.out.printf("Map Width in Tiles: %d\nVisible Tiles: %d\n" , numCols, visCols);
		
		width = numCols * imWidth;
		height = Math.max(h, numRows * imHeight);

		System.out.printf("Brick Size: %d %d\n", imWidth, imHeight);
		
		realHeight = numRows * imHeight;
		yOffset = height - realHeight;
		
		moveSize = (int) (imWidth * MOVE_FACTOR);
		if (moveSize == 0) {
			System.out.println("moveSize cannot be 0, setting it to 1");
			moveSize = 1;
		}
	}

	// ----------- load the bricks information -------------------

	/**
	 * Load the bricks map from a configuration file, fnm. The map starts with
	 * an image strip which contains the images referred to in the map. Format:
	 * s <fnm> <number>
	 * 
	 * This means that the map can use images numbered 0 to <number-1>. We
	 * assume number is less than 10.
	 * 
	 * The bricks map follows. Each line is processed by storeBricks(). There
	 * can only be at most MAX_BRICKS_LINES lines.
	 * 
	 * The configuration file can contain empty lines and comment lines (those
	 * starting with //), which are ignored.
	 */
	public int[][] loadBricksFile(String fnm)
	{
		String imsFNm = BRICKS_DIR + fnm;
		System.out.println("Reading bricks file: " + imsFNm);

		ArrayList<Brick> bricksList = new ArrayList<Brick>();
		
		int numStripImages = -1;
		int numBricksLines = 0;
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream(
					imsFNm);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			char ch;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0) // ignore a blank line
					continue;
				if (line.startsWith("//")) // ignore a comment line
					continue;
				ch = Character.toLowerCase(line.charAt(0));
				if (ch == 's') // an images strip
				{
					brickImages = imsLoader.getStripImages(line);
					numStripImages = brickImages.size();
					imWidth = brickImages.get(0).getWidth();
					imHeight = brickImages.get(0).getHeight();
				}
				else { // a bricks map line
					if (numBricksLines > MAX_BRICKS_LINES)
						System.out.println("Max reached, skipping bricks line: " + line);
					else if (numStripImages == -1)
						System.out.println("No strip image, skipping bricks line: " + line);
					else {
						storeBricks(line, numBricksLines, numStripImages, bricksList);
						numBricksLines++;
					}
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Error reading file: " + imsFNm);
			System.exit(1);
		}
		
		numRows = numBricksLines;
		
		int[][] bricks = new int[numRows][numCols];
		
		for (Brick b : bricksList)
		{
			bricks[b.y][b.x] = b.type;
		}
		
		bricksList = null;
		
		return bricks;
	}

	private String getPrefix(String fnm)
	// extract name before '.' of filename
	{
		int posn;
		if ((posn = fnm.lastIndexOf(".")) == -1) {
			System.out.println("No prefix found for filename: " + fnm);
			return fnm;
		} else
			return fnm.substring(0, posn);
	}

	private void storeBricks(String line, int lineNo, int numImages, ArrayList<Brick> bricksList)
	/*
	 * Read a single bricks line, and create Brick objects. A line contains
	 * digits and spaces (which are ignored). Each digit becomes a Brick object.
	 * The collection of Brick objects are stored in the bricksList ArrayList.
	 */
	{
		if (line.length() > numCols)
		{
			numCols = line.length();
		}
		
		int imageID;
		for (int x = 0; x < line.length(); x++) {
			char ch = line.charAt(x);
			if (ch == ' ') // ignore a space
				continue;
			if (Character.isDigit(ch)) {
				imageID = ch - '0'; // we assume a digit is 0-9
				if (imageID >= numImages)
					System.out.println("Image ID " + imageID + " out of range");
				else
					// make a Brick object
					bricksList.add(new Brick(imageID+1, x, lineNo));
			} else
				System.out.println("Brick char " + ch + " is not a digit");
		}
	} // end of storeBricks()

	// --------------- initialise bricks data structures -----------------

	/*
	 * Check that the bottom map line (numRows-1) has a brick in every x
	 * position from 0 to numCols-1. This prevents 'jack' from falling down a
	 * hole at the bottom of the panel.
	 */
	private void checkForGaps()
	{
		Brick b;
		for (int c = 0; c < bricks[bricks.length-1].length; c++)
		{
			int i = bricks[bricks.length-1][c];
			boolean gap = false;
			if (i == 0)
			{
				gap = true;
				for (int r = bricks[bricks.length-1][c]; r >= 0 && gap; r--)
				{
					if (bricks[r][c] > 0)
					{
						gap = false;
					}
				}
			}
			if (gap)
			{
				System.out.println("Gap found in bricks map bottom line at position " + c);
				System.exit(-1);
			}
		}
	}

	public void update(Point position)
	{
		xRange = position.x;
		left = (int)((xRange - pWidth/2f) / (float)imWidth);
		right = (int)Math.ceil((xRange + pWidth/2f) / (float)imWidth);
		
		System.out.printf("Vis Range: %d\nLeft/Right: %d %d\n", visCols, left, right);
	}

	// -------------- draw the bricks ----------------------

	/*
	 * The bricks map (bm) is wider than the panel (width >= pWidth) Consider 4
	 * cases: when xMapHead >= 0, draw the bm tail and bm start, or only the bm
	 * tail. when xMapHead < 0, draw the bm tail, or the bm tail and bm start
	 * 
	 * xMapHead can range between -width to width (exclusive)
	 */
	public void display(Graphics g)
	{
		drawBricks(g, left, right);
	}

	// ----------------- JumperSprite related methods -------------
	// various forms of collision detection with the bricks

	public int getBrickHeight() {
		return imHeight;
	}

	/*
	 * Called at sprite initialisation to find a brick containing the xSprite
	 * location which is higher up than other bricks containing that same
	 * location. Return the brick's y position.
	 * 
	 * xSprite is the same coordinate in the panel and the bricks map since the
	 * map has not moved yet.
	 * 
	 * xSprite is converted to an x-index in the brick map, and this is used to
	 * search the relevant bricks column for a max y location.
	 * 
	 * The returned y-location is the 'floor' of the bricks where the sprite
	 * will be standing initially.
	 */
	public int findFloor(int xSprite)
	{
		int xMap = (int) (xSprite / imWidth); // x map index
		int locY = (int) 0; // starting y position (the largest possible)
		
		int b;
		for (int i = locY; i < numCols && locY == 0; i++) {
			b = bricks[i][xMap];
			if (b > 0)
				locY = i; // reduce locY (i.e. move up)
		}
		return locY*imHeight + yOffset;
	}

	public int getMoveSize() {
		return moveSize;
	}

	/**
	 * Check if the world coord is inside a brick. 
	 */
	public boolean insideBrick(int xWorld, int yWorld)
	{
		Point mapCoord = worldToMap(xWorld, yWorld);
		if (mapCoord.y > -1 && mapCoord.x > -1)
			if (bricks[mapCoord.y][mapCoord.x] > 0)
				return true;
		return false;
	}
	
	/**
	 * Check if the world coord is inside a brick. 
	 */
	public boolean insideBrick(Point mapCoord)
	{
		if (mapCoord.y > -1 && mapCoord.x > -1)
			if (bricks[mapCoord.y][mapCoord.x] > 0)
				return true;
		return false;
	}

	/**
	 * convert world coord (x,y) to a map index tuple
	 * @param xWorld
	 * @param yWorld
	 * @return
	 */
	public Point worldToMap(int xWorld, int yWorld)
	{
		//System.out.println("World: " + xWorld + ", " + yWorld);

		int mapX = (int)(xWorld / (float)imWidth);
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
		else if (yWorld > height)
		{
			mapY = numRows - 1;
		}
		else
		{
			yWorld -= yOffset;
			mapY = (int)(yWorld/(float)imHeight);
		}

		//System.out.println("Map: " + mapX + ", " + mapY);
		return new Point(mapX, mapY);
	} // end of worldToMap()

	public Point mapToWorld(Point p)
	{
		Point world = new Point();
		world.x = p.x * imWidth;
		world.y = p.y * imHeight;
		world.y += yOffset;
		return world;
	}
	
	/*
	 * The sprite is moving upwards. It checks its next position (xWorld,
	 * yWorld) to see if it will enter a brick from below.
	 * 
	 * If it does, then its step value is reduced to smallStep so it will only
	 * rise to touch the base of the brick.
	 */
	public int checkBrickBase(int xWorld, int yWorld, int step)
	{
		Point map = worldToMap(xWorld, yWorld);
		if (insideBrick(map)) {
			Point world = mapToWorld(map);
			world.y += imHeight;
			int distance = world.y - (yWorld - step);
			
			return distance;
		}
		return step;
	} // end of checkBrickBase()

	/*
	 * The sprite is moving downwards. It checks its next position (xWorld,
	 * yWorld) to see if it will enter a brick from above.
	 * 
	 * If it does, then its step value is reduced to smallStep so it will only
	 * drop enough to touch the top of the brick.
	 */
	public int checkBrickTop(int xWorld, int yWorld, int step)
	{
		Point map = worldToMap(xWorld, yWorld);
		if (insideBrick(map)) {
			Point world = mapToWorld(map);
			System.out.println("tile loc: " + world.y);
			int distance = world.y - (yWorld - step);
			System.out.printf("Step: %d\nTravel: %d\n", step, distance);
			return distance;
		}
		return step;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	@Override
	public void drawBricks(Graphics g, int left, int right) {

		for (int i = left, loc = left, x = left * imWidth + (xRange % imWidth); i <= right; i++, loc++, x += imWidth)
		{
			if (loc < 0)
			{
				loc = numCols-1-(Math.abs(loc) % numCols);
			}
			else if (loc >= numCols)
			{
				loc %= numCols;
			}
			
			for (int j = 0; j < numRows; j++)
			{
				if (bricks[j][loc]-1 >= 0)
				{
					g.drawImage(brickImages.get(bricks[j][loc]-1), x, yOffset + (j-1)*imHeight, null);
				}
			}
		}
	}

	@Override
	public int distToFloor(int xWorld, int yWorld) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Point floor(int xWorld, int yWorld) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class Brick
	{
		int type, x, y;
		
		public Brick(int t, int x, int y)
		{
			this.type = t;
			this.x = x;
			this.y = y;
		}
	}

} // end of BricksManager

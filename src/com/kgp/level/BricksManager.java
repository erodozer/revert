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

import revert.util.BrickManager;

import com.kgp.imaging.ImagesLoader;

public class BricksManager extends BrickManager  {
	private final static String BRICKS_DIR = "Levels/";
	private final static int MAX_BRICKS_LINES = 15;
	// maximum number of lines (rows) of bricks in the scene

	private final static double MOVE_FACTOR = 0.25;
	// modifies how fast the bricks map moves; smaller is slower

	private int pWidth, pHeight; // dimensions of display panel
	private int width, height;   // max dimensions of bricks map
								 // width > pWidth

	private int imWidth, imHeight; // dimensions of a brick image

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

		loadBricksFile(fnm);
		
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
		
		yOffset = height - this.getMapHeight();
		
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
	public void loadBricksFile(String fnm)
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
		
		bricks = new int[numRows][numCols];
		collisionMask = new boolean[numCols][numRows];
		
		for (Brick b : bricksList)
		{
			bricks[b.y][b.x] = b.type;
			if (b.type > 0)
			{
				collisionMask[b.x][b.y] = true;
			}
		}
		
		bricksList = null;
		
		checkForGaps();
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
		
		//System.out.printf("Vis Range: %d\nLeft/Right: %d %d\n", visCols, left, right);
	}

	// -------------- draw the bricks ----------------------

	// ----------------- JumperSprite related methods -------------
	// various forms of collision detection with the bricks

	@Override
	public int getBrickHeight() {
		return imHeight;
	}
	

	@Override
	public int getBrickWidth() {
		return imWidth;
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

	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void display(Graphics2D g) {
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

}

package revert.util;

import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Parses a CSV style Tiled Map XML File into a collision mask for levels
 * @author Nicholas Hydock
 */
public class TMXBricksManager implements BrickManager, BrickRenderer {

	private static String TMX_DIR = "assets/levels/";
	
	private int stepX = 32;
	private int stepY = 32;

	/**
	 * Mask showing the impassability of the tile map.
	 * Read as 
	 * 	col x row
	 * 
	 * Passible tiles are marked with 1 (false)
	 * Impassible are 2 (true)
	 */
	Boolean[][] collisionMask;

	@Override
	public void loadBricksFile(String filename) {
		try
		{
			File fXmlFile = new File(TMX_DIR + filename + ".tmx");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
	
			loadBricksData(doc.createElement("map"));
		}
		catch (IOException e) 
	    { 	
			System.out.println("Error loading level file: " + filename + ".tmx");
			System.exit(1);
	    } catch (ParserConfigurationException e) {
	    	System.out.println("Error parsing level file: " + filename + ".tmx");
	    	e.printStackTrace();
	    	System.exit(1);
		} catch (SAXException e) {
			System.out.println("Error parsing level file: " + filename + ".tmx");
	    	e.printStackTrace();
	    	System.exit(1);
		}
	}
	
	/**
	 * Parse out the data from the nodes in the TMX file
	 * @param xmlNode
	 */
	public void loadBricksData(Element xmlNode)
	{
		int width = Integer.parseInt(xmlNode.getAttribute("width"));
		int height = Integer.parseInt(xmlNode.getAttribute("height"));
		
		collisionMask = new Boolean[height][width];
		
		Element tileset = (Element)xmlNode.getElementsByTagName("tileset").item(0);
		Element tiles = (Element)xmlNode.getElementsByTagName("layer").item(0).getFirstChild();
		
		stepX = Integer.parseInt(tileset.getAttribute("tilewidth"));
		stepY = Integer.parseInt(tileset.getAttribute("tileheight"));
		
		String[] tData = tiles.getTextContent().split("/[, ]+/");
		
		for (int i = 0, r = 0, c = 0; r < height; c = 0, r++)
		{
			for (;c < width; c++, i++)
			{
				collisionMask[c][r] = (Integer.parseInt(tData[i]) == 2) ? true : false;
			}
		}
	}

	@Override
	public boolean insideBrick(int xWorld, int yWorld) {
		return collisionMask[xWorld][yWorld];
	}
	
	public Point worldToMap(int xWorld, int yWorld) {
		return new Point(xWorld*stepX, yWorld*stepY);
	}

	@Override
	public void drawBricks(Graphics g, int xStart, int xEnd, int xBrick) {
		// Ignore
		// TMX files are just being used for collision, do not draw with them
	}

	@Override
	public void display(Graphics g) {
		// Ignore
		// TMX files are just being used for collision, do not draw with them
	}

	@Override
	public Point floor(int xWorld, int yWorld) {
		int y = yWorld;
		
		while (!collisionMask[xWorld][y])
			y++;
		
		return new Point(xWorld, y);
	}

	@Override
	public int distToFloor(int xWorld, int yWorld) {
		int y = yWorld;
		
		while (!collisionMask[xWorld][y])
			y++;
		
		return y-yWorld;
	}
	
	
}
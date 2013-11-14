package revert.util;

import java.awt.Graphics2D;
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
public class TMXBricksManager extends BrickManager {

	private static String TMX_DIR = "assets/levels/";
	
	private int stepX = 32;
	private int stepY = 32;

	/**
	 * Parses the TMX file into BrickManager data
	 * @param filename
	 */
	public void loadBricksFile(String filename) {
		try
		{
			File fXmlFile = new File(TMX_DIR + filename + ".tmx");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
	
			loadBricksData(doc.getElementById("map"));
			loadSpawnPoints(doc.getElementById("shapes"));
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
	 * Loads the spawn points in the map by looking at the points in a single polygon
	 * in the map.
	 * @param shape - DOM node holding the data for the spawn points
	 */
	private void loadSpawnPoints(Element shape) {
	
		String[] points = shape.getAttribute("points").split("/[ ,]+/");
		
		spawnPoints = new Point[points.length/2];
		for (int i = 0, n = 0; i < points.length; i += 2, n++)
		{
			int x, y;
			x = Integer.parseInt(points[i]);
			y = Integer.parseInt(points[i+1]);
			Point p = new Point(x, y);
			spawnPoints[n] = p;
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
		
		collisionMask = new boolean[width][height];
		
		Element tileset = (Element)xmlNode.getElementsByTagName("tileset").item(0);
		Element tiles = (Element)xmlNode.getElementsByTagName("layer").item(0).getFirstChild();
		
		stepX = Integer.parseInt(tileset.getAttribute("tilewidth"));
		stepY = Integer.parseInt(tileset.getAttribute("tileheight"));
		
		String[] tData = tiles.getTextContent().split("/[, ]+/");
		
		for (int i = 0, r = 0, c = 0; r < height; c = 0, r++)
		{
			for (;c < width; c++, i++)
			{
				boolean brick = (Integer.parseInt(tData[i]) == 2) ? true : false;;
				collisionMask[c][r] = brick;
				bricks[c][r] = (brick) ? 1 : 0;
			}
		}
	}

	@Override
	public void display(Graphics2D g) {
		// Ignore
		// TMX files are just being used for collision, do not draw with them
	}

	@Override
	public int getBrickWidth() {
		return stepX;
	}

	@Override
	public int getBrickHeight() {
		return stepY;
	}
	
	
}
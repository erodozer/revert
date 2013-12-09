package com.kgp.imaging;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** 
 * The Imagesfile and images are stored in "Images/"
 * (the IMAGE_DIR constant).
 * 
 * ImagesFile Formats:
 * <p>
 *  a single image file
 *		<br><b>o {fnm}</b></br>
 * </p>
 * <p>
 * a series of numbered image files, whose filenames use the numbers 0 - {number>}-1
 * 		<br><b>n {fnm*.ext} {number}</b></br>
 * </p>
 * <p>
 * a strip file {fnm} containing a single row of {number} images
 * 		<br><b>s {fnm} {number}</b></br>
 * </p>
 * a group of files with different names; they are accessible via <name> and position _or_ <fnm> prefix   
 * 		g {name} {fnm} [ {fnm} ]*
 * <br/>
 * and blank lines and comment lines.
 * <p/>
 * The numbered image files (n) can be accessed by the {fnm} prefix and {number}. 
 * <p/>
 * The strip file images can be accessed by the {fnm} prefix and their position inside the file 
 * (which is assumed to hold a single row of images).
 * <p/>
 * The images in group files can be accessed by the 'g' {name} and the {fnm} prefix 
 * of the particular file, or its position in the group.
 * <p/>
 * The images are stored as BufferedImage objects, so they will be 
 * manipulated as 'managed' images by the JVM (when possible).
 * 
 * @author Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
 * @author Nicholas Hydock, Nov 2013, nhydock@gmail.com
 */
public class ImagesLoader {
	private final static String IMAGE_DIR = "Images/";

	/*
	 * The key is the filename prefix, the object (value) is an ArrayList of
	 * BufferedImages
	 */
	private HashMap<String, ArrayList<BufferedImage>> imagesMap;
	
	/*
	 * The key is the 'g' <name> string, the object is an ArrayList of filename
	 * prefixes for the group. This is used to access a group image by its 'g'
	 * name and filename.
	 */
	private HashMap<String, ArrayList<String>> gNamesMap;	

	private GraphicsConfiguration gc;

	/**
	 * begin by loading the images specified in fnm
	 * @param fnm
	 */
	public ImagesLoader(String fnm)
	{
		initLoader();
		loadImagesFile(fnm);
	}
	
	public ImagesLoader() {
		initLoader();
	}

	private void initLoader() {
		this.imagesMap = new HashMap<String, ArrayList<BufferedImage>>();
		this.gNamesMap = new HashMap<String, ArrayList<String>>();

		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		this.gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
	}
	
	/**
	 * Formats: o <fnm> // a single image n <fnm*.ext> <number> // a numbered
	 * sequence of images s <fnm> <number> // an images strip g <name> <fnm> [
	 * <fnm> ]* // a group of images
	 * 
	 * and blank lines and comment lines.
	 */
	private void loadImagesFile(String fnm)
	{
		String imsFNm = IMAGE_DIR + fnm;
		System.out.println("Reading file: " + imsFNm);
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream(imsFNm);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			// BufferedReader br = new BufferedReader( new FileReader(imsFNm));
			String line;
			char ch;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0) // blank line
					continue;
				if (line.startsWith("//")) // comment
					continue;
				ch = Character.toLowerCase(line.charAt(0));
				if (ch == 'o') // a single image
					getFileNameImage(line);
				else if (ch == 'n') // a numbered sequence of images
					getNumberedImages(line);
				else if (ch == 's') // an images strip
				{
					if (line.startsWith("s2"))
						getStrip2DImages(line);
					else
						getStripImages(line);
				} else if (ch == 'g') // a group of images
					getGroupImages(line);
				else
					System.out.println("Do not recognize line: " + line);
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Error reading file: " + imsFNm);
			System.exit(1);
		}
	}
	
	/**
	 * format: o {filename}
	 */
	private void getFileNameImage(String line)
	{
		StringTokenizer tokens = new StringTokenizer(line);

		if (tokens.countTokens() != 2)
			System.out.println("Wrong no. of arguments for " + line);
		else {
			tokens.nextToken(); // skip command label
			System.out.print("o Line: ");
			loadSingleImage(tokens.nextToken());
		}
	} 
	
	public boolean loadSingleImage(String fnm)
	{
		String name = getPrefix(fnm);

		if (imagesMap.containsKey(name)) {
			System.out.println("Error: " + name + "already used");
			return false;
		}

		BufferedImage bi = loadImage(fnm);
		if (bi != null) {
			ArrayList<BufferedImage> imsList = new ArrayList<BufferedImage>();
			imsList.add(bi);
			imagesMap.put(name, imsList);
			System.out.println("  Stored " + name + "/" + fnm);
			return true;
		} else
			return false;
	}

	/**
	 * extract name before '.' of filename
	 * @param fnm
	 * @return
	 */
	private String getPrefix(String fnm)
	{
		int posn;
		if ((posn = fnm.lastIndexOf(".")) == -1) {
			System.out.println("No prefix found for filename: " + fnm);
			return fnm;
		} else
			return fnm.substring(0, posn);
	}
	
	/**
	 * format: n {fnm*.ext} {number}
	 */
	private void getNumberedImages(String line)
	{
		StringTokenizer tokens = new StringTokenizer(line);

		if (tokens.countTokens() != 3)
			System.out.println("Wrong no. of arguments for " + line);
		else {
			tokens.nextToken(); // skip command label
			System.out.print("n Line: ");

			String fnm = tokens.nextToken();
			int number = -1;
			try {
				number = Integer.parseInt(tokens.nextToken());
			} catch (Exception e) {
				System.out.println("Number is incorrect for " + line);
			}

			loadNumImages(fnm, number);
		}
	}

	/**
	 * Can be called directly. {fnm} is the filename argument in: n <f*.ext>
	 * {number}
	 */
	public int loadNumImages(String fnm, int number)
	{
		String prefix = null;
		String postfix = null;
		int starPosn = fnm.lastIndexOf("*"); // find the '*'
		if (starPosn == -1) {
			System.out.println("No '*' in filename: " + fnm);
			prefix = getPrefix(fnm);
		} else { // treat the fnm as prefix + "*" + postfix
			prefix = fnm.substring(0, starPosn);
			postfix = fnm.substring(starPosn + 1);
		}

		if (imagesMap.containsKey(prefix)) {
			System.out.println("Error: " + prefix + "already used");
			return 0;
		}

		return loadNumImages(prefix, postfix, number);
	} // end of loadNumImages()

	/**
	 * Load a series of image files with the filename format prefix + {i} +
	 * postfix where i ranges from 0 to number-1
	 */
	private int loadNumImages(String prefix, String postfix, int number)
	{
		String imFnm;
		BufferedImage bi;
		ArrayList<BufferedImage> imsList = new ArrayList<BufferedImage>();
		int loadCount = 0;

		if (number <= 0) {
			System.out.println("Error: Number <= 0: " + number);
			imFnm = prefix + postfix;
			if ((bi = loadImage(imFnm)) != null) {
				loadCount++;
				imsList.add(bi);
				System.out.println("  Stored " + prefix + "/" + imFnm);
			}
		} else { // load prefix + <i> + postfix, where i = 0 to <number-1>
			System.out.print("  Adding " + prefix + "/" + prefix + "*"
					+ postfix + "... ");
			for (int i = 0; i < number; i++) {
				imFnm = prefix + i + postfix;
				if ((bi = loadImage(imFnm)) != null) {
					loadCount++;
					imsList.add(bi);
					System.out.print(i + " ");
				}
			}
			System.out.println();
		}

		if (loadCount == 0)
			System.out.println("No images loaded for " + prefix);
		else
			imagesMap.put(prefix, imsList);

		return loadCount;
	}
	
	/**
	 * format: s {filename} {number}
	 */
	public ArrayList<BufferedImage> getStripImages(String line)
	{
		Scanner reader = new Scanner(line);

		reader.next();
		System.out.print("s Line: ");

		String fnm = reader.next();
		int number = reader.nextInt();
		String name = loadStripImages(fnm, number);

		reader.close();

		if (name != null)
			return this.imagesMap.get(name);
		return null;
	}
	
	/**
	 * Loads a 2D Array formatted Strip Image
	 * <p/>
	 * format: s2 {filename} {row} {col}
	 */
	public ArrayList<BufferedImage> getStrip2DImages(String line)
	{
		Scanner reader = new Scanner(line);
		// skip command at the beginning
		reader.next();
		System.out.print("s Line: ");

		String fnm = reader.next();
		int rows = reader.nextInt();
		int cols = reader.nextInt();
		String name = loadStripImages(fnm, rows, cols);

		reader.close();

		if (name != null)
			return this.imagesMap.get(name);
		return null;
	}

	/**
	 * Can be called directly, to load a strip file, {fnm}, holding {number}
	 * images.
	 */
	public String loadStripImages(String fnm, int number)
	{
		String name = getPrefix(fnm);
		if (imagesMap.containsKey(name)) {
			System.out.println("Error: " + name + "already used");
			return null;
		}

		// load the images into an array
		BufferedImage[] strip = null;
		strip = loadStripImageArray(fnm, number);
		if (strip == null)
			return null;

		ArrayList<BufferedImage> imsList = new ArrayList<BufferedImage>();
		int loadCount = 0;
		System.out.print("  Adding " + name + "/" + fnm + "... ");
		for (int i = 0; i < strip.length; i++) {
			loadCount++;
			imsList.add(strip[i]);
			System.out.print(i + " ");
		}
		System.out.println();

		if (loadCount == 0)
			System.out.println("No images loaded for " + name);
		else
			imagesMap.put(name, imsList);

		return name;
	}

	/**
	 * Loads a 2D strip image
	 * 
	 * @param fnm
	 *            - filename
	 * @param row
	 *            - number of rows
	 * @param col
	 *            - number of columns
	 * @return name of the file loaded
	 */
	public String loadStripImages(String fnm, int row, int col)
	{
		String name = getPrefix(fnm);
		if (imagesMap.containsKey(name)) {
			System.out.println("Error: " + name + "already used");
			return null;
		}

		// load the images into an array
		BufferedImage[] strip = null;
		strip = loadStripImageArray(fnm, row, col);
		if (strip == null)
			return null;

		ArrayList<BufferedImage> imsList = new ArrayList<BufferedImage>();
		int loadCount = 0;
		System.out.print("  Adding " + name + "/" + fnm + "... ");
		for (int i = 0; i < strip.length; i++) {
			loadCount++;
			imsList.add(strip[i]);
			System.out.print(i + " ");
		}
		System.out.println();

		if (loadCount == 0)
			System.out.println("No images loaded for " + name);
		else
			imagesMap.put(name, imsList);

		return name;
	}

	/**
	 * format: g <name> <fnm> [ <fnm> ]*
	 */
	private void getGroupImages(String line)
	{
		StringTokenizer tokens = new StringTokenizer(line);

		if (tokens.countTokens() < 3)
			System.out.println("Wrong no. of arguments for " + line);
		else {
			tokens.nextToken(); // skip command label
			System.out.print("g Line: ");

			String name = tokens.nextToken();

			ArrayList<String> fnms = new ArrayList<String>();
			fnms.add(tokens.nextToken()); // read filenames
			while (tokens.hasMoreTokens())
				fnms.add(tokens.nextToken());

			loadGroupImages(name, fnms);
		}
	}

	/**
	 * Can be called directly to load a group of images, whose filenames are
	 * stored in the ArrayList {fnms}. They will be stored under the 'g' name
	 * {name}.
	 */
	public int loadGroupImages(String name, ArrayList<String> fnms)
	{
		if (imagesMap.containsKey(name)) {
			System.out.println("Error: " + name + "already used");
			return 0;
		}

		if (fnms.size() == 0) {
			System.out.println("List of filenames is empty");
			return 0;
		}

		BufferedImage bi;
		ArrayList<String> nms = new ArrayList<String>();
		ArrayList<BufferedImage> imsList = new ArrayList<BufferedImage>();
		String nm, fnm;
		int loadCount = 0;

		System.out.println("  Adding to " + name + "...");
		System.out.print("  ");
		for (int i = 0; i < fnms.size(); i++) { // load the files
			fnm = (String) fnms.get(i);
			nm = getPrefix(fnm);
			if ((bi = loadImage(fnm)) != null) {
				loadCount++;
				imsList.add(bi);
				nms.add(nm);
				System.out.print(nm + "/" + fnm + " ");
			}
		}
		System.out.println();

		if (loadCount == 0)
			System.out.println("No images loaded for " + name);
		else {
			imagesMap.put(name, imsList);
			gNamesMap.put(name, nms);
		}

		return loadCount;
	}

	/**
	 * supply the group filenames in an array
	 * @param name
	 * @param fnms
	 * @return
	 */
	public int loadGroupImages(String name, String[] fnms)
	{
		ArrayList<String> al = new ArrayList<String>(Arrays.asList(fnms));
		return loadGroupImages(name, al);
	}

	/**
	 * Get the image associated with <name>. If there are several images stored
	 * under that name, return the first one in the list.
	 */
	public BufferedImage getImage(String name)
	{
		ArrayList<BufferedImage> imsList = imagesMap.get(name);
		if (imsList == null) {
			System.out.println("No image(s) stored under " + name);
			return null;
		}

		// System.out.println("Returning image stored under " + name);
		return (BufferedImage) imsList.get(0);
	} // end of getImage() with name input;

	/**
	 * Get the image associated with {name} at position {posn} in its list. If
	 * {posn} is < 0 then return the first image in the list. If {posn} is
	 * bigger than the list's size, then calculate its value modulo the size.
	 */
	public BufferedImage getImage(String name, int posn)
	{
		ArrayList<BufferedImage> imsList = imagesMap.get(name);
		if (imsList == null) {
			System.out.println("No image(s) stored under " + name);
			return null;
		}

		int size = imsList.size();
		if (posn < 0) {
			// System.out.println("No " + name + " image at position " + posn +
			// "; return position 0");
			return (BufferedImage) imsList.get(0); // return first image
		} else if (posn >= size) {
			// System.out.println("No " + name + " image at position " + posn);
			int newPosn = posn % size; // modulo
			// System.out.println("Return image at position " + newPosn);
			return (BufferedImage) imsList.get(newPosn);
		}

		// System.out.println("Returning " + name + " image at position " +
		// posn);
		return (BufferedImage) imsList.get(posn);
	}

	/**
	 * Get the image associated with the group {name} and filename prefix
	 * {fnmPrefix}.
	 */
	public BufferedImage getImage(String name, String fnmPrefix)

	{
		ArrayList<BufferedImage> imsList = imagesMap.get(name);
		if (imsList == null) {
			System.out.println("No image(s) stored under " + name);
			return null;
		}

		int posn = getGroupPosition(name, fnmPrefix);
		if (posn < 0) {
			// System.out.println("Returning image at position 0");
			return imsList.get(0); // return first image
		}

		// System.out.println("Returning " + name +
		// " image with pair name " + fnmPrefix);
		return imsList.get(posn);
	}

	/**
	 * Search the hashmap entry for <name>, looking for <fnmPrefix>. Return its
	 * position in the list, or -1.
	 */
	private int getGroupPosition(String name, String fnmPrefix)

	{
		ArrayList<String> groupNames = gNamesMap.get(name);
		if (groupNames == null) {
			System.out.println("No group names for " + name);
			return -1;
		}

		String nm;
		for (int i = 0; i < groupNames.size(); i++) {
			nm = (String) groupNames.get(i);
			if (nm.equals(fnmPrefix))
				return i; // the posn of <fnmPrefix> in the list of names
		}

		System.out.println("No " + fnmPrefix + " group name found for " + name);
		return -1;
	}

	/**
	 * fetch all the BufferedImages for the given name
	 * 
	 * @param name
	 * @return
	 */
	public ArrayList<BufferedImage> getImages(String name) {
		ArrayList<BufferedImage> imsList = imagesMap.get(name);
		if (imsList == null) {
			System.out.println("No image(s) stored under " + name);
			return null;
		}

		System.out.println("Returning all images stored under " + name);
		return imsList;
	}

	/**
	 * is {name} a key in the imagesMap hashMap?
	 * 
	 * @param name
	 * @return
	 */
	public boolean isLoaded(String name) {
		ArrayList<BufferedImage> imsList = (ArrayList<BufferedImage>) imagesMap
				.get(name);
		if (imsList == null)
			return false;
		return true;
	}

	/**
	 * how many images are stored under {name}?
	 * 
	 * @param name
	 * @return
	 */
	public int numImages(String name) {
		ArrayList<BufferedImage> imsList = (ArrayList<BufferedImage>) imagesMap
				.get(name);
		if (imsList == null) {
			System.out.println("No image(s) stored under " + name);
			return 0;
		}
		return imsList.size();
	}

	/**
	 * Load the image from <fnm>, returning it as a BufferedImage which is
	 * compatible with the graphics device being used. Uses ImageIO.
	 */
	public BufferedImage loadImage(String fnm) {
		try {
			BufferedImage im = ImageIO.read(getClass().getClassLoader()
					.getResource(IMAGE_DIR + fnm));
			// An image returned from ImageIO in J2SE <= 1.4.2 is
			// _not_ a managed image, but is after copying!

			int transparency = im.getColorModel().getTransparency();
			BufferedImage copy = gc.createCompatibleImage(im.getWidth(),
					im.getHeight(), transparency);
			// create a graphics context
			Graphics2D g2d = copy.createGraphics();
			// g2d.setComposite(AlphaComposite.Src);

			// reportTransparency(IMAGE_DIR + fnm, transparency);

			// copy image
			g2d.drawImage(im, 0, 0, null);
			g2d.dispose();
			return copy;
		} catch (IOException e) {
			System.out.println("Load Image error for " + IMAGE_DIR + "/" + fnm
					+ ":\n" + e);
			return null;
		}
	}

	/**
	 * make a BufferedImage copy of im, assuming an alpha channel
	 * 
	 * @param im
	 * @param width
	 * @param height
	 * @return
	 */
	private BufferedImage makeBIM(Image im, int width, int height) {
		BufferedImage copy = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		// create a graphics context
		Graphics2D g2d = copy.createGraphics();
		// g2d.setComposite(AlphaComposite.Src);

		// copy image
		g2d.drawImage(im, 0, 0, null);
		g2d.dispose();
		return copy;
	}

	/**
	 * Load the image from <fnm>, returning it as a BufferedImage. Use Image.
	 */
	public BufferedImage loadImage3(String fnm) {
		Image im = readImage(fnm);
		if (im == null)
			return null;

		int width = im.getWidth(null);
		int height = im.getHeight(null);

		return makeBIM(im, width, height);
	}

	/**
	 * load the image, waiting for it to be fully downloaded
	 * 
	 * @param fnm
	 * @return
	 */
	private Image readImage(String fnm) {
		Image image = Toolkit.getDefaultToolkit().getImage(
				getClass().getClassLoader().getResource(IMAGE_DIR + fnm));
		MediaTracker imageTracker = new MediaTracker(new JPanel());

		imageTracker.addImage(image, 0);
		try {
			imageTracker.waitForID(0);
		} catch (InterruptedException e) {
			return null;
		}
		if (imageTracker.isErrorID(0))
			return null;
		return image;
	}

	/**
	 * Extract the individual images from the strip image file, <fnm>. We assume
	 * the images are stored in a single row, and that there are <number> of
	 * them. The images are returned as an array of BufferedImages
	 */
	public BufferedImage[] loadStripImageArray(String fnm, int number) {
		if (number <= 0) {
			System.out.println("number <= 0; returning null");
			return null;
		}

		BufferedImage stripIm;
		if ((stripIm = loadImage(fnm)) == null) {
			System.out.println("Returning null");
			return null;
		}

		int imWidth = (int) stripIm.getWidth() / number;
		int height = stripIm.getHeight();
		int transparency = stripIm.getColorModel().getTransparency();

		BufferedImage[] strip = new BufferedImage[number];
		Graphics2D stripGC;

		// each BufferedImage from the strip file is stored in strip[]
		for (int i = 0; i < number; i++) {
			strip[i] = gc.createCompatibleImage(imWidth, height, transparency);

			// create a graphics context
			stripGC = strip[i].createGraphics();
			// stripGC.setComposite(AlphaComposite.Src);

			// copy image
			stripGC.drawImage(stripIm, 0, 0, imWidth, height, i * imWidth, 0,
					(i * imWidth) + imWidth, height, null);
			stripGC.dispose();
		}
		return strip;
	}

	/**
	 * Loads and parses a 2D arranged Strip image
	 * 
	 * @param fnm
	 *            - filename
	 * @param rows
	 *            - number of rows in the strip image
	 * @param cols
	 *            - number of columns in the strip image
	 * @return an array of buffered images
	 */
	public BufferedImage[] loadStripImageArray(String fnm, int rows, int cols) {
		// load the image data
		BufferedImage stripIm;
		if ((stripIm = loadImage(fnm)) == null) {
			System.out.println("Returning null");
			return null;
		}
		if (rows <= 0 || cols <= 0) {
			System.out.println("number <= 0; returning null");
			return null;
		}

		// get dimensions of a frame
		int frameW = stripIm.getWidth() / cols;
		int frameH = stripIm.getHeight() / rows;

		BufferedImage[] fullSet = new BufferedImage[rows * cols];

		// parse the sheet and put it into the array
		for (int r = 0, y = 0, i = 0; r < rows; r++, y += frameH) {
			for (int c = 0, x = 0; c < cols; c++, x += frameW, i++) {
				BufferedImage b = stripIm.getSubimage(x, y, frameW, frameH);
				fullSet[i] = b;
			}
		}

		return fullSet;

	}

}

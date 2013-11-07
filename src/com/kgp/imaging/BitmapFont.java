package com.kgp.imaging;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Allows the use of pngs/bmp/gif/whatever image files you want as a way of rendering strings
 * 
 * @author Nicholas Hydock
 */
public class BitmapFont {

	//loads the bitmap file
	ImagesLoader imsLoader;
	
	//dictionary of acceptable glyphs for rendering, and their glyph data
	HashMap<Character, Glyph> glyphs;
	
	//directory where font files are located
	// not the same directory as where the bitmap referenced by the font is located
	private static final String FONTDIR = "Fonts/";
	
	//height of a line
	private int lineHeight;
	
	public BitmapFont(String fileName, ImagesLoader imsLoader)
	{
		this.imsLoader = imsLoader;
		
		parseGlyphs(fileName);
	}
	
	/**
	 * Parses a .font file for the dictionary of glyphs in the bitmap font
	 * @param glyphFile
	 */
	private void parseGlyphs(String glyphFile)
	{
		InputStream data = this.getClass().getClassLoader().getResourceAsStream(FONTDIR+glyphFile+".font");
		
		Scanner reader = new Scanner(data);
		
		//parse the first line of data specifying the image file used
		BufferedImage src = imsLoader.getImage(reader.nextLine());
		
		//second line specifies so general values
		this.lineHeight = reader.nextInt();
	
		while (reader.hasNextLine())
		{
			String line = reader.nextLine();
		
			Scanner lineParser = new Scanner(line);
			
			char character = (char)lineParser.nextInt();
			int x = lineParser.nextInt();
			int y = lineParser.nextInt();
			int w = lineParser.nextInt();
			int h = lineParser.nextInt();
			int base = lineParser.nextInt();
			
			this.glyphs.put(character, new Glyph(x, y, w, h, base, src));
			
			lineParser.close();
		}
		
		reader.close();
	}
	
	/**
	 * Draws a string using the Bitmap font's set of glyphs
	 * @param g
	 * @param str
	 * @param x
	 * @param y
	 */
	public void drawString(Graphics2D g, String str, int x, int y)
	{
		for (int i = 0, locX = 0, locY = y; i < str.length(); i++ )
		{
			Character c = str.charAt(i);
			if (c == '\n' || c == '\r')
			{
				locY += lineHeight;
				locX = x;
			}
			else if (c == ' ')
			{
				locX += 10;
			}
			else
			{
				Glyph glyph = glyphs.get(c);
				if (glyph != null)
				{
					g.drawImage(glyph.img, locX, locY - glyph.topHeight, null);
					locX += glyph.w;
				}
			}
		}
	}
	
	/**
	 * Representation of a container for a buffered image subsection in a bitmap font.
	 * 
	 * Glyphs in a font file are defined as
	 * 	{character (ASCII id)} {left} {top} {width} {height} {baseline}
	 * 
	 * @author Nicholas Hydock
	 */
	private class Glyph {
		int w, h, topHeight;
		BufferedImage img;
		
		public Glyph(int x, int y, int w, int h, int baseline, BufferedImage src)
		{
			this.img = src.getSubimage(x, y, w, h);
			this.w = w + 2;  //add salvage to the right for the next character that follows
			this.h = h;
			this.topHeight = h - baseline;
		}
	}
	
}

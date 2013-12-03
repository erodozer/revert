package com.kgp.imaging;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Allows the use of pngs/bmp/gif/whatever image files you want as a way of
 * rendering strings
 * <p/>
 * Please realize the when drawing a string, the string is positioned by its
 * baseline, not the top left coordinate of each glyph
 * 
 * @author Nicholas Hydock
 */
public class BitmapFont {

	// loads the bitmap file
	ImagesLoader imsLoader;

	// dictionary of acceptable glyphs for rendering, and their glyph data
	HashMap<Character, Glyph> glyphs;

	// directory where font files are located
	// not the same directory as where the bitmap referenced by the font is
	// located
	private static final String FONTDIR = "Fonts/";

	// height of a line
	private int lineHeight;

	public BitmapFont(String fileName, ImagesLoader imsLoader) {
		this.imsLoader = imsLoader;

		parseGlyphs(fileName);
	}

	/**
	 * Parses a .font file for the dictionary of glyphs in the bitmap font
	 * 
	 * @param glyphFile
	 */
	private void parseGlyphs(String glyphFile) {
		InputStream data = this.getClass().getClassLoader()
				.getResourceAsStream(FONTDIR + glyphFile + ".fnt");

		this.glyphs = new HashMap<Character, Glyph>();

		Scanner reader = new Scanner(data);

		// parse the first line of data specifying the image file used
		BufferedImage src = imsLoader.getImage(reader.nextLine());

		// second line specifies so general values
		this.lineHeight = reader.nextInt();

		System.out.println(lineHeight);

		while (reader.hasNextLine()) {
			char character = (char) reader.nextInt();
			int x = reader.nextInt();
			int y = reader.nextInt();
			int w = reader.nextInt();
			int h = reader.nextInt();
			int base = reader.nextInt();

			this.glyphs.put(character, new Glyph(x, y, w, h, base, src));
		}

		reader.close();
	}

	public int getLineHeight() {
		return lineHeight;
	}

	/**
	 * Find the width in pixels of a string if rendered using this font
	 * 
	 * @param str
	 */
	public void stringWidth(String str) {
		int max = 0;

		for (int i = 0, width = 0; i < str.length(); i++) {
			Character c = str.charAt(i);
			if (c == '\n' || c == '\r') {
				if (max < width) {
					max = width;
					width = 0;
				}
			} else if (c == ' ') {
				width += 10;
			} else {
				Glyph glyph = glyphs.get(c);
				if (glyph != null) {
					width += glyph.w;
				}
			}
		}
	}

	/**
	 * Draws a string using the Bitmap font's set of glyphs
	 * 
	 * @param g
	 * @param str
	 * @param x
	 * @param y
	 * @param a
	 *            - Alignment determining which side to anchor to
	 */
	public void drawString(Graphics2D g, String str, int x, int y, Alignment a) {
		if (a == Alignment.Center) {
		} else if (a == Alignment.Right) {
			for (int i = str.length() - 1, locX = x, locY = y; i >= 0; i--) {
				Character c = str.charAt(i);
				if (c == '\n' || c == '\r') {
					locY -= lineHeight;
					locX = x;
				} else if (c == ' ') {
					locX -= 10;
				} else {
					Glyph glyph = glyphs.get(c);
					if (glyph != null) {
						g.drawImage(glyph.img, locX - glyph.w, locY
								- glyph.topHeight, null);
						locX -= glyph.w;
					}
				}
			}
		} else {
			for (int i = 0, locX = x, locY = y; i < str.length(); i++) {
				Character c = str.charAt(i);
				if (c == '\n' || c == '\r') {
					locY += lineHeight;
					locX = x;
				} else if (c == ' ') {
					locX += 10;
				} else {
					Glyph glyph = glyphs.get(c);
					if (glyph != null) {
						g.drawImage(glyph.img, locX, locY - glyph.topHeight,
								null);
						locX += glyph.w;
					}
				}
			}
		}

	}

	/**
	 * Draws a string using the Bitmap font's set of glyphs
	 * 
	 * @param g
	 * @param str
	 * @param x
	 * @param y
	 */
	public void drawString(Graphics2D g, String str, int x, int y) {
		this.drawString(g, str, x, y, Alignment.Left);
	}

	/**
	 * Representation of a container for a buffered image subsection in a bitmap
	 * font.
	 * <p/>
	 * Glyphs in a font file are defined as {character (ASCII id)} {left} {top}
	 * {width} {height} {baseline}
	 * 
	 * @author Nicholas Hydock
	 */
	private class Glyph {
		int w, topHeight;
		BufferedImage img;

		public Glyph(int x, int y, int w, int h, int baseline, BufferedImage src) {
			this.img = src.getSubimage(x, y, w, h);
			this.w = w + 2; // add salvage to the right for the next character
							// that follows
			this.topHeight = h - (h - baseline);
		}
	}

	public static enum Alignment {
		Left, Right, Center;
	}

}

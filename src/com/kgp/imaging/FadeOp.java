package com.kgp.imaging;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Fades the color of the image to a specified hue and opacity over time.
 * Unlike other kinds of ops, this one is locked to the image
 * @author nhydock
 *
 */
public class FadeOp implements BufferedImageOp {

	private float[] next;
	private float[] prev;
	private float[] color;
	private float[] step;
	private float timer;
	
	public FadeOp(){
		color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
		prev = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
		next = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
		step = new float[]{0f, 0f, 0f, 0f};
		timer = 0f;
	}
	
	/**
	 * Not used
	 */
	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage arg0, ColorModel arg1) {
		return null;
	}

	
	@Override
	public BufferedImage filter(BufferedImage arg0, BufferedImage arg1) {
		if (arg1 == null)
			arg1 = new BufferedImage(arg0.getWidth(), arg0.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Raster r = arg0.getData();
		WritableRaster w = arg1.getRaster();
		float[] f = new float[4];
		for (int x = 0; x < arg0.getWidth(); x++)
			for (int y = 0; y < arg0.getHeight(); y++)
			{
				r.getPixel(x, y, f);
				for (int i = 0; i < 4; i++)
				{
					f[i] *= color[i];
				}
				w.setPixel(x, y, f);
			}
		
		
		return arg1;
	}
	
	/**
	 * fades the values to their dest color over time
	 * @param delta
	 */
	public void update(float delta)
	{
		if (timer > 0f){
			timer -= delta;
			for (int i = 0; i < 4; i++)
				color[i] += step[i] * delta;
			
			//force jump to destination when timer is up
			if (timer <= 0f){
				for (int i = 0; i < 4; i++)
					color[i] = next[i];
			}
					
		}
	}

	/**
	 * Not used
	 */
	@Override
	public Rectangle2D getBounds2D(BufferedImage arg0) {
		return null;
	}

	/**
	 * Not used
	 */
	@Override
	public Point2D getPoint2D(Point2D arg0, Point2D arg1) {
		return null;
	}

	/**
	 * Not used
	 */
	@Override
	public RenderingHints getRenderingHints() {
		return null;
	}

	/**
	 * Reset the mask effect to flash this specified value
	 */
	public void fade(float r, float g, float b, float a, float duration) {
		prev[0] = next[0];
		prev[1] = next[1];
		prev[2] = next[2];
		prev[3] = next[3];
				
		next[0] = r;
		next[1] = g;
		next[2] = b;
		next[3] = a;
		
		if (duration > 0){
			for (int i = 0; i < 4; i++) {
				color[i] = prev[i];
				step[i] = (next[i] - prev[i])/duration;
			}
		}
		else {
			for (int i = 0; i < 4; i++) {
				color[i] = next[i];
			}
		}
		
		timer = duration;
	}
	
	/**
	 * Reset the mask effect to flash this specified value
	 */
	public void fade(float r, float g, float b, float a) {
		this.fade(r, g, b, 1f);
	}
	

	public boolean active() {
		return timer > 0.0f;
	}
}

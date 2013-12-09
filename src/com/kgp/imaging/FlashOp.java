package com.kgp.imaging;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class FlashOp implements BufferedImageOp {

	private float[] color;
	private float timer;
	
	public FlashOp(){
		color = new float[]{1.0f, 1.0f, 1.0f};
	}
	
	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage arg0,
			ColorModel arg1) {
		return null;
	}

	@Override
	public BufferedImage filter(BufferedImage arg0, BufferedImage arg1) {
		if (timer <= 0f)
			return arg0;
		
		if (arg1 == null)
			arg1 = new BufferedImage(arg0.getWidth(), arg0.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Raster r = arg0.getData();
		WritableRaster w = arg1.getRaster();
		float[] f = new float[4];
		for (int x = 0; x < arg0.getWidth(); x++)
			for (int y = 0; y < arg0.getHeight(); y++)
			{
				r.getPixel(x, y, f);
				for (int i = 0; i < color.length; i++)
				{
					f[i] = 255f;
					f[i] *= color[i];
				}
				f[3] *= timer;
				w.setPixel(x, y, f);
			}
		
		
		return arg1;
	}
	
	//resets the values back to their defaults
	public void update(float delta)
	{
		if (timer > 0){
			timer -= delta;
		}
	}

	@Override
	public Rectangle2D getBounds2D(BufferedImage arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point2D getPoint2D(Point2D arg0, Point2D arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RenderingHints getRenderingHints() {
		return null;
	}

	public void flash(float r, float g, float b) {
		this.color[0] = r;
		this.color[1] = g;
		this.color[2] = b;
		timer = 1.0f;
	}

	public boolean active() {
		return timer > 0.0f;
	}
}

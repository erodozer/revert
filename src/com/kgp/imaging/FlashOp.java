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

	public float[] color;
	
	public FlashOp(float[] color){
		this.color = color;
	}
	
	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage arg0,
			ColorModel arg1) {
		return null;
	}

	@Override
	public BufferedImage filter(BufferedImage arg0, BufferedImage arg1) {
		if (arg1 == null)
			arg1 = new BufferedImage(arg0.getWidth(), arg0.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Raster r = arg0.getData();
		WritableRaster w = arg1.getRaster();
		for (int x = 0; x < arg0.getWidth(); x++)
			for (int y = 0; y < arg0.getHeight(); y++)
			{
				int[] f = new int[4];
				r.getPixel(x, y, f);
				f[0] *= color[0];
				f[1] *= color[1];
				f[2] *= color[2];
				
				w.setPixel(x, y, f);
			}
		
		
		return arg1;
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
		// TODO Auto-generated method stub
		return null;
	}

}

package revert.Entities;

import com.kgp.imaging.ImagesLoader;
import com.kgp.imaging.Sprite;

public class Bullet extends Sprite {

	private static final int STEP = 20;
	
	private boolean active = true;
	
	private int type;
	
	public Bullet(int x, int y, int w, int h, float angle, int type, ImagesLoader imsLd) {
		super(x, y, w, h, imsLd, "bullet");
	
		this.angle = (float)Math.toRadians(angle);
		this.setVelocity((int)Math.cos(this.angle)*STEP, (int)Math.sin(this.angle)*STEP);
		
		this.active = true;
		this.type = type;
	}
	
	public int getType()
	{
		return this.type;
	}
}

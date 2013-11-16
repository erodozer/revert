package revert.Entities;

import revert.MainScene.World;
import revert.util.AssetsManager;

import com.kgp.imaging.ImagesLoader;
import com.kgp.imaging.Sprite;

public class Bullet extends Sprite {

	private int type;
	
	public Bullet(World w, int type) {
		super(0, 0, w.getWidth(), w.getHeight(), AssetsManager.Images, "bullet");
	}

	public int getType()
	{
		return this.type;
	}
}

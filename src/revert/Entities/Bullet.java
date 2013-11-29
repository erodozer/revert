package revert.Entities;

import revert.MainScene.World;

import com.kgp.core.AssetsManager;
import com.kgp.imaging.ImagesLoader;
import com.kgp.imaging.Sprite;

public class Bullet extends Sprite {

	public static enum Mode {
		Gold, Silver, Copper;
		
		public Mode getNext()
		{
			int type = this.ordinal();
			type++;
			if (type >= Mode.values().length)
				type = 0;
			return Mode.values()[type];
		}
		
		public Mode getPrev()
		{
			int type = this.ordinal();
			type--;
			if (type < 0)
				type = Mode.values().length-1;
			return Mode.values()[type];
		}
	}
	
	private Mode type;
	
	public Bullet(World w, Mode type) {
		super(0, 0, w.getWidth(), w.getHeight(), AssetsManager.Images, "bullet");
	
		this.type = type;
	}

	public Mode getType()
	{
		return this.type;
	}
}

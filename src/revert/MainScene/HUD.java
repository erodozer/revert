package revert.MainScene;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import revert.Entities.Player;
import revert.MainScene.notifications.PlayerNotification;
import revert.MainScene.notifications.WorldNotification;

import com.kgp.core.AssetsManager;
import com.kgp.imaging.BitmapFont;
import com.kgp.imaging.BitmapFont.Alignment;

public class HUD implements Observer {

	BitmapFont font;
	
	Dimension view;
	
	String score = "$ 0";
	String hp = "# 0 / 10";
	String time = "0";
	
	int ammo;
	
	ArrayList<BufferedImage> bulletImages;
	BufferedImage bulletIm;
	BufferedImage emptyBullet;
	
	public HUD(Dimension view)
	{
		font = new BitmapFont("bm", AssetsManager.Images);
		bulletImages = AssetsManager.Images.getImages("ammo");
		bulletIm = bulletImages.get(0);
		emptyBullet = bulletImages.get(3);
		this.view = view;
	}
	
	/**
	 * Render the interface
	 * @param g
	 */
	public void display(Graphics2D g)
	{
		font.drawString(g, score, view.width-10, view.height - 10, Alignment.Right);
		font.drawString(g, hp, 10, view.height - 10);
		font.drawString(g, time, view.width - 10, 10 + font.getLineHeight(), Alignment.Right);
		
		for (int i = 0, x = view.width/2 - (bulletIm.getWidth() + 4) * Player.FULLAMMO/2, y = view.height - bulletIm.getHeight() - 10; i <= Player.FULLAMMO; i++, x += bulletIm.getWidth() + 4)
		{
			if (i > ammo)
			{
				g.drawImage(emptyBullet, x, y, null);
			}
			else
			{
				g.drawImage(bulletIm, x, y, null);		
			}
		}
	}
	
	@Override
	public void update(Observable o, Object args) {
		if (o instanceof World)
		{
			if (args instanceof WorldNotification)
			{
				WorldNotification n = (WorldNotification) args;
				score = n.score;
				time = n.time;
			}
		}
		else if (o instanceof Player)
		{
			PlayerNotification n = (PlayerNotification) args;
			hp = n.hp;
			ammo = n.ammo;
			this.bulletIm = bulletImages.get(n.mode.ordinal());
		}
	}
	
}

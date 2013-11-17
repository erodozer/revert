package revert.MainScene;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Panel;
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
	
	public HUD(Dimension view)
	{
		font = new BitmapFont("bm", AssetsManager.Images);
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
	}
	
	@Override
	public void update(Observable o, Object args) {
		if (o instanceof World)
		{
			WorldNotification n = (WorldNotification) args;
			score = n.score;
			time = n.time;
		}
		else if (o instanceof Player)
		{
			PlayerNotification n = (PlayerNotification) args;
			hp = n.hp;
			//ammo = n.ammo;
		}
	}
	
}

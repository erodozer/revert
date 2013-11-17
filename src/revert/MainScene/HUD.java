package revert.MainScene;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.util.Observable;
import java.util.Observer;

import revert.Entities.Player;
import revert.MainScene.notifications.WorldNotification;

import com.kgp.core.AssetsManager;
import com.kgp.imaging.BitmapFont;
import com.kgp.imaging.BitmapFont.Alignment;

public class HUD implements Observer {

	BitmapFont font;
	
	Player player;
	Dimension view;
	
	String score = "$ 0";
	String hp = "# 0 / 10";
	String time = "0";
	
	World world;
	
	public HUD(Player p, World w, Dimension view)
	{
		this.player = p;
		
		font = new BitmapFont("bm", AssetsManager.Images);
		world = w;
		world.addObserver(this);
		this.view = view;
	}
	
	public void display(Graphics2D g)
	{
		font.drawString(g, score, view.width-10, view.height - 10, Alignment.Right);
		font.drawString(g, hp, 10, 10 + font.getLineHeight());
	}
	
	@Override
	public void update(Observable o, Object args) {
		if (o == world)
		{
			WorldNotification n = (WorldNotification) args;
			score = n.score;
			time = n.time;
		}
	}

	
	
}

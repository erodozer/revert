import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import revert.MainScene.Scene;

import com.kgp.core.AssetsManager;
import com.kgp.core.GameFrame;

/**
 * Simple runner class to get the game started
 * @author nhydock
 *
 */
public class GameRunner {

	public static void main(String args[]) {
		GameFrame g = new GameFrame("Revert", 60);
		
		AssetsManager.init();
		
		Scene s = new Scene(g);
		
		if (args[0].equals("fullscreen")) {
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			if (gd.isFullScreenSupported()){
				try{
					g.setUndecorated(true);
					g.setGame(s);
					g.setSize(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
					gd.setFullScreenWindow(g);
				}
				finally {
					g.setUndecorated(false);
					g.setGame(s);
					g.pack();
					
					Dimension res = Toolkit.getDefaultToolkit().getScreenSize();

					//center the window on screen
					g.setLocation(res.width / 2 - g.getWidth() / 2, res.height / 2 - g.getHeight() / 2);
				}
			}
		}
		else
		{
			g.setUndecorated(false);
			g.setGame(s);
			g.pack();
			
			Dimension res = Toolkit.getDefaultToolkit().getScreenSize();

			//center the window on screen
			g.setLocation(res.width / 2 - g.getWidth() / 2, res.height / 2
					- g.getHeight() / 2);
		}
	}
}

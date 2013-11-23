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
		
		g.setGame(new Scene(g));
	}
}

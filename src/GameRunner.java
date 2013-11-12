import revert.MainScene.Scene;

import com.kgp.core.GameFrame;

public class GameRunner {

	public static void main(String args[]) {
		long period = (long) 1000.0 / GameFrame.DEFAULT_FPS;
		GameFrame g = new GameFrame("Revert", period*1000000L); // ms --> nanosecs
		g.setGame(new Scene(g, g.period));

	}
}

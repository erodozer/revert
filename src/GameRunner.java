import com.kgp.core.JumpingJack;

public class GameRunner {

	public static void main(String args[])
    { 
		long period = (long) 1000.0/JumpingJack.DEFAULT_FPS;
		// System.out.println("fps: " + DEFAULT_FPS + "; period: " + period + " ms");
		new JumpingJack(period*1000000L);    // ms --> nanosecs 
    }
}

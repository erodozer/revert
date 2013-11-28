package revert.level;

import java.util.Observable;


/**
 * Player must kill a set amount of enemies in a specified
 * amount of time.
 * @author nhydock
 *
 */
public class TimeObjective implements Objective {

	float timer;
	int enemyCount;
	
	public void init(){
		timer = (float)(Math.random() * 120) + 30;
		enemyCount = (int)(Math.random() * 30) + 10;
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Rank getRank() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getBonus() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getMessage() {
		String message = "D %d E\nW %d &";
		return String.format(message, enemyCount, (int)timer);
	}

	@Override
	public boolean isFinished() {
		return timer <= 0 || enemyCount <= 0;
	}

	@Override
	public void update(float delta) {
		timer -= delta;
	}
	
}

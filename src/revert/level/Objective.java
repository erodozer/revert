package revert.level;

import java.util.Observer;

/**
 * Level objectives rank the player's performance in completing the level
 * under specified conditions.
 * @author nhydock
 *
 */
public interface Objective extends Observer {

	public static enum Rank {
		S, A, B, C, D, F;
	}
	
	/**
	 * Initialize key values of the objective
	 */
	public void init();
	
	/**
	 * Calculate the player earned rank for the objective
	 * @return
	 */
	public Rank getRank();
	
	/**
	 * Get a calculated bonus return for completing the objective
	 * @return int
	 */
	public int getBonus();
	
	/**
	 * Gets the objective prompt to display on screen before the level starts
	 * @return
	 */
	public String getMessage();
	
	/**
	 * Get if the conditions of the objective have been fully met
	 * @return
	 */
	public boolean isFinished();
	
	
	public void update(float delta);
}

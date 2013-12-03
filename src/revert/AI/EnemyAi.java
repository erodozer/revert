package revert.AI;

import revert.Entities.Actor;

public interface EnemyAi {

	/**
	 * AI to be invoked when an actor is told to attack
	 */
	public void attack(Actor a);
	
	/**
	 * AI to be invoked when an actor enters the enemy's view
	 * @param a
	 */
	public void inView(Actor a);
	
	/**
	 * AI to be invoked when an actor exits the enemy's view
	 * @param a
	 */
	public void outOfView(Actor a);
	
	/**
	 * AI definition of how to act with actors when in an aggressive state
	 */
	public void aggress(Actor a);
	
	/**
	 * Get the distance that the ai is capable of seeing things
	 * @return
	 */
	public float viewRange();
	
	/**
	 * Get distance and which the ai is able to follow at while being aggressive after
	 * something enters its range
	 */
	public float aggressRange();
}

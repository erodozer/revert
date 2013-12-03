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
	
	/**
	 * Get the speed at which the enemy will issue attack commands when aggressing
	 * @return
	 */
	public int attackRate();
	
	/**
	 * Get the distance at which enemy attacks may hit
	 * @return
	 */
	public float attackRange();
	
	/**
	 * Perform generic updates dependent on the update cycle of the game,
	 * such as iterating internal timers
	 * @param delta
	 */
	public void update(float delta);

	/**
	 * Perform basic movement when non aggressive
	 */
	public void walk();
	
	/**
	 * Get if the enemy is in an aggressive state
	 * @return
	 */
	public boolean isAgro();
}

package revert.AI;

import java.util.HashSet;
import java.util.Set;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;
import revert.Entities.Player;

public class PassiveAI implements EnemyAi 
{
	Enemy parent;
	
	//max movement time
	private final float MOVE_TIME = (float)Math.pow(3,9);

	float walkTimer;
	
	Set<Actor> aggressors;
	
	public PassiveAI(Enemy e)
	{
		parent = e;
		aggressors = new HashSet<Actor>();
	}
	/**
	 * This agent does not attack
	 */
	@Override
	public void attack(Actor a) 
	{
		//stare to death
		a.takeHit();
	}

	/**
	 * If the player comes in view the agent stops 
	 * and the player begins to loose health
	 */
	@Override
	public void inView(Actor a) 
	{
		//stare only at players
		if (a instanceof Player) {
			parent.stop();
			aggressors.add(a);
		}
	}

	/**
	 * Meanders back and forth at random
	 */
	@Override
	public void outOfView(Actor a) 
	{
		aggressors.remove(a);
	}
	
	/**
	 * This agent does not agress, only stares
	 */
	@Override
	public void aggress(Actor a) 
	{
		attack(a);
	}
	
	@Override
	public float viewRange() {
		return 30;
	}
	
	/**
	 *  passive AI are indirectly aggressive thanks to their stare
	 */
	@Override
	public float aggressRange() {
		return -1;
	}
	
	/**
	 * Never attacks
	 */
	@Override
	public float attackRate() {
		return -1;
	}
	
	@Override
	public float attackRange() {
		// no worries
		return 0;
	}
	
	/**
	 * Walks around when not staring at something
	 */
	@Override
	public void update(float delta) {
		if (!isAgro()){
			if (walkTimer > 0)
				walkTimer -= delta;
			else
				walk();
		}
	}
	
	@Override
	/**
	 * Simple walking action
	 */
	public void walk()
	{
		int i = (int)Math.random()*10;
		if( i <= 2)
		{
			int j = (int)Math.random();
			if(j == 1)
			{
				parent.lookAt(Vector2.LEFT);
				parent.moveLeft();
			}
			else
			{
				parent.lookAt(Vector2.RIGHT);
				parent.moveRight();
			}
			walkTimer = MOVE_TIME;
		}
		else
		{
			parent.stop();
			walkTimer = .5f;
		}
	}
	
	@Override
	public boolean isAgro() {
		// staring is their aggressiveness
		return aggressors.size() > 0;
	}
	@Override
	public void hit() {
		// do nothing
	}
	
	@Override
	public void update(Actor a) {
		// stare at the player when he's visible to the passive AI
		if (isAgro())
			if (a instanceof Player)
				aggress(a);
	}

}

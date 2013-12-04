package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;

public class PassiveAI implements EnemyAi 
{
	Enemy parent;
	
	private final float MOVE_TIME = (float)Math.pow(3,9);		//max movement time

	float walkTimer;
	
	boolean stare;
	
	public PassiveAI(Enemy e)
	{
		parent = e;
	}
	/**
	 * This agent does not attack
	 */
	@Override
	public void attack(Actor a) 
	{
		//Does Nothing
		a.takeHit();
	}

	/**
	 * If the player comes in view the agent stops 
	 * and the player begins to loose health
	 */
	@Override
	public void inView(Actor a) 
	{
		stare = true;
		parent.stop();
		a.takeHit();
	}

	/**
	 * Meanders back and forth at random
	 */
	@Override
	public void outOfView(Actor a) 
	{
		stare = false;
	}
	
	/**
	 * This agent does not agress
	 */
	@Override
	public void aggress(Actor a) 
	{
		a.takeHit();
	}
	
	@Override
	public float viewRange() {
		return 30;
	}
	@Override
	public float aggressRange() {
		return -1;
	}
	@Override
	public int attackRate() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public float attackRange() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void update(float delta) {
		if (!stare){
			if (walkTimer > 0)
				walkTimer -= delta;
		}
	}
	
	@Override
	/**
	 * Simple walking action
	 */
	public void walk()
	{
		if (stare)
			return;
		
		if (walkTimer < 0)
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
	}
	
	@Override
	public boolean isAgro() {
		// does not get aggressive
		return stare;
	}
	@Override
	public void hit() {
		// TODO Auto-generated method stub
		
	}

}

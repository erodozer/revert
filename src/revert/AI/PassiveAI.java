package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;
import sun.awt.AWTCharset;

public class PassiveAI implements EnemyAi 
{

	private Enemy e;									//agent
	private float timer;								//timer for movement
	private final double MOVE_TIME = Math.pow(3,9);		//max movement time
	private final int VIEW_RANGE = 30;
	private final int AGGRESS_RANGE = 0;
	
	
	public PassiveAI(Enemy e)
	{
		this.e = e;
	}
	/**
	 * This agent does not attack
	 */
	@Override
	public void attack(Actor a) 
	{
		//Does Nothing
	}

	/**
	 * If the player comes in view the agent stops 
	 * and the player begins to loose health
	 */
	@Override
	public void inView(Actor a) 
	{
		e.stop();
		a.takeHit();
	}

	/**
	 * Meanders back and forth at random
	 */
	@Override
	public void outOfView(Actor a) 
	{
		int i = (int)Math.random()*10;
		if( i <= 2)
		{
			timer = System.nanoTime();
			int j = (int)Math.random();
			if(j == 1)
			{
				e.lookAt(Vector2.LEFT);
				while((timer - System.nanoTime()) < MOVE_TIME)
					e.moveLeft();
				e.stop();
			}
			else
			{
				e.lookAt(Vector2.RIGHT);
				while((timer - System.nanoTime()) < MOVE_TIME)
					e.moveRight();
				e.stop();
			}
		}
		

	}
	
	/**
	 * This agent does not agress
	 */
	@Override
	public void aggress(Actor a) 
	{
	}
	@Override
	public float viewRange() {
		// TODO Auto-generated method stub
		return VIEW_RANGE;
	}
	@Override
	public float aggressRange() {
		// TODO Auto-generated method stub
		return AGGRESS_RANGE;
	}

}

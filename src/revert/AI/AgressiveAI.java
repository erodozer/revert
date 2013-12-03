package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;
import revert.Entities.Actor.Direction;

public class AgressiveAI implements EnemyAi 
{
	private Enemy e;
	private float timer;
	private final double MOVE_TIME = Math.pow(5,9);
	private boolean agro;
	private final int VIEW_RANGE = 70;
	private final int AGGRESS_RANGE = 50;
	
	public AgressiveAI(Enemy e)
	{
		this.e = e;
		agro = false;
	}
	
	/**
	 * Rolls to attack and sits
	 */
	@Override
	public void attack(Actor a) 
	{
		int i = (int)Math.random()*10;
		
		if(i <= 5)
		{
			a.takeHit();
		}
		e.stop();

	}

	/**
	 * Chase player and attack
	 */
	@Override
	public void inView(Actor a) 
	{
		if(agro)
		{
			e.lookAt(new Vector2(a.getXPosn(),a.getYPosn()));
			if(e.getDirection() == Direction.Left)
				e.moveLeft();
			else
				e.moveRight();
			if(e.inRange(a))
				attack(a);
		}

	}

	/**
	 * Meanders randomly
	 */
	@Override
	public void outOfView(Actor a) 
	{
		walk();

	}

	/**
	 * Always Agressive so once play comes into view jump to inView
	 */
	@Override
	public void aggress(Actor a) 
	{
		inView(a);

	}
	
	/**
	 * 
	 * @return true if agent is aggressive
	 */
	public boolean isAgro()
	{
		return agro;
	}

	/**
	 * Simple walking action
	 */
	public void walk()
	{
		int i = (int)Math.random()*10;
		if( i <= 8)
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

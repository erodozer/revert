package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Actor.Direction;
import revert.Entities.Enemy;

public class ActiveAI implements EnemyAi 
{
	
	private Enemy e;									//agent
	private float timer;								//timer for movement
	private final double MOVE_TIME = Math.pow(5,9);		//maximum movement time
	private boolean agro;								//bool for agro
	private final int VIEW_RANGE = 50;
	private final int AGGRESS_RANGE = 30;
	
	public ActiveAI(Enemy e)
	{
		this.e = e;
		agro = false;
	}
	
	/**
	 * Rolls for attack and sits still
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
	 * if agro'ed will chase player and attack
	 * otherwise meander
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
		else
			walk();
	}

	/**
	 * Drops Agro and Meanders
	 */
	@Override
	public void outOfView(Actor a) 
	{
		agro = false;
		walk();
	}

	/**
	 * Agent only becomes agressive if prevoked
	 */
	@Override
	public void aggress(Actor a) 
	{
		agro = true;
		e.addObserver(a);
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
		if( i <= 5)
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

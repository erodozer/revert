package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;

public class ActiveAI implements EnemyAi 
{
	
	private Enemy e;									//agent
	private float timer;								//timer for movement
	private final double MOVE_TIME = Math.pow(5,9);		//maximum movement time
	private boolean agro;								//bool for agro
	
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
			if(e.isRightOf(a))
				e.moveRight();
			else
				e.moveLeft();
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
		e.deleteObserver(a);
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
}

package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;

public class ActiveAI implements EnemyAi 
{
	
	private Enemy e;
	private float timer;
	private final double MOVE_TIME = Math.pow(5,9);
	private boolean agro;
	
	public ActiveAI(Enemy e)
	{
		this.e = e;
		agro = false;
	}
	
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

	@Override
	public void inView(Actor a) 
	{
		if(agro)
		{
			if(e.isRightOf(a))
				e.moveRight();
			else
				e.moveLeft();
		}
		else
			walk();
	}

	@Override
	public void outOfView(Actor a) 
	{
		agro = false;
		e.deleteObserver(a);
		walk();
	}

	@Override
	public void aggress(Actor a) 
	{
		agro = true;
		e.addObserver(a);
	}

	public boolean isAgro()
	{
		return agro;
	}
	
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

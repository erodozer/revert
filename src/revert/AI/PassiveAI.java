package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;
import sun.awt.AWTCharset;

public class PassiveAI implements EnemyAi 
{

	private Enemy e;
	private float timer;
	private final double MOVE_TIME = Math.pow(3,9);
	
	public PassiveAI(Enemy e)
	{
		this.e = e;
	}
	@Override
	public void attack(Actor a) 
	{
		//Does Nothing
	}

	@Override
	public void inView(Actor a) 
	{
		e.stop();
		a.takeHit();
	}

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

	@Override
	public void aggress(Actor a) 
	{
		//Does Nothing

	}

}

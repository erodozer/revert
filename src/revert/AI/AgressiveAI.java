package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;
import revert.Entities.Actor.Direction;

public class AgressiveAI implements EnemyAi 
{
	boolean agro;
	Enemy parent;
	
	float agroTimer;
	float attackTimer;
	float walkTimer;
	
	float MOVE_TIME;
	
	public AgressiveAI(Enemy e)
	{
		parent = e;
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
		parent.stop();
	}

	/**
	 * Chase player and attack
	 */
	@Override
	public void inView(Actor a) 
	{
		if(agro)
		{
			parent.lookAt(new Vector2(a.getXPosn(),a.getYPosn()));
			if(parent.getDirection() == Direction.Left)
				parent.moveLeft();
			else
				parent.moveRight();
			if(parent.inRange(a))
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
		if (!agro)
		{
			if (a.getPosn().distance(parent.getPosn()) < this.aggressRange())
			{
				agro = true;
				attackTimer = this.attackRate();
				agroTimer = 3.0f;
			}
		}
		else
		{
			//set the timer to full as long as there is an actor within range
			if (a.getPosn().distance(parent.getPosn()) < this.aggressRange())
			{
				agroTimer = 3.0f;
				agro = true;
			}
			
			//attack this enemy if the timer is up
			if (attackTimer < 0)
			{
				attack(a);
				attackTimer = this.attackRate();
			}
		}
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
		if (walkTimer < 0)
		{
			int i = (int)Math.random()*10;
			if( i <= 8)
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
	public float viewRange() {
		return 70;
	}

	@Override
	public float aggressRange() {
		return 50;
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

	/**
	 * Update timers
	 */
	public void update(float delta)
	{
		if (agro)
		{
			agroTimer -= delta;
			
			//time out agro when the target is too far away
			if (agroTimer < 0)
				agro = false;
			
			//decrease attack timer
			if (attackTimer > 0)
				attackTimer -= delta;
		}
		else
		{
			if (walkTimer > 0)
				walkTimer -= delta;
		}
	}
}

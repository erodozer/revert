package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Actor.Direction;
import revert.Entities.Enemy;

public class ActiveAI implements EnemyAi 
{
	
	Enemy parent;									//agent
	boolean agro;								//bool for agro
	
	float agroTimer;
	float attackTimer;
	float walkTimer;
	
	float MOVE_TIME;
	
	public ActiveAI(Enemy e)
	{
		parent = e;
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
			parent.stop();
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
			parent.lookAt(new Vector2(a.getXPosn(),a.getYPosn()));
			if(parent.getDirection() == Direction.Left)
				parent.moveLeft();
			else
				parent.moveRight();
			if(parent.inRange(a))
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
		parent.addObserver(a);
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
		if (walkTimer < 0)
		{
			int i = (int)Math.random()*10;
			if( i <= 5)
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
		// TODO Auto-generated method stub
		return 50;
	}

	@Override
	public float aggressRange() {
		// TODO Auto-generated method stub
		return 30;
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

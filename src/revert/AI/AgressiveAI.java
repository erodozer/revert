package revert.AI;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;
import revert.Entities.Actor.Direction;
import revert.Entities.Player;

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
		if (a instanceof Player)
		{
			//go back to agro if the player steps within view 
			// while it's waiting to phase out of agro
			if (!agro && agroTimer > 0f)
			{
				agro = true;
			}
		}
	}

	/**
	 * Meanders randomly
	 */
	@Override
	public void outOfView(Actor a) 
	{
		if (a instanceof Player)
		{
			//start transition phase to out of agro once the player 
			// exits the view range of the player
			agro = false;
			agroTimer = 3.0f;
		}
	}

	/**
	 * Always Agressive so once play comes into view jump to inView
	 */
	@Override
	public void aggress(Actor a) 
	{
		float dist = (float)a.getPosn().distance(parent.getPosn());
		
		//player within range of the enemy
		if (agro) {
			//attack this enemy if the timer is up
			if (attackTimer <= 0)
			{
				if (dist < this.attackRange()) {
					attack(a);
					attackTimer = attackRate();
				}
			}
		}
		else
		{
			if (dist < this.aggressRange())
			{
				attackTimer = attackRate();
				agro = true;
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
	public float attackRate() {
		return 2.0f;
	}

	@Override
	public float attackRange() {
		return 30;
	}

	@Override
	public void hit()
	{
		agro = true;
	}

	/**
	 * Update timers
	 */
	public void update(float delta)
	{
		//as long as the ai is truly aggressive, try attacking
		if (agro) {
			//decrease attack timer
			if (attackTimer > 0)
				attackTimer -= delta;
		}
		//wait for agro to go away once the timer is done
		else if (!agro && agroTimer > 0)
		{
			agroTimer -= delta;
		}
		else
		{
			//decrease walk wait timer when not agro
			if (walkTimer > 0)
				walkTimer -= delta;
		}
	}

	@Override
	public void update(Actor a) {
		if (a instanceof Player) {
			//as long as it's in an aggressive phase, try aggressing
			if (agroTimer > 0)
			{
				aggress(a);
			}
		}

		if (a instanceof Enemy)
		{
			Enemy e = (Enemy)a;
			
			//go agro if nearby ally is hurt
			if (e.getAI().isAgro())
			{
				this.agro = true;
				this.agroTimer = 3f;
			}
		}
	}
}

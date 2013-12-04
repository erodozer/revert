package revert.AI;

import java.util.HashSet;
import java.util.Set;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Enemy;
import revert.Entities.Player;

public class AgressiveAI implements EnemyAi 
{
	Enemy parent;
	
	float agroTimer;	//phase out of agro when once agro and now there are no more aggressors
	float attackTimer;	//staller to prevent constant attacks
	float walkTimer;	//staller to create a methodical walking pattern
	
	float MOVE_TIME;
	
	//keep track of all actors that are causing this AI to be aggressive
	Set<Actor> aggressors;
	
	public AgressiveAI(Enemy e)
	{
		parent = e;
		aggressors = new HashSet<Actor>();
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
			//increase agro dependence if already agro
			if (isAgro())
				aggressors.add(a);
			//go back to agro if the player steps within view 
			// while it's waiting to phase out of agro
			else if (agroTimer > 0f)
				aggressors.add(a);
		}
	}

	/**
	 * Meanders randomly
	 */
	@Override
	public void outOfView(Actor a) 
	{
		//remove actor from list of aggressors if it is one
		aggressors.remove(a);
		
		//start transition phase to out of agro once all players have
		// exited the view range of the enemy
		if (!isAgro())
		{
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
		//attack this enemy if the timer is up
		if (attackTimer <= 0)
		{
			if (dist < this.attackRange()) {
				attack(a);
				attackTimer = attackRate();
			}
		}
	}
	
	/**
	 * @return true if agent is aggressive
	 */
	public boolean isAgro()
	{
		return aggressors.size() > 0 && agroTimer > 0;
	}

	/**
	 * Simple walking action
	 */
	public void walk()
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

	@Override
	public float viewRange() {
		return 70;
	}

	/**
	 * Aggressive enemies become aggressive as soon as the player enters its view range
	 */
	@Override
	public float aggressRange() {
		return 70;
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
		
	}

	/**
	 * Update timers
	 */
	public void update(float delta)
	{
		//as long as the ai is truly aggressive, try attacking
		if (isAgro()) {
			//decrease attack timer
			if (attackTimer > 0)
				attackTimer -= delta;
		}
		else 
		{
			//wait for agro to go away once the timer is done
			if (!isAgro() && agroTimer > 0)
				agroTimer -= delta;
			
			//decrease walk wait timer when not pure agro
			if (walkTimer > 0) 
				walkTimer -= delta;
			else
				walk();
		}
	}

	@Override
	public void update(Actor a) {
		if (a instanceof Player) {
			//as long as it's in an aggressive phase, try aggressing
			if (isAgro())
			{
				aggress(a);
				
				//make sure this player is an aggressor even if they were just in the
				// visible range before the enemy became agro
				aggressors.add(a);
			}
			else {
				//if the player is in the aggression range while visible, 
				// and the enemy was not previously agro, then make them
				if (a.getPosn().distance(parent.getPosn()) < this.aggressRange())
				{
					aggressors.add(a);
				}
			}	
		}

		if (a instanceof Enemy)
		{
			Enemy e = (Enemy)a;
			
			//if another visible enemy is agro, then this enemy is agro
			if (e.getAI().isAgro())
				aggressors.add(e);
		}
	}

}

package revert.AI;

import java.util.HashSet;
import java.util.Set;

import com.kgp.util.Vector2;

import revert.Entities.Actor;
import revert.Entities.Actor.Direction;
import revert.Entities.Enemy;
import revert.Entities.Player;

public class ActiveAI implements EnemyAi 
{
	Enemy parent;								//agent
	
	float agroTimer;
	float attackTimer;
	float walkTimer;
	
	float MOVE_TIME;
	
	//keep track of all actors that are causing this AI to be aggressive
	Set<Actor> aggressors;
		
	public ActiveAI(Enemy e)
	{
		parent = e;
		aggressors = new HashSet<Actor>();
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
		if (a instanceof Player) {
			if(isAgro())
			{
				aggressors.add(a);
			}
		}
	}

	/**
	 * Drops Agro and Meanders
	 */
	@Override
	public void outOfView(Actor a) 
	{
		aggressors.remove(a);
		
		if (!isAgro()){
			agroTimer = 2f;
		}
	}

	@Override
	public void aggress(Actor a) 
	{
		//player within range of the enemy
		//attack this enemy if the timer is up
		if (attackTimer <= 0)
		{
			float dist = (float)a.getPosn().distance(parent.getPosn());
			if (dist < this.attackRange()) {
				attack(a);
				attackTimer = attackRate();
			}
		}
	}

	/**
	 * 
	 * @return true if agent is aggressive
	 */
	public boolean isAgro()
	{
		return aggressors.size() > 0;
	}
	
	
	/**
	 * Simple walking action
	 */
	public void walk()
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

	@Override
	public float viewRange() {
		return 80f;
	}

	/**
	 * Active AI doesn't become aggressive on its own
	 */
	@Override
	public float aggressRange() {
		return 40f;
	}

	@Override
	public float attackRate() {
		return 3f;
	}

	@Override
	public float attackRange() {
		return 20f;
	}

	@Override
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
			if (agroTimer > 0)
				agroTimer -= delta;
			
			//decrease walk wait timer when not pure agro
			if (walkTimer > 0) 
				walkTimer -= delta;
			else
				walk();
		}
	}

	@Override
	public void hit() 
	{
		agroTimer = 3f;
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
			else if (agroTimer > 0f){
				//if the player is in the aggression range while visible, 
				// and the enemy is in an aware state, make the player an aggressor
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

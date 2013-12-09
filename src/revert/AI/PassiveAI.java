package revert.AI;

import java.util.HashSet;
import java.util.Set;

import revert.Entities.Actor;
import revert.Entities.Enemy;
import revert.Entities.Player;

import com.kgp.core.Game;

public class PassiveAI implements EnemyAi 
{
	Enemy parent;
	
	//max movement time
	private final float MOVE_TIME = 5f;

	float walkTimer;
	float attackTimer;
	
	Set<Actor> aggressors;
	
	public PassiveAI(Enemy e)
	{
		parent = e;
		aggressors = new HashSet<Actor>();
	}
	/**
	 * This agent does not attack
	 */
	@Override
	public void attack(Actor a) 
	{
		//stare to death
		a.takeHit();
	}

	/**
	 * If the player comes in view the agent stops 
	 * and the player begins to loose health
	 */
	@Override
	public void inView(Actor a) 
	{
		//stare only at players
		if (a instanceof Player) {
			parent.stop();
			aggressors.add(a);
		}
	}

	/**
	 * Meanders back and forth at random
	 */
	@Override
	public void outOfView(Actor a) 
	{
		aggressors.remove(a);
	}
	
	/**
	 * This agent does not agress, only stares
	 */
	@Override
	public void aggress(Actor a) 
	{
		if (attackTimer < 0)
		{
			attack(a);
			attackTimer = this.attackRate();
		}
		
	}
	
	@Override
	public float viewRange() {
		return 200f;
	}
	
	/**
	 *  passive AI are indirectly aggressive thanks to their stare
	 */
	@Override
	public float aggressRange() {
		return -1;
	}
	
	/**
	 * Never attacks
	 */
	@Override
	public float attackRate() {
		return 3f;
	}
	
	@Override
	public float attackRange() {
		// no worries
		return 0;
	}
	
	/**
	 * Walks around when not staring at something
	 */
	@Override
	public void update(float delta) {
		if (!isAgro()){
			if (walkTimer > 0)
				walkTimer -= delta;
			else
				walk();
		}
	}
	
	@Override
	/**
	 * Simple walking action
	 */
	public void walk()
	{
		int i = (int)Math.random()*10;
		if( i <= 2)
		{
			double j = (int)Math.random();
			if(j > .5)
			{
				parent.faceLeft();
				parent.moveLeft();
			}
			else
			{
				parent.faceRight();
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
	public boolean isAgro() {
		// staring is their aggressiveness
		return aggressors.size() > 0;
	}
	@Override
	public void hit() {
		// do nothing
	}
	
	@Override
	public void update(Actor a) {
		// stare at the player when he's visible to the passive AI
		if (isAgro())
		{
			if (a instanceof Player)
				aggress(a);
			
			attackTimer -= Game.getDeltaTime();
		}
	}
	@Override
	public Set<Actor> getAggressors() {
		return aggressors;
	}
	@Override
	public float moveRate() {
		return 20f;
	}

}

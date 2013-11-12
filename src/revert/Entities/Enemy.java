package revert.Entities;

import java.util.ArrayList;

import revert.AI.EnemyAi;
import revert.MainScene.World;

public class Enemy extends Actor {
	
	//enemy is aggressively chasing the player and will attack
	protected boolean aggro;
	
	//enemy is chasing after an actor
	protected Actor follow;

	//timer for waiting until attacking again
	protected int attackWait;
	
	//timer for waiting to exit aggro mode
	protected int aggroTimer;
	
	protected EnemyAi ai;
	
	public Enemy(World w, Player player) {
		super(w, "enemy", new ArrayList<Actor>());
		
		this.actors.add(player);
	}
	
	protected void setAI(EnemyAi ai)
	{
		this.ai = ai;
	}

	@Override
	protected String getNextImage() {
		return null;
	}

	/**
	 * Attack the target the enemy is following
	 */
	@Override
	public void attack() {
		//Do Nothing
		//Override for different actions on queuing attacks
	}

	/**
	 * React on notification of an actor entering this enemy's view area
	 */
	@Override
	protected void reactOnInView(Actor a) {
	}

	/**
	 * React on notification of an actor exiting this enemy's view area
	 */
	@Override
	protected void reactOnOutOfView(Actor a) {
		
	}

	/**
	 * Perform updates on the enemy's aggressive condition handling
	 */
	final protected void updateAggro()
	{
		//count down the aggro timer to leave agro mode
		if (!aggro && aggroTimer > 0)
		{
			aggroTimer -= period;
		}
		
		if (aggro)
		{
			ai.aggress(follow);
			if (follow == null)
				return;
			
			//check distance and chance to attack
			if (Math.abs(follow.getPosn().distance(this.getPosn())) < this.getAttackRange())
			{
				if (attackWait < 0)
				{
					attack();
					attackWait = this.getAttackWait();
				}
			}
		}
	}
	
	/**
	 * @return enemy's reach for attacking the player
	 */
	protected double getAttackRange() {
		return -1;
	}
	
	/**
	 * @return time pause between attacks
	 */
	protected int getAttackWait()
	{
		return 300;
	}
	
	/**
	 * @return the area range of contact for being hit by a bullet
	 */
	protected double getCollissionRange()
	{
		return 10;
	}

	/**
	 * Check for collision with the entity
	 * @param b - bullet object fired by the player
	 * @return true if bullet has hit the enemy
	 */
	public boolean hit(Bullet b) {
		if (b.getPosn().distance(this.getPosn()) < this.getCollissionRange())
		{
			if (b.getType() == this.getType())
			{
				this.hp--;
				this.isHit = true;
				this.hitTimer = 200;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return Enemy's identifying type
	 */
	public int getType()
	{
		return 0;
	}
	
	
}

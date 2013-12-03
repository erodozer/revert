package revert.Entities;

import java.util.ArrayList;
import java.util.Observable;

import com.kgp.core.Game;

import revert.AI.EnemyAi;
import revert.MainScene.World;
import revert.MainScene.notifications.ActorsRemoved;

public class Enemy extends Actor {
	
	//enemy is chasing after an actor
	protected Actor follow;

	//timer for waiting until attacking again
	protected int attackWait;
	
	//timer for waiting to exit aggro mode
	protected int aggroTimer;
	
	protected EnemyAi ai;
	
	public Enemy(World w, Player player) {
		super(w, "enemy", new ArrayList<Actor>());
		
		this.hp = 3;
		
		this.velocity.y = 2;
		this.vertMoveMode = VertMovement.Falling;
		maxVertTravel = 40;
		vertStep = maxVertTravel * 2 * Game.getDeltaTime();
		vertTravel = 0f;

		this.actors.add(player);

	}
	
	protected void setAI(EnemyAi ai)
	{
		this.ai = ai;
	}

	@Override
	protected void setNextImage() {
		if (isJumping())
			setImage("enemy_1", false);
		else if (!isStill())
			setImage("enemy_1", true);
		else
			setImage("enemy_1", false);
	}
	
	/**
	 * Updates on notifications from the world that the actor is observing
	 * @param o - object that sent the notification
	 * @param args - type of notification
	 */
	@Override
	public void update(Observable o, Object args)
	{
		super.update(o, args);
		
		if (args instanceof Actor)
		{
			if (this.visibility.get(args))
			{
				if (ai.isAgro())
				{
					ai.aggress((Actor)args);
				}
				else
				{
					ai.walk();
				}
			}
		}
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
		ai.inView(a);
	}

	/**
	 * React on notification of an actor exiting this enemy's view area
	 */
	@Override
	protected void reactOnOutOfView(Actor a) {
		ai.outOfView(a);
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
		if (this.getMyRectangle().contains(b.getPosn()))
		{
			System.out.println("hit");
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
	public Bullet.Mode getType()
	{
		return Bullet.Mode.Gold;
	}
	
	/**
	 * Check the actor postion wrt enemy
	 * @param a - any actor on field
	 * @return true if actor is to the right of enemy
	 */
	public boolean isRightOf(Actor a)
	{
		if(this.getXPosn() - a.getXPosn() < 0)
			return true;
		else
			return false;
	}
	
	public boolean inRange(Actor a)
	{
		if(this.getPosn().distance(a.getPosn()) <= ai.viewRange())
			return true;
		else
			return false;
	}
	
}

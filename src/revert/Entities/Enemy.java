package revert.Entities;

import java.util.Observable;

import com.kgp.core.Game;
import com.kgp.util.Vector2;

import revert.AI.ActiveAI;
import revert.AI.AgressiveAI;
import revert.AI.EnemyAi;
import revert.AI.NullAI;
import revert.AI.PassiveAI;
import revert.Entities.Bullet.Mode;
import revert.MainScene.World;

/**
 * Main enemy object that the player will have to fight during the
 * course of the game.
 * @author nhydock
 *
 */
public class Enemy extends Actor {

	//filename for animations
	private String name;

	protected EnemyAi ai;

	private Mode type;

	protected Enemy(World w, Player player, int type) {
		super(w, "enemy");

		this.hp = 3;

		this.velocity.y = 2;
		this.vertMoveMode = VertMovement.Falling;
		maxVertTravel = 40;
		vertStep = maxVertTravel * 2 * Game.getDeltaTime();
		vertTravel = 0f;

		
		visibility.put(player, false);
		for (Actor a : w.getEnemies())
		{
			visibility.put(a, false);
		}

		this.type = Bullet.Mode.values()[type];

		if (type == 0) {
			ai = new ActiveAI(this);
		} else if (type == 1) {
			ai = new PassiveAI(this);
		} else if (type == 2) {
			ai = new AgressiveAI(this);
		} else {
			System.out.println("No enemy AI type corresponds to value: " + type + ".  Instantiating basic enemy");
			ai = new NullAI();
		}
		
		name = "enemy_" + (type+1);
	}
	
	@Override
	protected void setNextImage() {
		if (isHit)
			return;
			//setImage(name + "_hit", true, false);
		else if (isJumping())
			setImage(name, false);
		else if (!isStill())
			setImage(name, false);
		else
			setImage(name, false);
	}

	/**
	 * Attack the target the enemy is following
	 */
	@Override
	public void attack() {
		// Do Nothing
		// Override for different actions on queuing attacks
	}

	/**
	 * React on notification of an actor entering this enemy's view area
	 */
	@Override
	protected void reactOnInView(Actor a) {
		System.out.println("woot");
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
	 * Check for collision with the entity
	 * 
	 * @param b
	 *            - bullet object fired by the player
	 * @return true if bullet has hit the enemy
	 */
	public boolean hit(Bullet b) {
		if (getMyRectangle().contains(b.getPosn())) {
			System.out.println("hit");
			if (b.getType() == this.getType()) {
				takeHit();
				this.timer = 2f;
				return true;
			}
		}
		return false;
	}

	public void takeHit() {
		super.takeHit();
		ai.hit();
	}

	/**
	 * @return Enemy's identifying type
	 */
	public Bullet.Mode getType() {
		return type;
	}
	
	public boolean inRange(Actor a)
	{
		//return this.getPosn().distance(a.getPosn()) <= ai.viewRange();
		
		if(this.getPosn().distance(a.getPosn()) <= ai.viewRange())
		{
			Vector2 enemyPosn = brickMan.worldToMap(this.getRealXPosn(), this.getRealYPosn());
			Vector2 actorPosn = brickMan.worldToMap(a.getRealXPosn(), a.getRealYPosn());
			Vector2[][] nextBrick;
			
			if(enemyPosn.y == actorPosn.y)
			{
				if(this.getDirection() == Direction.Right)
				{
					
					//Look out as far as the agent's view Range
					//Um...Only go so many bricks?
					//Not wanting to count the whole thing
					for(float i = 0; i < ai.viewRange()/brickMan.getBrickWidth(); i+=brickMan.getBrickWidth())
					{
						//Set up the means that it adds bricks to check vertically
						//I've changed this so many time  ~o.o~
						//Only going to trying doing one line of bricks at a time for now
						//Needs to work on the triangular fill
						if(brickMan.brickExists(new Vector2(i,enemyPosn.y)))
							return false;
						
					}
				}
				else
				{
					//Well if we're not facing Right, we're facing left
					for(float i = ai.viewRange()/brickMan.getBrickWidth(); i > 0; i-=brickMan.getBrickWidth())
					{
						if(brickMan.brickExists(new Vector2(i,enemyPosn.y)))
							return false;
					}
				}
			}
			else
			{
				for(float i = 0; i > ai.viewRange()/brickMan.getBrickHeight(); i-=brickMan.getBrickHeight())
				{
					if(brickMan.brickExists(new Vector2(enemyPosn.x,i)))
						return false;
				}
			}
			return true;
		}
		else
			return false;
		//*/
	}

	public EnemyAi getAI() {
		return ai;
	}

	@Override
	public void updateSprite() {
		super.updateSprite();
		ai.update(Game.getDeltaTime());
	}

	/**
	 * Updates on notifications from the world that the actor is observing
	 * 
	 * @param o
	 *            - object that sent the notification
	 * @param args
	 *            - type of notification
	 */
	@Override
	public void update(Observable o, Object args) {
		super.update(o, args);
		if (args instanceof Actor) {
			// only call ai updates against actors that are seen by this one
			if (visibility.containsKey(args)){
				if (visibility.get(args)) {
					ai.update((Actor) args);
				}
			}
		}
	}
}

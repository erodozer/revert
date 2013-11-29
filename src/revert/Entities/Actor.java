package revert.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import revert.MainScene.World;
import revert.MainScene.notifications.ActorsRemoved;
import revert.util.BrickManager;

import com.kgp.core.AssetsManager;
import com.kgp.imaging.Sprite;
import com.kgp.util.Vector2;

/**
 * Generic actor class for sprites that move within the world space and are animated with states
 * 
 * @author nhydock
 */
public abstract class Actor extends Sprite implements Observer{

	/**
	 * Facing direction of the actor
	 */
	public static enum Direction 
	{
		Left,
		Right;
	}
	
	/**
	 * How the actor is moving within the world horizontally
	 */
	public static enum Movement
	{
		Still,
		Left,
		Right;
	}
	
	/**
	 * How the actor is moving within the world vertically
	 */
	public static enum VertMovement
	{
		Grounded,
		Rising,
		Falling;
	}
	
	//states used for setting movement properties of the actor
	protected Movement moving;
	protected Direction facing;
	protected VertMovement vertMoveMode;
	
	//additional states
	protected boolean isHit;
	protected boolean isAttacking;
	
	//timer for how long the actor is in a state of reacting to being hit
	protected int hitTimer;
	
	//speed at which the sprite moves within the world
	protected float moveRate;
	
	//list of names used to associate as a spritesheet for the character
	protected String spritesheet;

	//Distance away that the actor can see another	
	protected double viewRange;
	
	//synchronization lock on actor updating
	protected boolean actorLock = false;
	
	//list of actors in range
	protected HashMap<Actor, Boolean> visibility;
	
	//all actors that this actor can interact with/watch
	protected ArrayList<Actor> actors;
	
	//the amount of times the actor can still be hit before dying
	protected int hp;
	
	//the world the actor is present within
	protected World world;

	//brick space the actor moves within
	// all actors move within tile maps
	protected BrickManager brickMan;

	//the current position of the sprite in the tilemap coordinates
	protected Vector2 map;
	// this sprite's height in tiles
	protected int tileHeight; 

	// max number of steps to take when rising upwards in a jump
	protected float maxVertTravel;
	// total vertical travel so far in a jump
	protected float vertTravel;
	// distance to move vertically in one step
	protected float vertStep; 


	public Actor(World w, String name, ArrayList<Actor> actors) {
		super(0, 0, w.getWidth(), w.getHeight(), AssetsManager.Images, name);
		this.world = w;
		this.actors = actors;
		this.visibility = new HashMap<Actor, Boolean>();
		for (Actor a : this.actors)
		{
			this.visibility.put(a, false);
		}
		
		this.velocity.x = 0;
		this.velocity.y = 0;
		
		this.brickMan = w.getLevel();

		this.map = new Vector2();
		
		this.stop();
	}
	
	public void setImage(String name)
	{
		super.setImage(name);
		
		if (this.brickMan != null)
			this.tileHeight = (int)(this.getHeight() / this.brickMan.getBrickHeight());
	}
	
	/**
	 * Get the image that the actor is suppose to switch to dependent on current states
	 */
	abstract protected String getNextImage();
	
	/**
	 * Tells the sprite to start moving to the left
	 */
	final public void moveLeft()
	{
		this.velocity.x = -moveRate;
		this.moving = Movement.Left;
		this.setImage(getNextImage(), true);
	}
	
	/**
	 * Tells the sprite to start moving to the right
	 */
	final public void moveRight()
	{
		this.velocity.x = moveRate;
		this.moving = Movement.Right;
		this.setImage(getNextImage(), true);
	}
	
	/**
	 * Stops horizontal movement
	 */
	final public void stop()
	{
		this.velocity.x = 0;
		this.moving = Movement.Still;
		this.setImage(getNextImage(), false);
	}
	
	/**
	 * Make the actor look towards a point and face in that direction
	 * @param v
	 */
	public void lookAt(Vector2 v)
	{
		Vector2 dir = this.getPosn().to(v);
		if (dir.x < position.x)
			faceLeft();
		else if (dir.x > position.x)
			faceRight();
	}
	
	/**
	 * Turns the actor left
	 */
	final public void faceLeft()
	{
		this.facing = Direction.Left;
		this.flipX = true;
	}
	
	/**
	 * Turns the actor right
	 */
	final public void faceRight()
	{
		this.facing = Direction.Right;
		this.flipX = false;
	}
	
	/**
	 * The sprite is asked to jump. It sets its vertMoveMode to RISING, and
	 * changes its image. The y- position adjustment is done in updateSprite().
	 */
	public void jump() {
		if (vertMoveMode == VertMovement.Grounded) {
			this.vertMoveMode = VertMovement.Rising;
			this.vertTravel = 0f;
			this.velocity.y = -vertStep;
			setImage(getNextImage(), false);
		}
	}

	/**
	 * Force the actor into a falling state
	 */
	public void fall() {
		if (vertMoveMode != VertMovement.Falling) {
			this.vertMoveMode = VertMovement.Falling;
			this.velocity.y = vertStep;
			setImage(getNextImage(), false);
		}
	}
	
	/**
	 * Have the actor invoke an attack
	 */
	abstract public void attack();
	
	/**
	 * @return the direction that the player is facing
	 */
	final public Direction getDirection()
	{
		return this.facing;
	}

	final public Movement getMovement()
	{
		return this.moving;
	}
	
	final public float getMoveRate()
	{
		return this.moveRate;
	}
	
	/**
	 * Updates what the actor sees and have them react to changes
	 */
	final protected void updateView()
	{
		while (actorLock);
		
		actorLock = true;
		
		for (Actor a : actors)
		{
			boolean sees = (a.position.distance(this.position) < viewRange);
			boolean vis = visibility.get(a);
			
			if (vis != sees)
			{
				visibility.put(a, sees);
				
				if (sees)
					reactOnInView(a);
				else
					reactOnOutOfView(a);
			}
		}
		
		actorLock = false;
		
		return ;
	}
	
	/**
	 * Have the actor do something once something is within their viewing range
	 */
	abstract protected void reactOnInView(Actor a);
	
	/**
	 * Have the actor do something once an actor is out of their viewing range
	 */
	abstract protected void reactOnOutOfView(Actor a);

	/**
	 * @return true if the actor has more than 0 hp
	 */
	final public boolean isAlive() {
		return this.hp > 0;
	}
	
	/**
	 * Check if the player is on the ground or moving in the air
	 * @return boolean
	 */
	final public boolean isJumping() {
		return this.vertMoveMode != VertMovement.Grounded;
	}
	
	/**
	 * @return true if the character is not moving
	 */
	final public boolean isStill()
	{
		return moving == Movement.Still;
	}
	
	final public boolean isHit()
	{
		return this.isHit;
	}
	
	/**
	 * Updates on notifications from the world that the actor is observing
	 * @param o - object that sent the notification
	 * @param args - type of notification
	 */
	@Override
	public void update(Observable o, Object args)
	{
		if (args instanceof ActorsRemoved)
		{
			ActorsRemoved a = (ActorsRemoved)args;
				
			for (Actor actor : a.actors)
			{
				this.visibility.remove(actor);
			}
		}
	}
}

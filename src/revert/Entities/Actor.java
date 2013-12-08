package revert.Entities;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import revert.MainScene.World;
import revert.MainScene.notifications.ActorsRemoved;
import revert.util.BrickManager;

import com.kgp.core.AssetsManager;
import com.kgp.core.Game;
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
	
	//speed at which the sprite moves within the world
	protected float moveRate;
	
	//list of names used to associate as a spritesheet for the character
	protected String spritesheet;

	//Distance away that the actor can see another	
	protected double viewRange;
	
	//synchronization lock on actor updating
	protected boolean actorLock = false;
	
	//list of actors in that are in range and can be interacted with
	protected HashMap<Actor, Boolean> visibility;
	
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

	// general timer used for mode switching/response
	protected float timer;
	
	public Actor(World w, String name) {
		super(0, 0, w.getWidth(), w.getHeight(), AssetsManager.Images, name);
		this.world = w;
		this.visibility = new HashMap<Actor, Boolean>();
		
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
			this.tileHeight = (int)(this.dimensions.height / this.brickMan.getBrickHeight());
	
		this.offset.x = -this.dimensions.width / 2;
		this.offset.y = -this.dimensions.height;
	}
	
	/**
	 * Get the image that the actor is suppose to switch to dependent on current states
	 */
	abstract protected void setNextImage();
	
	/**
	 * Tells the sprite to start moving to the left
	 */
	final public void moveLeft()
	{
		if (moving != Movement.Left) {
			velocity.x = -moveRate;
			moving = Movement.Left;
			setNextImage();
		}
	}
	
	/**
	 * Tells the sprite to start moving to the right
	 */
	final public void moveRight()
	{
		if (moving != Movement.Right) {
			velocity.x = moveRate;
			moving = Movement.Right;
			setNextImage();
		}
	}
	
	/**
	 * Stops horizontal movement
	 */
	final public void stop()
	{
		this.velocity.x = 0;
		this.moving = Movement.Still;
		setNextImage();
	}
	
	/**
	 * Make the actor look towards a point and face in that direction
	 * @param v
	 */
	public void lookAt(Vector2 v)
	{
		Vector2 dir = position.to(v).normalize();
		if (dir.x < 0)
			faceLeft();
		else if (dir.x > 0)
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
			setNextImage();
		}
	}

	/**
	 * Force the actor into a falling state
	 */
	public void fall() {
		if (vertMoveMode != VertMovement.Falling) {
			this.vertMoveMode = VertMovement.Falling;
			this.velocity.y = world.gravity;
			setNextImage();
		}
	}

	/**
	 * Stop jumping
	 */
	private void land() {
		this.vertMoveMode = VertMovement.Grounded;
		this.vertTravel = 0;
		this.velocity.y = 0;
		setNextImage();
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
	 * If the left/right move has put the sprite out in thin air, then put it
	 * into falling mode.
	 */
	protected void checkIfFalling() {
		// could the sprite move downwards if it wanted to?
		// test its center x-coord, base y-coord
		float yTrans = brickMan.checkBrickTop(this.getXPosn(), this.getYPosn(), world.gravity * Game.getDeltaTime());
		if (yTrans != 0) {
			fall();
		}
	}

	/**
	 * Rising will continue until the maximum number of vertical steps is
	 * reached, or the sprite hits the base of a brick. The sprite then switches
	 * to falling mode.
	 */
	protected void updateRising() {
		if (vertTravel >= maxVertTravel) {
			fall();
		}
		else {
			float yTrans = brickMan.checkBrickBase(this.getXPosn(), this.getYPosn() - this.getHeight(), vertStep * Game.getDeltaTime());
			if (yTrans <= 0) {
				fall();
			}
			else { // can move upwards another step
				vertTravel += yTrans;
				if (yTrans < vertStep * Game.getDeltaTime()) {
					this.velocity.y = -yTrans;
				}
			}
		}
	}

	/**
	 * Falling will continue until the sprite hits the top of a brick. The game
	 * only allows a brick ribbon which has a complete floor, so the sprite must
	 * eventually touch down.
	 * 
	 * Falling mode can be entered without a corresponding rising sequence, for
	 * instance, when the sprite walks off a cliff.
	 */
	protected void updateFalling() {
		float yTrans = brickMan.checkBrickTop(this.getXPosn(), this.getYPosn(), world.gravity * Game.getDeltaTime());
		if (yTrans < world.gravity * Game.getDeltaTime()) {
			this.velocity.y = yTrans;
			land();
		}
	}
	
	/**
	 * Looks at all bricks ahead of the character as he's traveling to ensure
	 * there is no collisions before moving ahead
	 * <p/>
	 * Automatically calculates the base tile ahead of the character and checks
	 * up the body of the character to find any collisions ahead in the direction
	 * that the he is moving.
	 * 
	 * @return true if collision occurs
	 */
	protected boolean checkAhead() {
		Vector2 nextBrick;
		// adjust to base and check brick ahead
		Vector2 p = position.clone();

		// adjust for visible offset
		if (moving == Movement.Right)
			p.x -= offset.x;
		else if (moving == Movement.Left)
			p.x += offset.x;

		p.x += this.velocity.x;
		p.y -= 1;

		nextBrick = brickMan.worldToMap(p.x, p.y);
		
		return this.checkAhead(nextBrick);
	}
	
	/**
	 * Looks at all bricks ahead of the character as he's traveling to ensure
	 * there is no collisions before moving ahead
	 * 
	 * @param nextBrick - should be the brick at the base of the character, as this
	 * 	method iterates up the length of the character to check for bricks/collissions
	 * 
	 * @return true if collision occurs
	 */
	private boolean checkAhead(Vector2 nextBrick) {
		// if the brick is the same as what we're currently on then we do
		// nothing and just let royer continue on his way across the brick
		if (nextBrick.equals(this.map))
			return false;

		// if the next brick exists, we check the brick above it to see if it's
		// empty
		for (int i = 0; i < this.tileHeight-1; i++) {
			nextBrick.y--;
			
			// if it isn't and we run into a wall and stop
			if (brickMan.brickExists(nextBrick)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Causes Royer to advance up the tilemap if the next tile is only 1 up Does
	 * not cause Royer to think he's jumping
	 */
	protected void stepNext() {
		Vector2 nextBrick;
		// adjust to base and check brick ahead
		Vector2 p = position.clone();

		// adjust for visible offset
		if (moving == Movement.Right)
			p.x -= offset.x;
		else if (moving == Movement.Left)
			p.x += offset.x;

		p.x += this.velocity.x * Game.getDeltaTime();
		p.y -= 1;

		nextBrick = brickMan.worldToMap(p.x, p.y);

		// if the brick is the same as what we're currently on then we do
		// nothing and just let royer continue on his way across the brick
		if (!brickMan.brickExists(nextBrick))
			return;

		if (brickMan.getBrickHeight() > this.getHeight() * .25 && checkAhead(nextBrick))
		{
			stop();
		}
		else
		{
			this.position.y -= brickMan.getBrickHeight();
		}
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
	
	public void takeHit()
	{
		hp--;
		isHit = true;
		timer = 2f;
		
		this.flash.color[0] = 1.0f;
		this.flash.color[1] = 0.0f;
		this.flash.color[2] = 0.0f;
		
		//stop();
		setNextImage();
	}
	
	abstract public boolean inRange(Actor a);
	
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
		else if (args instanceof Set)
		{
			Set<Actor> a = (Set<Actor>)args;
			for (Actor actor : a)
			{
				if (actor != this)
				{
					this.visibility.put(actor, false);
				}
			}
		}
		else if (args instanceof Actor)
		{
			Actor a = (Actor)args;
			
			if (visibility.containsKey(a))
			{
				boolean see = visibility.get(a);
				if (see)
				{
					if (!inRange(a))
					{
						visibility.put(a, false);
						this.reactOnOutOfView(a);
					}
				}
				else
				{
					if (inRange(a))
					{
						visibility.put(a, true);
						this.reactOnInView(a);
					}
				}
			}
		}
	}
	
	public void updateSprite()
	{
		if (isHit)
		{
			timer -= Game.getDeltaTime();
			if (timer < 0) {
				isHit = false;
			}
			
			if (!isHit)
				setNextImage();
		}
		
		if (!isStill()) { // moving
			if (!isJumping()) // if not jumping
			{
				checkIfFalling(); // may have moved out into empty space
				this.stepNext();
			}
			else
			{
				if (checkAhead())
				{
					stop();
				}
			}
		}
		else if (isAttacking)
		{
			timer -= Game.getPeriodInMSec();
			if (timer < 0)
			{
				isAttacking = false;
				stop();
			}
		}

		if (vertMoveMode == VertMovement.Rising)
			updateRising();
		else if (vertMoveMode == VertMovement.Falling)
			updateFalling();

		super.updateSprite();

		if (this.getXPosn() > world.getWidth()) {
			this.setPosition(this.getXPosn() - world.getWidth(), this.getYPosn());
		}
		else if (this.getXPosn() < 0) {
			this.setPosition(this.getXPosn() + world.getWidth(), this.getYPosn());
		}

		this.map = brickMan.worldToMap(this.getXPosn(), this.getYPosn());
	}
}

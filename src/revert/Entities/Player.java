package revert.Entities;

// JumperSprite.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* A sprite can move left/right, jump and stand still.
 In fact, a sprite doesn't move horizontally at all, so
 the left and right movement requests only change various
 status flags, not its locx value.

 The sprite has looping images for when it is moving
 left or right, and single images for when it is
 standing still or jumping.

 The sprite stores its world coordinate in (xWorld, yWorld).

 Jumping has a rising and falling component. Rising and 
 falling can be stopped by the sprite hitting a brick.

 The sprite's movement left or right can be stopped by hitting 
 a brick.

 A sprite will start falling if it walks off a brick into space.

 Brick queries (mostly about collision detection) are sent
 to the BricksManager object.
 */

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Observable;

import revert.MainScene.Controller;
import revert.MainScene.World;
import revert.MainScene.notifications.PlayerModeNotification;
import revert.MainScene.notifications.PlayerMovementNotification;

import com.kgp.imaging.ImagesLoader;
import com.kgp.level.BricksManager;
import com.kgp.util.Vector2;

public class Player extends Actor {
	private static double DURATION = 0.5; // secs
	// total time to cycle through all the images

	private static final int NOT_JUMPING = 0;
	private static final int RISING = 1;
	private static final int FALLING = 2;
	// used by vertMoveMode
	// (in J2SE 1.5 we could use a enumeration for these)

	private static final int MAX_UP_STEPS = 8;
	// max number of steps to take when rising upwards in a jump

	private int vertMoveMode;
	/* can be NOT_JUMPING, RISING, or FALLING */
	private int vertStep; // distance to move vertically in one step
	private int upCount;

	private BricksManager brickMan;

	private int tileHeight; //this sprite's height in tiles
	
	/*
	 * the current position of the sprite in the tilemap coordinates
	 */
	private Point map;

	/*
	 * the current point that the player is aiming at
	 */
	private Vector2 aim;
	
	private int mode;
	
	//general timer used for mode switching/response
	private int timer;
	
	private int worldY;

	private int ammo;

	public Player(World w, ImagesLoader imsLd) {
		super(w, "royer01", new ArrayList());
		
		this.brickMan = w.getLevel();
				
		// standing center screen, facing right
		this.moveRate = brickMan.getMoveSize();
		// the move size is the same as the bricks ribbon

		setVelocity(0, 0); // no movement
		
		this.duration = DURATION;
		
		/*
		 * Adjust the sprite's y- position so it is standing on the brick at its
		 * mid x- psoition.
		 */
		this.position.y = brickMan.findFloor(0, 0, false) - getHeight();
		this.aim = new Vector2(this.position.x + 10, this.position.y);
		
		//set a normal height from the initial standing position
		//this allows for landing in a somewhat natural looking animation
		this.tileHeight = this.getHeight() / w.getLevel().getBrickHeight();

		this.moving = Movement.Still;
		
		vertMoveMode = NOT_JUMPING;
		vertStep = brickMan.getBrickHeight() / 2;
		// the jump step is half a brick's height
		upCount = 0;
	}

	/*
	 * The sprite is asked to jump. It sets its vertMoveMode to RISING, and
	 * changes its image. The y- position adjustment is done in updateSprite().
	 */
	public void jump()
	{
		if (vertMoveMode == NOT_JUMPING) {
			vertMoveMode = RISING;
			upCount = 0;
			setImage(getNextImage(), false);
		}
	}

	public void updateSprite()
	/*
	 * Although the sprite is not moving in the x-direction, we must still
	 * update its (xWorld, yWorld) coordinate. Also, if the sprite is jumping
	 * then its y position must be updated with moveVertically(). updateSprite()
	 * should only be called after collsion checking with willHitBrick()
	 */
	{
		if (!isStill()) { // moving
			if (vertMoveMode == NOT_JUMPING) // if not jumping
				checkIfFalling(); // may have moved out into empty space
			
			this.stepNext();
		}

		// vertical movement has two components: RISING and FALLING
		if (vertMoveMode == RISING)
			updateRising();
		else if (vertMoveMode == FALLING)
			updateFalling();
		
		this.position.y = this.worldY - this.getHeight() - brickMan.getBrickHeight();
		
		super.updateSprite();
		
		if (this.getXPosn() > world.getWidth())
		{
			this.setPosition(this.getXPosn() - world.getWidth(), this.getYPosn());
		}
		else if (this.getXPosn() < 0)
		{
			this.setPosition(this.getXPosn() + world.getWidth(), this.getYPosn());
		}
		
		this.map = brickMan.worldToMap(this.getXPosn(), this.getYPosn());
		
	} // end of updateSprite()

	private void checkIfFalling()
	/*
	 * If the left/right move has put the sprite out in thin air, then put it
	 * into falling mode.
	 */
	{
		// could the sprite move downwards if it wanted to?
		// test its center x-coord, base y-coord
		int yTrans = brickMan.checkBrickTop(this.getXPosn(), this.worldY, vertStep);
		// System.out.println("checkIfFalling: " + yTrans);
		if (yTrans != 0) // yes it could
		{
			vertMoveMode = FALLING; // set it to be in falling mode
		}
	} // end of checkIfFalling()

	private void updateRising()
	/*
	 * Rising will continue until the maximum number of vertical steps is
	 * reached, or the sprite hits the base of a brick. The sprite then switches
	 * to falling mode.
	 */
	{
		if (upCount == MAX_UP_STEPS) {
			vertMoveMode = FALLING; // at top, now start falling
			upCount = 0;
		} else {
			int yTrans = brickMan.checkBrickBase(this.getXPosn(), worldY - this.getHeight(), vertStep);
			worldY -= yTrans; // update position
			if (yTrans <= 0) { // hit the base of a brick
				vertMoveMode = FALLING; // start falling
				upCount = 0;
			} else { // can move upwards another step
				upCount++;
			}
		}
	} // end of updateRising()

	/**
	 * Falling will continue until the sprite hits the top of a brick. The game
	 * only allows a brick ribbon which has a complete floor, so the sprite must
	 * eventually touch down.
	 * 
	 * Falling mode can be entered without a corresponding rising sequence, for
	 * instance, when the sprite walks off a cliff.
	 */
	private void updateFalling()
	{
		int yTrans = brickMan.checkBrickTop(this.getXPosn(), worldY, vertStep);
		worldY += yTrans;
		if (yTrans < vertStep)
		{
			finishJumping();
		}
	}
	
	/**
	 * Causes Royer to advance up the tilemap if the next tile is only 1 up
	 * Does not cause Royer to think he's jumping
	 */
	private void stepNext()
	{
		Point nextBrick;
		nextBrick = brickMan.worldToMap(this.getXPosn() + this.getXVelocity(), worldY - 1);
		
		//if the brick is the same as what we're currently on then we do nothing and just let
		// royer continue on his way across the brick
		if (nextBrick.equals(this.map) || !brickMan.brickExists(nextBrick))
			return;
		
		//if the next brick even exists, we check the brick above it to see if it's empty
		for (int i = 1; i < this.tileHeight; i++)
		{
			nextBrick = new Point(nextBrick.x, nextBrick.y - 1);
			
			//if it isn't, then we run into a wall and step
			if (brickMan.brickExists(nextBrick))
			{	
				this.stop();
				return;
			}
		}
		
		worldY -= brickMan.getBrickHeight(); // update position
	}

	private void finishJumping() {
		this.vertMoveMode = NOT_JUMPING;
		this.upCount = 0;
		
		setImage(getNextImage(), true);
	}

	/**
	 * @return the current attack mode of the player
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param i - the attack mode to set the player to
	 */
	private void setMode(int i) {
		this.mode = i;
	}

	/**
	 * Sets attack mode to one kind higher
	 */
	private void nextMode() {
		this.mode++;
		if (this.mode > 2)
			this.mode = 0;
	}
	
	/**
	 * Sets attack mode to one kind lower
	 */
	private void prevMode() {
		this.mode--;
		if (this.mode < 0)
			this.mode = 2;
	}

	public boolean isJumping() {
		return this.vertMoveMode != NOT_JUMPING;
	}

	@Override
	protected String getNextImage() {
		if (isJumping())
		{
			return "royer_jmp";
		}
		else if (!isStill())
		{
			return "royer_walking";
		}
		
		return "royer01";
	}

	/**
	 * Respond to firing a bullet
	 */
	@Override
	public void attack() {
		ammo--;
		timer = 100;
		isAttacking = true;
		stop();
		setImage(getNextImage(), false);
	}
	
	/**
	 * Check if player can attack
	 * @return boolean
	 */
	public boolean hasAmmo() {
		return ammo > 0;
	}
	
	/**
	 * Have the player look towards a point in the world
	 * @param p
	 */
	public void lookAt(Point2D target, AffineTransform m)
	{
		Point2D p = m.transform(new Point((int)position.x, (int)position.y), null);
		aim.x = (float) target.getX();
		aim.y = (float) target.getY();
		
		aim.translate((float)-p.getX(), (float)-p.getY());
		aim = aim.normalize();
		aim.mult(80);
		
		flipX = (aim.x < 0);
	}
	
	public Vector2 getAim()
	{
		return aim;
	}

	@Override
	protected void reactOnInView(Actor a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void reactOnOutOfView(Actor a) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Update on notifications
	 */
	public void update(Observable o, Object args)
	{
		super.update(o, args);
		if (o instanceof Controller)
		{
			//handle movement commands
			if (args instanceof PlayerMovementNotification)
			{
				PlayerMovementNotification note = (PlayerMovementNotification) args;
				
				if (note.jump)
				{
					jump();
				}
				else
				{
					if (note.movement == Movement.Left)
						moveLeft();
					else if (note.movement == Movement.Right)
						moveRight();
					else
						stop();
				}
			}
			//set attack mode
			else if (args instanceof PlayerModeNotification)
			{
				PlayerModeNotification note = (PlayerModeNotification) args;
				
				if (note.next)
				{
					nextMode();
				}
				else if (note.prev)
				{
					prevMode();
				}
				else
				{
					setMode(note.mode);
				}
			}
		}
	}
	
}


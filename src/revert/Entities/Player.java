package revert.Entities;

// JumperSprite.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/*
 * A sprite can move left/right, jump and stand still. In fact, a sprite doesn't
 * move horizontally at all, so the left and right movement requests only change
 * various status flags, not its locx value. The sprite has looping images for
 * when it is moving left or right, and single images for when it is standing
 * still or jumping. The sprite stores its world coordinate in (xWorld, yWorld).
 * Jumping has a rising and falling component. Rising and falling can be stopped
 * by the sprite hitting a brick. The sprite's movement left or right can be
 * stopped by hitting a brick. A sprite will start falling if it walks off a
 * brick into space. Brick queries (mostly about collision detection) are sent
 * to the BricksManager object.
 */

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Observable;

import revert.Entities.Actor.VertMovement;
import revert.Entities.Bullet.Mode;
import revert.MainScene.Controller;
import revert.MainScene.World;
import revert.MainScene.notifications.PlayerAttackNotification;
import revert.MainScene.notifications.PlayerModeNotification;
import revert.MainScene.notifications.PlayerMovementNotification;
import revert.MainScene.notifications.PlayerNotification;
import revert.util.BrickManager;

import com.kgp.core.Game;
import com.kgp.imaging.ImagesLoader;
import com.kgp.level.BricksManager;
import com.kgp.util.Vector2;

public class Player extends Actor {

	public static final int FULLAMMO = 6;
	public static final int MAXHP = 10;
	private static final float DURATION = 0.5f; // secs

	/*
	 * the current point that the player is aiming at
	 */
	private Vector2 aim;

	private Bullet.Mode mode;

	// general timer used for mode switching/response
	private int timer;

	private int ammo;

	public Player(World w, ImagesLoader imsLd) {
		super(w, "royer01", new ArrayList());

		// standing center screen, facing right
		// walks 8 tiles per second
		this.moveRate = (int) (brickMan.getBrickWidth() * 8 * Game.getDeltaTime());
		// the move size is the same as the bricks ribbon

		setVelocity(0, 0); // no movement

		this.duration = DURATION;

		// our position is the bottom center of the sprite
		this.position.y = brickMan.findFloor(0, 0, false);

		this.aim = new Vector2(this.position.x + 10, this.position.y);

		// set a normal height from the initial standing position
		// this allows for checking how tall the player is in tiles
		this.tileHeight = this.getHeight() / w.getLevel().getBrickHeight();

		this.moving = Movement.Still;

		this.velocity.y = 5;
		this.vertMoveMode = VertMovement.Falling;
		maxVertTravel = 80;
		// should be able to jump his max height in .5 sec
		vertStep = maxVertTravel * 2 * Game.getDeltaTime();
		vertTravel = 0f;

		this.hp = MAXHP;
		this.ammo = FULLAMMO;
		
		this.mode = Bullet.Mode.Copper;
	}
	
	public void init()
	{
		this.hp = MAXHP;
		this.ammo = FULLAMMO;
	
		this.stop();
		this.isAttacking = false;
		
		updateStatus();
	}

	/**
	 * Set image and readjust offset
	 */
	public void setImage(String name) {
		super.setImage(name);

		this.offset.x = -this.dimensions.width / 2;
		this.offset.y = -this.dimensions.height;
	}

	/**
	 * Stop jumping
	 */
	private void land() {
		this.vertMoveMode = VertMovement.Grounded;
		this.vertTravel = 0;
		this.velocity.y = 0;
		setImage(getNextImage(), true);
	}
	
	/**
	 * Sends a notification about the player's current states
	 * Mainly for the HUD
	 */
	public void updateStatus() {
		setChanged();
		notifyObservers(new PlayerNotification(this.hp, this.ammo, this.mode));
	}

	/**
	 * Although the sprite is not moving in the x-direction, we must still
	 * update its (xWorld, yWorld) coordinate. Also, if the sprite is jumping
	 * then its y position must be updated with moveVertically(). updateSprite()
	 * should only be called after collsion checking with willHitBrick()
	 */
	public void updateSprite() {
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

	/**
	 * If the left/right move has put the sprite out in thin air, then put it
	 * into falling mode.
	 */
	private void checkIfFalling() {
		// could the sprite move downwards if it wanted to?
		// test its center x-coord, base y-coord
		float yTrans = brickMan.checkBrickTop(this.getXPosn(), this.getYPosn(), vertStep);
		if (yTrans != 0) {
			fall();
		}
	}

	/**
	 * Rising will continue until the maximum number of vertical steps is
	 * reached, or the sprite hits the base of a brick. The sprite then switches
	 * to falling mode.
	 */
	private void updateRising() {
		if (vertTravel >= maxVertTravel) {
			fall();
		}
		else {
			float yTrans = brickMan.checkBrickBase(this.getXPosn(), this.getYPosn() - this.getHeight(), vertStep);
			if (yTrans <= 0) {
				fall();
			}
			else { // can move upwards another step
				vertTravel += yTrans;
				if (yTrans < vertStep) {
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
	private void updateFalling() {
		float yTrans = brickMan.checkBrickTop(this.getXPosn(), this.getYPosn(), vertStep);
		if (yTrans < vertStep) {
			this.position.y += yTrans;
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
	private boolean checkAhead() {
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
	private void stepNext() {
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
	 * @return the current attack mode of the player
	 */
	public Bullet.Mode getMode() {
		return mode;
	}

	/**
	 * @param i - the attack mode to set the player to
	 */
	private void setMode(int i) {
		this.mode = Bullet.Mode.values()[i-1];
	}

	/**
	 * Sets attack mode to one kind higher
	 */
	private void nextMode() {
		this.mode = this.mode.getNext();
	}

	/**
	 * Sets attack mode to one kind lower
	 */
	private void prevMode() {
		this.mode = this.mode.getPrev();
	}

	@Override
	protected String getNextImage() {
		if (isAttacking){
			return "royer_atk";
		}
		else if (isJumping()) {
			return "royer_jmp";
		}
		else if (!isStill()) {
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
		timer = 1000;
		isAttacking = true;
		stop();
		setImage(getNextImage(), false);
		updateStatus();
	}

	private void reload() {
		ammo = FULLAMMO;
		updateStatus();
	}

	/**
	 * Check if player can attack
	 * 
	 * @return boolean
	 */
	public boolean hasAmmo() {
		return ammo > 0;
	}

	/**
	 * Have the player look towards a point in the world
	 * 
	 * @param p
	 */
	public void lookAt(Vector2 target, AffineTransform m) {
		Vector2 p = new Vector2();
		m.transform(new Point((int) (position.x + offset.x), (int) (position.y + offset.y)), p);
		aim.x = target.x;
		aim.y = target.y;

		aim.translate((float) -p.x, (float) -p.y);
		aim = aim.normalize();
		aim.mult(80);
		
		lookAt(aim);
	}

	public Vector2 getAim() {
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
	public void update(Observable o, Object args) {
		super.update(o, args);
		if (o instanceof Controller) {
			// handle movement commands
			if (args instanceof PlayerMovementNotification) {
				PlayerMovementNotification note = (PlayerMovementNotification) args;

				if (!isAttacking)
				{
					if (note.jump) {
						jump();
					}
					else {
						if (note.movement == Movement.Left) {
							moveLeft();
						}
						else if (note.movement == Movement.Right) {
							moveRight();
						}
						else {
							stop();
						}
					}
				}
			}
			// set attack mode
			else if (args instanceof PlayerModeNotification) {
				PlayerModeNotification note = (PlayerModeNotification) args;

				if (note.next) {
					nextMode();
				}
				else if (note.prev) {
					prevMode();
				}
				else {
					setMode(note.mode);
				}
				
				updateStatus();
			}
			else if (args instanceof PlayerAttackNotification) {
				if (hasAmmo()) {
					attack();
				}
				else {
					reload();
				}
			}
		}
	}

}

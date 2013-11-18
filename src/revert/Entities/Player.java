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

import revert.MainScene.Controller;
import revert.MainScene.World;
import revert.MainScene.notifications.PlayerAttackNotification;
import revert.MainScene.notifications.PlayerModeNotification;
import revert.MainScene.notifications.PlayerMovementNotification;
import revert.util.BrickManager;

import com.kgp.core.Game;
import com.kgp.imaging.ImagesLoader;
import com.kgp.level.BricksManager;
import com.kgp.util.Vector2;

public class Player extends Actor {

	private static final float DURATION = 0.5f; // secs

	// max number of steps to take when rising upwards in a jump
	private VertMovement vertMoveMode;
	private final float maxVertTravel;
	private float vertTravel;
	private float vertStep; // distance to move vertically in one step

	private BrickManager brickMan;

	private int tileHeight; // this sprite's height in tiles

	/*
	 * the current position of the sprite in the tilemap coordinates
	 */
	private Vector2 map;

	/*
	 * the current point that the player is aiming at
	 */
	private Vector2 aim;

	private int mode;

	// general timer used for mode switching/response
	private int timer;

	private static final int FULLAMMO = 6;
	private int ammo;

	private int yOffset;

	public Player(World w, ImagesLoader imsLd) {
		super(w, "royer01", new ArrayList());

		this.brickMan = w.getLevel();

		// standing center screen, facing right
		// walks 8 tiles per second
		this.moveRate = (int) (brickMan.getBrickWidth() * 8 * Game.getDeltaTime());
		// the move size is the same as the bricks ribbon

		setVelocity(0, 0); // no movement

		this.duration = DURATION;

		this.yOffset = this.getHeight();

		// our position is the bottom center of the sprite
		this.position.y = brickMan.findFloor(0, 0, false);

		this.aim = new Vector2(this.position.x + 10, this.position.y);

		// set a normal height from the initial standing position
		// this allows for checking how tall the player is in tiles
		this.tileHeight = this.getHeight() / w.getLevel().getBrickHeight();

		this.moving = Movement.Still;

		vertMoveMode = VertMovement.Grounded;
		maxVertTravel = brickMan.getBrickHeight() * 4;
		// should be able to jump his max high in .5 sec
		vertStep = maxVertTravel * 2 * Game.getDeltaTime();
		vertTravel = 0f;

		this.ammo = FULLAMMO;
	}

	/**
	 * Set image and readjust offset
	 */
	public void setImage(String name) {
		super.setImage(name);

		this.offset.x = -this.getWidth() / 2;
		this.offset.y = -this.getHeight();
	}

	/**
	 * Check if the player's vert mode is
	 * 
	 * @return
	 */
	public boolean isJumping() {
		return this.vertMoveMode != VertMovement.Grounded;
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
	 * Stop jumping
	 */
	private void land() {
		this.vertMoveMode = VertMovement.Grounded;
		this.vertTravel = 0;
		this.velocity.y = 0;
		setImage(getNextImage(), true);
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
				checkIfFalling(); // may have moved out into empty space
			this.stepNext();
		}

		if (vertMoveMode == VertMovement.Rising)
			updateRising();
		else if (vertMoveMode == VertMovement.Falling)
			updateFalling();

		super.updateSprite();
		System.out.print(this.position);

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
		if (nextBrick.equals(this.map) || !brickMan.brickExists(nextBrick))
			return;

		// if the next brick exists, we check the brick above it to see if it's
		// empty
		for (int i = 1; i < this.tileHeight; i++) {
			nextBrick = new Vector2(nextBrick.x, nextBrick.y - 1);

			// if it isn't and we run into a wall and stop
			if (brickMan.brickExists(nextBrick)) {
				stop();
				return;
			}
		}

		// shift up a step
		this.position.y -= brickMan.getBrickHeight();
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

	@Override
	protected String getNextImage() {
		if (isJumping()) {
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
		timer = 100;
		isAttacking = true;
		stop();
		setImage(getNextImage(), false);
	}

	private void reload() {

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
	public void lookAt(Point2D target, AffineTransform m) {
		Point2D p = m.transform(new Point((int) (position.x + offset.x), (int) (position.y + offset.y)), null);
		aim.x = (float) target.getX();
		aim.y = (float) target.getY();

		aim.translate((float) -p.getX(), (float) -p.getY());
		aim = aim.normalize();
		aim.mult(80);

		flipX = (aim.x < 0);
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

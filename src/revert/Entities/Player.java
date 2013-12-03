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
import java.util.ArrayList;
import java.util.Observable;

import revert.MainScene.Controller;
import revert.MainScene.World;
import revert.MainScene.notifications.PlayerAttackNotification;
import revert.MainScene.notifications.PlayerModeNotification;
import revert.MainScene.notifications.PlayerMovementNotification;
import revert.MainScene.notifications.PlayerNotification;

import com.kgp.core.Game;
import com.kgp.imaging.ImagesLoader;
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
		
		this.mode = Bullet.Mode.Gold;
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
	 * Sends a notification about the player's current states
	 * Mainly for the HUD
	 */
	public void updateStatus() {
		setChanged();
		notifyObservers(new PlayerNotification(this.hp, this.ammo, this.mode));
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
		this.mode = Bullet.Mode.values()[i];
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
	protected void setNextImage() {
		if (isAttacking){
			this.setImage("royer_atk", false);
		}
		else if (isJumping()) {
			this.setImage("royer_jmp", false);
		}
		else if (!isStill()) {
			this.setImage("royer_walking", true);
		}
		else
		{
			this.setImage("royer01", false);
		}
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
		setNextImage();
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

	@Override
	public boolean inRange(Actor a) {
		return false;
	}

}

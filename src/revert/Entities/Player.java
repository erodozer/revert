package revert.Entities;

import java.util.Observable;

import revert.MainScene.Controller;
import revert.MainScene.World;
import revert.MainScene.notifications.PlayerAttackNotification;
import revert.MainScene.notifications.PlayerModeNotification;
import revert.MainScene.notifications.PlayerMovementNotification;
import revert.MainScene.notifications.PlayerNotification;

import com.kgp.imaging.ImagesLoader;
import com.kgp.util.Vector2;

public class Player extends Actor {

	public static final int FULLAMMO = 6;
	public static final int MAXHP = 100;
	private static final float DURATION = 0.5f; // secs

	/*
	 * the current point that the player is aiming at
	 */
	private Vector2 aim;

	private Bullet.Mode mode;

	private int ammo;

	public Player(World w, ImagesLoader imsLd) {
		super(w, "royer01");

		// standing center screen, facing right
		// walks 8 tiles per second
		this.moveRate = (int) (brickMan.getBrickWidth() * 8);
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

		maxVertTravel = 150;
		// should be able to jump his max height in .5 sec
		vertStep = maxVertTravel/2;
		vertTravel = 0f;

		this.hp = MAXHP;
		this.ammo = FULLAMMO;

		this.mode = Bullet.Mode.Gold;
		
		fall();
	}

	public void init() {
		this.hp = MAXHP;
		this.ammo = FULLAMMO;

		this.stop();
		this.isAttacking = false;

		updateStatus();
	}
	
	public void takeHit() {
		super.takeHit();
		updateStatus();
	}

	/**
	 * Sends a notification about the player's current states Mainly for the HUD
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
	 * @param i
	 *            - the attack mode to set the player to
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

		if (isHit) {
			this.setImage("royer_hit", true, false);
		} else if (isAttacking) {
			this.setImage("royer_atk", true, false);
		} else if (isJumping()) {
			this.setImage("royer_jmp", false);
		} else if (!isStill()) {
			this.setImage("royer_walking", true);
		} else {
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
    public void lookAt(Vector2 target) {
            super.lookAt(target);
            
            aim = this.position.to(target);
            aim = aim.normalize();
            aim.mult(80);
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

				if (!isAttacking) {
					if (note.jump) {
						jump();
					} else {
						if (note.movement == Movement.Left) {
							moveLeft();
						} else if (note.movement == Movement.Right) {
							moveRight();
						} else {
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
				} else if (note.prev) {
					prevMode();
				} else {
					setMode(note.mode);
				}

				updateStatus();
			} else if (args instanceof PlayerAttackNotification) {
				if (hasAmmo()) {
					attack();
				} else {
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

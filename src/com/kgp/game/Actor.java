package com.kgp.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import com.kgp.game.World.ActorsRemoved;
import com.kgp.imaging.ImagesLoader;
import com.kgp.imaging.Sprite;

/**
 * Generic actor class for sprites that move within the world space and are animated with states
 * 
 * @author nhydock
 */
public abstract class Actor extends Sprite implements Observer{

	public static enum Direction 
	{
		Left,
		Right;
	}
	
	public static enum Movement
	{
		Still,
		Left,
		Right;
	}
	
	//states used for setting movement properties of the actor
	protected Movement moving;
	protected Direction facing;
	
	//additional states
	protected boolean isHit;
	protected boolean isAttacking;
	
	//speed at which the sprite moves within the world
	protected int moveRate;
	
	//list of names used to associate as a spritesheet for the character
	protected String spritesheet;
	
	//vars used in looping images
	protected int period;
	protected int duration;
	

	/**
	 * Distance away that the actor can see another
	 */
	protected double viewRange;
	
	//synchronization lock on actor updating
	protected boolean actorLock = false;
	
	//list of actors in range
	protected HashMap<Actor, Boolean> visibility;
	
	//all actors that this actor can interact with/watch
	protected ArrayList<Actor> actors;
	protected int hp;
	
	protected World world;
	
	public Actor(int x, int y, int w, int h, ImagesLoader imsLd, String name, ArrayList<Actor> actors) {
		super(x, y, w, h, imsLd, name);
		this.actors = actors;
		this.visibility = new HashMap<Actor, Boolean>();
		for (Actor a : this.actors)
		{
			this.visibility.put(a, false);
		}
	}
	
	/**
	 * Tells the sprite to start moving to the left
	 */
	public void moveLeft()
	{
		this.setVelocity(-this.moveRate, this.getYVelocity());
		this.moving = Movement.Left;
		this.setImage(getNextImage());
	}
	
	/**
	 * Get the image that the actor is suppose to switch to dependent on current states
	 */
	abstract protected String getNextImage();
	
	public void setImage(String name, boolean loop)
	{
		super.setImage(name);
		super.loopImage(period, duration);
	}
	
	/**
	 * Tells the sprite to start moving to the right
	 */
	public void moveRight()
	{
		this.setVelocity(this.moveRate, this.getYVelocity());
		this.moving = Movement.Right;
	}
	
	/**
	 * Stops horizontal movement
	 */
	private void stop()
	{
		this.setVelocity(0, this.getYVelocity());
		this.moving = Movement.Still;
	}
	
	/**
	 * Turns the actor left
	 */
	public void faceLeft()
	{
		this.facing = Direction.Left;
	}
	
	/**
	 * Turns the actor right
	 */
	public void faceRight()
	{
		this.facing = Direction.Right;
	}
	
	/**
	 * @return true if the character is not moving
	 */
	public boolean isStill()
	{
		return moving == Movement.Still;
	}
	
	/**
	 * Have the actor invoke an attack
	 */
	abstract public void attack();
	
	/**
	 * @return the direction that the player is facing
	 */
	final private Direction getDirection()
	{
		return this.facing;
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

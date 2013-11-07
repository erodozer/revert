package com.kgp.game;

import java.util.ArrayList;
import java.util.Observable;

/**
 * World class that keeps track of all entities and alterations to the world
 * @author nhydock
 *
 */
public class World extends Observable{

	JumperSprite player;
	ArrayList<Enemy> enemies;
	ArrayList<ArrayList<Enemy>> waves;
	ArrayList<Actor> allActors;
	ArrayList<Bullet> bullets;
	
	public void update()
	{
		ActorsRemoved action = new ActorsRemoved();
		
		for (Actor a : allActors)
		{
			if (!a.isAlive())
			{
				if (a instanceof Enemy)
				{
					enemies.remove(a);
					action.actors.add(a);
					this.setChanged();
				}
			}
			else
			{
				a.updateSprite();
			}
		}
		
		this.notifyObservers(action);
		
		
		/**
		 * Perform bullet update
		 */
		for (Bullet b : bullets)
		{
			b.updateSprite();
		}
		
		if (enemies.size() < 0 && waves.size() > 0)
		{
			this.enemies = waves.remove(0);
			this.startWave();
		}
	}
	
	public void startWave()
	{
		// TODO implement wave startup
	}
	
	public class ActorsRemoved 
	{
		ArrayList<Actor> actors;
		
		{
			actors = new ArrayList<Actor>();
		}
	}
}

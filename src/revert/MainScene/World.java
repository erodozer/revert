package revert.MainScene;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

import revert.Entities.*;
import revert.MainScene.notifications.ActorsRemoved;

import com.kgp.core.GamePanel;
import com.kgp.level.BricksManager;

/**
 * World class that keeps track of all entities and alterations to the world
 * @author nhydock
 *
 */
public class World extends Observable{

	/**
	 * The playable entity in the world
	 */
	Player player;
	
	/**
	 * All the current enemies available for attacking
	 */
	ArrayList<Enemy> enemies;
	
	/**
	 * All the waves of enemies queued for the world
	 */
	int waves;
	int currentWave;
	
	/**
	 * All the actors added to this world
	 */
	ArrayList<Actor> allActors;
	
	/**
	 * Projectiles sent by the player
	 */
	ArrayList<Bullet> bullets;
	

	//tiles of the level
	private BricksManager level;

	private EnemyFactory enemyFactory;
	
	private int period;

	public World(int p)
	{
		this.period = p;
		this.enemies = new ArrayList<Enemy>();
		this.allActors = new ArrayList<Actor>();
		this.bullets = new ArrayList<Bullet>();
	}
	
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
			for (Enemy e : enemies)
			{
				if (b.getPosn().distance(e.getPosn()) < 10)
				{
					e.hit(b);
				}
			}
			
		}
		
		/*
		if (enemies.size() < 0 && currentWave < waves)
		{
			this.enemies = genEnemies(this.enemyFactory.createWave(10));
			this.startWave();
		}
		*/
		
		this.level.update(this.player.getPosn());
	}
	
	/**
	 * Create enemies from a list of type data
	 * @param waveData - array formatted in [enemyNum][x, y, type]
	 * @return ArrayList of instantiated enemy objects in the world
	 */
	public ArrayList<Enemy> genEnemies(int[][] waveData)
	{
		ArrayList<Enemy> e = new ArrayList<Enemy>();
		
		// TODO generate the enemies
		
		return e;
	}
	
	public void startWave()
	{
		// TODO implement wave startup
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setLevel(BricksManager bricksMan) {
		this.level = bricksMan;
		
		Point[] spawnPoints = new Point[10];
		
		this.enemyFactory = new EnemyFactory(this, spawnPoints);
	}
	
	public BricksManager getLevel()
	{
		return this.level;
	}
	
	/**
	 * Start a new game
	 */
	public void init()
	{
		this.waves = 5;
	}

	public int getHeight() {
		return this.level.getHeight();
	}
	
	public int getWidth() {
		return this.level.getWidth();
	}

	/**
	 * @return the player being managed by the world
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * @return the list of enemies currently being managed by the world
	 */
	public ArrayList<Enemy> getEnemies() {
		return this.enemies;
	}
	
	/**
	 * Renders the current state of the world to the screen
	 * @param g
	 */
	public void display(Graphics2D g)
	{
		this.level.display(g);
		this.player.drawSprite(g);
		
		for (Enemy e : enemies)
		{
			e.drawSprite(g);
		}
		
		for (Bullet b : bullets)
		{
			b.drawSprite(g);
		}
	}

	public int getPeriod() {
		return period;
	}
	
}

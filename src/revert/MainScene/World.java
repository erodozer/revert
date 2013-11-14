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
	
	/**
	 * Perform update operations on the objects in the world
	 */
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
		ArrayList<Enemy> list = new ArrayList<Enemy>();
		
		// TODO generate the enemies
		for (int i = 0; i < waveData.length; i++)
		{
			int[] data = waveData[i];
			Enemy e = enemyFactory.generateEnemy(data[2]);
			e.setPosition(data[0], data[1]);
			list.add(e);
		}
		
		return list;
	}
	
	
	/**
	 * Sets the enemies for the level
	 */
	public void startWave()
	{
		// TODO implement wave startup
		this.enemies = genEnemies(enemyFactory.createWave(15));
		currentWave++;
		waves--;
	}

	/**
	 * Sets the user controlled player entity belonging to this world instance
	 * @param player
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Sets the brick level of the world
	 * @param bricksMan
	 */
	public void setLevel(BricksManager bricksMan) {
		this.level = bricksMan;
		this.enemyFactory = new EnemyFactory(this, bricksMan.getSpawnPoints());
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

	/**
	 * @return the world's pixel width (same as the level's)
	 */
	public int getHeight() {
		return this.level.getMapHeight();
	}
	
	/**
	 * @return the world's pixel height (same as the level's)
	 */
	public int getWidth() {
		return this.level.getMapWidth();
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

	/**
	 * @return game update rate
	 */
	public int getPeriod() {
		return period;
	}
	
}

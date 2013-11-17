package revert.MainScene;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Observable;

import revert.Entities.Actor;
import revert.Entities.Bullet;
import revert.Entities.Enemy;
import revert.Entities.EnemyFactory;
import revert.Entities.Player;
import revert.MainScene.notifications.ActorsRemoved;

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

	public World()
	{
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
		
		for (int i = 0, n = allActors.size(); i < n;)
		{
			Actor a = allActors.get(i);
			if (!a.isAlive())
			{
				if (a instanceof Enemy)
				{
					enemies.remove(i);
					allActors.remove(a);
					action.actors.add(a);
					this.setChanged();
					n--;
				}
			}
			else
			{
				a.updateSprite();
				i++;
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
			if ((b.getXPosn() > player.getPosn().x + player.getPWidth()/2) ||
			   (b.getXPosn() < player.getPosn().x - player.getPWidth()/2) ||
			   (b.getYPosn() > player.getPosn().y + player.getPHeight()/2) ||
			   (b.getYPosn() < player.getPosn().y - player.getPHeight()/2)){
				
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
		this.allActors.addAll(this.enemies);
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

	public void add(Bullet b) {
		this.bullets.add(b);
	}
	
	public void add(Enemy e) {
		this.enemies.add(e);
		this.allActors.add(e);
	}
}

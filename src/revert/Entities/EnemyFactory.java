package revert.Entities;

import com.kgp.util.Vector2;

import revert.AI.*;
import revert.MainScene.World;


public class EnemyFactory {

	Vector2[] spawnPoints;
	World world;
		
	/**
	 * Create a factory for the world that will generate enemies at spawn points
	 * @param world
	 * @param spawns
	 */
	public EnemyFactory(World world, Vector2... spawns)
	{
		this.spawnPoints = spawns;
		this.world = world;
	}
	
	/**
	 * Create a data set array
	 * @param size
	 * @return
	 */
	public int[][] createWave(int size)
	{
		int[][] wave = new int[size][];
		
		for (int i = 0; i < size; i++)
		{
			Vector2 loc = spawnPoints[(int)(Math.random()*spawnPoints.length)];
			
			int[] n = {(int)loc.x, (int)loc.y, (int)(Math.random()*3)};
			
			wave[i] = n;
		}
		
		return wave;
	}
	
	/**
	 * Creates an enemy with an AI type
	 * @param type
	 * @return
	 */
	public Enemy generateEnemy(int type)
	{
		Enemy e = new Enemy(world, world.getPlayer(), type);
		return e;
	}
}

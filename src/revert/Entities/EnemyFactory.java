package revert.Entities;

import java.awt.Point;

import revert.AI.EnemyAi;
import revert.MainScene.World;


public class EnemyFactory {

	Point[] spawnPoints;
	World world;
		
	public EnemyFactory(World world, Point... spawns)
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
			Point loc = spawnPoints[(int)(Math.random()*spawnPoints.length)];
			
			int[] n = {loc.x, loc.y, (int)(Math.random()*3)};
			
			wave[i] = n;
		}
		
		return wave;
	}
	
	public Enemy generateEnemy(int type)
	{
		Enemy e = new Enemy(world, world.getPlayer());
		EnemyAi ai = null;
		if (type == 0)
		{
			//ai = new PassiveAi();
		}
		else
		{
			System.out.println("No enemy type corresponds to value: " + type + ".  Instantiating basic enemy");
		}
		
		e.setAI(ai);
		
		return e;
	}
}

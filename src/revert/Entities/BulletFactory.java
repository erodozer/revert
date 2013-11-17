package revert.Entities;

import com.kgp.util.Vector2;

import revert.MainScene.World;

public class BulletFactory {

	Player parent;
	World world;
	
	public BulletFactory(World world, Player p)
	{
		this.world = world;
		this.parent = p;
	}
	
	public Bullet spawnBullet()
	{
		Bullet b = new Bullet(world, parent.getMode());
		Vector2 v = parent.getPosn().clone();
		v.add(parent.getAim());
		b.setPosition(v.x, v.y);
		
		v = parent.getAim().clone();
		v.normalize();
		v.mult(20);
		b.setVelocity(v.x, v.y);
		
		return b;
	}
	
}

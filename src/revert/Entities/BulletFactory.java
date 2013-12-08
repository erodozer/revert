package revert.Entities;

import revert.MainScene.World;

import com.kgp.util.Vector2;

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
		Vector2 v = new Vector2(parent.getCenterXPosn(), parent.getCenterYPosn());
		
		b.setPosition(v.x, v.y);
		
		v = parent.getAim().clone();
		v.normalize();
		v.mult(10f);
		b.setVelocity(v.x, v.y);
		
		b.setOrientation(v.angle());
		
		return b;
	}
	
}

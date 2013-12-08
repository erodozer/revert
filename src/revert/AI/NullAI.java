package revert.AI;

import java.util.HashSet;
import java.util.Set;

import revert.Entities.Actor;

/**
 * Enemy AI that does nothing
 * @author nhydock
 */
public class NullAI implements EnemyAi{

	Set<Actor> aggressors = new HashSet<Actor>();
	
	@Override
	public void attack(Actor a) {
		// Do Nothing
	}

	@Override
	public void inView(Actor a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outOfView(Actor a) {
		// Do Nothing
	}

	@Override
	public void aggress(Actor a) {
		// Do Nothing
	}

	@Override
	public float viewRange() {
		return -1;
	}

	@Override
	public float aggressRange() {
		return -1;
	}

	@Override
	public float attackRate() {
		return -1f;
	}

	@Override
	public float attackRange() {
		return -1;
	}

	@Override
	public void walk() {
		// Do nothing
	}

	@Override
	public boolean isAgro() {
		return false;
	}

	@Override
	public void hit() {
		//Do nothing
	}

	@Override
	public void update(Actor a) {
		//Do nothing
	}

	@Override
	public void update(float delta) {
		// DO NOTHING
	}

	@Override
	public Set<Actor> getAggressors() {
		return aggressors;
	}
}

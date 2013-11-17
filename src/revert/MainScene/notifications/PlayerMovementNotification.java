package revert.MainScene.notifications;

import revert.Entities.Actor.Movement;

public class PlayerMovementNotification {

	public final Movement movement;
	public final boolean jump;
	
	public PlayerMovementNotification(Movement m)
	{
		this.movement = m;
		this.jump = false;
	}
	
	public PlayerMovementNotification(boolean jump)
	{
		this.jump = jump;
		this.movement = Movement.Still;
	}
	
}

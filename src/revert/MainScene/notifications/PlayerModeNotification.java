package revert.MainScene.notifications;

public class PlayerModeNotification {

	public final int mode;
	public final boolean prev;
	public final boolean next;
	
	public PlayerModeNotification(int mode)
	{
		this.mode = mode;
		this.prev = false;
		this.next = false;
	}
	
	public PlayerModeNotification(boolean next)
	{
		this.mode = 0;
		this.prev = !next;
		this.next = next;
	}
	
}

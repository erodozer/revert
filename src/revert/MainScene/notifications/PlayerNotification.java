package revert.MainScene.notifications;

/**
 * Notification for updating the HUD with player information
 * @author nhydock
 *
 */
public class PlayerNotification {

	private static final String HP_FMT = "# %d/%d";
	
	public final String hp;
	public final int ammo;
	
	public PlayerNotification(final int hp, final int ammo)
	{
		this.hp = String.format(HP_FMT, hp);
		this.ammo = ammo;
	}
	
}

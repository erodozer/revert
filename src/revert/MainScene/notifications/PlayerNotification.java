package revert.MainScene.notifications;

import revert.Entities.Player;

/**
 * Notification for updating the HUD with player information
 * @author nhydock
 *
 */
public class PlayerNotification {

	private static final String HP_FMT = "# %d/%d";
	
	public final String hp;
	public final int ammo;
	public final int mode;
	
	public PlayerNotification(final int hp, final int ammo, final int mode)
	{
		this.hp = String.format(HP_FMT, hp, Player.MAXHP);
		this.ammo = ammo;
		this.mode = mode;
	}
	
}

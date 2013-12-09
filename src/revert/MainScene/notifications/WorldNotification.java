package revert.MainScene.notifications;

/**
 * Notification type of the game's scoring mechanics
 * 
 * @author nhydock
 */
public class WorldNotification {

	private static final String SCORE_FMT = "$\n%d";
	private static final String TIME_FMT = "%02d";
	
	public final String score;
	public final String time;

	public WorldNotification(final int score, final float time)
	{
		this.score = String.format(SCORE_FMT, score);
		//make sure to convert time from ms to sec
		this.time = String.format(TIME_FMT, (int)(time));
	}
	
	public WorldNotification(final int score, final int time)
	{
		this.score = String.format(SCORE_FMT, score);
		//make sure to convert time from ms to sec
		this.time = String.format(TIME_FMT, (int)(time/1000));
	}
}

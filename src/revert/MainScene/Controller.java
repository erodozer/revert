package revert.MainScene;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import revert.Entities.Actor.Movement;
import revert.Entities.Player;
import revert.MainScene.notifications.PlayerAttackNotification;
import revert.MainScene.notifications.PlayerModeNotification;
import revert.MainScene.notifications.PlayerMovementNotification;

import com.kgp.core.GameController;
import com.kgp.core.GamePanel;
import com.kgp.core.GameState;
import com.kgp.util.Vector2;

/**
 * Main controller class for handling the game input that alters the player in the world
 * @author Nicholas Hydock
 *
 */
public class Controller extends GameController {

	Player player;
	Crosshair cross;
	
	GamePanel panel;
	World world;

	Vector2 mouse = new Vector2();
	
	/**
	 * Creates a game controller that interacts directly with the player
	 * 
	 * @param p
	 * @param c
	 */
	public Controller(Player p, Crosshair c, GamePanel panel, World world) {
		this.player = p;
		this.cross = c;
		this.panel = panel;
		this.world = world;
	}

	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		mouse.x = x - panel.getWidth()/2f + player.getCenterXPosn();
		mouse.y = y - panel.getHeight()/2f + player.getCenterYPosn();
		
		this.player.lookAt(mouse);
	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		// game-play keys
		if (panel.getState() == GameState.Active) {
			PlayerMovementNotification note = null;
			// move the sprite and ribbons based on the arrow key pressed
			if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A )
			{
				note = new PlayerMovementNotification(Movement.Left);
				setChanged();
			}
			else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D )
			{
				note = new PlayerMovementNotification(Movement.Right);
				setChanged();
			}
			else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_SPACE)
			{
				note = new PlayerMovementNotification(true);
				setChanged();
			}
			else if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN)
			{
				note = new PlayerMovementNotification(Movement.Still);
				setChanged();
			}
			
			this.notifyObservers(note);
		}
	}

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		
		Object note = null;
		
		if (panel.getState() == GameState.Active) {
			/*
			 * Set attack modes
			 */
			if (keyCode == KeyEvent.VK_SHIFT) {
				note = new PlayerModeNotification(true);
				setChanged();
			} 
			else if (keyCode == KeyEvent.VK_1) {
				note = new PlayerModeNotification(0);
				setChanged();
			} 
			else if (keyCode == KeyEvent.VK_2) {
				note = new PlayerModeNotification(1);
				setChanged();
			} 
			else if (keyCode == KeyEvent.VK_3) {
				note = new PlayerModeNotification(2);
				setChanged();
			}
		}

		this.notifyObservers(note);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (panel.getState() == GameState.Active) {
			//scroll through attack modes
			PlayerModeNotification note = null;
			if (e.getWheelRotation() < 0)
			{
				note = new PlayerModeNotification(false);
				setChanged();
			}
			else if (e.getWheelRotation() > 0)
			{
				note = new PlayerModeNotification(true);
				setChanged();
			}
			this.notifyObservers(note);
		}

	}
	
	public void mouseReleased(MouseEvent e){
		if (panel.getState() == GameState.Active) {
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				//mouse clicked to fire a bullet
				this.setChanged();
				this.notifyObservers(new PlayerAttackNotification());
			}
		}
	}
}

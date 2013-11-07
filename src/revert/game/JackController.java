package revert.game;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import com.kgp.core.GameController;
import com.kgp.game.JumperSprite;

/**
 * Main controller class for handling the game input that alters the player in the world
 * @author Nicholas Hydock
 *
 */
public class JackController extends GameController {

	JumperSprite player;
	Crosshair cross;
	
	JackPanel panel;

	/**
	 * Creates a game controller that interacts directly with the player
	 * 
	 * @param p
	 * @param c
	 */
	public JackController(JumperSprite p, Crosshair c, JackPanel panel) {
		this.player = p;
		this.cross = c;
		this.panel = panel;
	}

	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		Point2D p = new Point(x, y);
		
		cross.setAngle(p, panel.getMatrix());
	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		// game-play keys
		if (panel.getState() == GameState.Active) {
			// move the sprite and ribbons based on the arrow key pressed
			if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A )
				this.player.moveLeft();
			else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D )
				this.player.moveRight();
			else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W )
				this.player.jump(); // jumping has no effect on the
									// bricks/ribbons
			else if ((keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S ) && (!this.player.isJumping()))
				this.player.stayStill();
		}
	}

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		
		/*
		 * Set attack modes
		 */
		if (keyCode == KeyEvent.VK_SHIFT) {
			this.player.nextMode();
		} 
		else if (keyCode == KeyEvent.VK_1) {
			this.player.setMode(1);
		} 
		else if (keyCode == KeyEvent.VK_2) {
			this.player.setMode(2);
		} 
		else if (keyCode == KeyEvent.VK_3) {
			this.player.setMode(3);
		}

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0)
			this.player.prevMode();
		else if (e.getWheelRotation() > 0)
			this.player.nextMode();
			
	}
}

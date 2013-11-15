package com.kgp.core;

// JumpingJack.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* A side-scroller showing how to implement background
 movement, bricks, and a jumping sprite (called 'jack)
 who can run, jump, and collide with bricks.

 Fireball shoot out from the rhs of the panel, and 
 will explode if they hit jack.

 The background is composed of multiple ribbons
 (wraparound images) which move at different rates 
 depending on how 'far back' they are in
 the scene. This effect is called parallax scrolling.

 -----
 Pausing/Resuming/Quiting are controlled via the frame's window
 listener methods.

 Active rendering is used to update the JPanel. See WormP for
 another example, with additional statistics generation.

 Using Java 3D's timer: J3DTimer.getValue()
 *  nanosecs rather than millisecs for the period

 The MidisLoader, ClipsLoader, ImagesLoader, and ImagesPlayer
 classes are used for music, images, and animation.

 The jumping and fireball sprites are subclasses of the 
 Sprite class discussed in chapter 6.
 */

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import com.kgp.audio.MidisLoader;

public class GameFrame extends JFrame implements WindowListener, ComponentListener {
	private static final long serialVersionUID = 8839701706801702692L;

	public static int DEFAULT_FPS = 30; // 40 is too fast!

	private GamePanel game; // where the game is drawn
	private MidisLoader midisLoader;

	public long period;
	public int periodInMsec;

	public GameFrame(String title, long period) {
		super("JumpingJack");

		this.period = period;
		this.periodInMsec = (int) (period/1000000L);
	}

	public void setGame(GamePanel panel) {
		// load the background MIDI sequence
		midisLoader = new MidisLoader();
		//midisLoader.load("jjf", "jumping_jack_flash.mid");
		//midisLoader.play("jjf", true); // repeatedly play it

		Container c = getContentPane(); // default BorderLayout used
		game = panel;
		c.add(game, "Center");

		addWindowListener(this);
		addComponentListener(this);
		pack();
		//setResizable(false);

		Dimension res = Toolkit.getDefaultToolkit().getScreenSize();

		this.setLocation(res.width/2 - this.getWidth() / 2,
				res.height/2 - this.getHeight() / 2);

		setVisible(true);
	}

	// ----------------- window listener methods -------------

	public void windowActivated(WindowEvent e) {
		game.resumeGame();
	}

	public void windowDeactivated(WindowEvent e) {
		game.pauseGame();
	}

	public void windowDeiconified(WindowEvent e) {
		game.resumeGame();
	}

	public void windowIconified(WindowEvent e) {
		game.pauseGame();
	}

	public void windowClosing(WindowEvent e) {
		game.stopGame();
		midisLoader.close(); // not really required
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		game.setSize(this.getSize());
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

} // end of JumpingJack class


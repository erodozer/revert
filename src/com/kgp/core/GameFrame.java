package com.kgp.core;

import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import com.kgp.audio.MidisLoader;

/**
 * Generic game frame that provides a container for a game.
 * 
 * Based on an abstracted version of Andrew Davison's JumpingJack game
 * 
 * @author nhydock
 * @author Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
 * 
 */
public class GameFrame extends JFrame implements WindowListener {
	private static final long serialVersionUID = 8839701706801702692L;

	public static int DEFAULT_FPS = 60; // 40 is too fast!

	private GamePanel game; // where the game is drawn
	private MidisLoader midisLoader;

	public GameFrame(String title)
	{
		this(title, DEFAULT_FPS);
	}
	
	public GameFrame(String title, int fps) {
		super(title);

		Game.setPeriod((long) (1000.0 / fps)*1000000L);
	}

	public void setGame(GamePanel panel) {
		// load the background MIDI sequence
		midisLoader = new MidisLoader();

		Container c = getContentPane(); // default BorderLayout used
		game = panel;
		c.add(game);

		addWindowListener(this);
		setResizable(false);
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

} // end of JumpingJack class


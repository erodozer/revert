package com.kgp.core;

// JackPanel.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* The game's drawing surface. Uses active rendering to a JPanel
 with the help of Java 3D's timer.

 Set up the background and sprites, and update and draw
 them every period nanosecs.

 The background is a series of ribbons (wraparound images
 that move), and a bricks ribbon which the JumpingSprite
 (called 'jack') runs and jumps along.

 'Jack' doesn't actually move horizontally, but the movement
 of the background gives the illusion that it is.

 There is a fireball sprite which tries to hit jack. It shoots
 out horizontally from the right hand edge of the panel. After
 MAX_HITS hits, the game is over. Each hit is accompanied 
 by an animated explosion and sound effect.

 The game begins with a simple introductory screen, which
 doubles as a help window during the course of play. When
 the help is shown, the game pauses.

 The game is controlled only from the keyboard, no mouse
 events are caught.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import com.kgp.util.Vector2;

public abstract class GamePanel extends JPanel implements Runnable {
	private static final long serialVersionUID = -3619653557275641227L;

	private static final int NO_DELAYS_PER_YIELD = 16;
	/*
	 * Number of frames with a delay of 0 ms before the animation thread yields
	 * to other running threads.
	 */
	private static final int MAX_FRAME_SKIPS = 5;
	// no. of frames that can be skipped in any one animation loop
	// i.e the games state is updated but not rendered

	private Thread animator; // the thread that performs the animation
	private boolean running;

	protected volatile GameState prevState = GameState.ProcessPaused;
	protected volatile GameState state = GameState.ProcessPaused;

	// used at game termination
	protected volatile boolean gameOver = false;

	// for displaying messages
	protected Font msgsFont;
	protected FontMetrics metrics;

	// off-screen rendering
	private Graphics dbg;
	private Image dbImage = null;

	private long gameStartTime;

	protected GameFrame parent;

	// provided camera for doing cool effects and following of the character
	protected volatile Vector2 camera;
	protected volatile AffineTransform camMatrix;

	/**
	 * Create game content to be inserted into a frame
	 * @param parent
	 */
	public GamePanel(GameFrame parent) {
		this.parent = parent;

		setDoubleBuffered(false);
		setBackground(Color.white);

		setFocusable(true);
		requestFocus(); // the JPanel now has focus, so receives key events

		state = null;

		// set up message font
		msgsFont = new Font("SansSerif", Font.BOLD, 24);
		metrics = this.getFontMetrics(msgsFont);

		camera = new Vector2();
		camMatrix = new AffineTransform();
	}

	public void setState(GameState s) {
		prevState = state;
		state = s;
	}

	public GameState getState() {
		return state;
	}

	/**
	 * Initialize all game resources
	 */
	abstract protected void initGame();

	/**
	 * Wait for the JPanel to be added to the JFrame before starting
	 */
	final public void addNotify() {
		super.addNotify(); // creates the peer
		startGame(); // start the thread
	}

	/**
	 * Initialize and start the thread
	 */
	final protected void startGame() {
		if (animator == null || state == null) {
			initGame();
			animator = new Thread(this);
			animator.start();
		}
	}

	/**
	 * Resumes the game from being paused
	 * <p/>
	 * Called when the JFrame is activated / deiconified
	 */
	public void resumeGame()
	{
		this.setState(this.prevState);
	}

	/**
	 * Pauses the game from running
	 * <p/>
	 * Called when the JFrame is deactivated / iconified
	 */
	public void pauseGame()
	{
		this.setState(GameState.Paused);
	}

	/**
	 * End the game when the JFrame is closing
	 */
	public void stopGame()
	{
		running = false;
	}

	// ----------------------------------------------

	/**
	 * Game thread runner
	 */
	final public void run()
	{
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;

		gameStartTime = System.nanoTime();
		beforeTime = gameStartTime;

		running = true;

		while (running) {
			gameUpdate();
			gameRender();
			paintScreen();

			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepTime = (Game.period - timeDiff) - overSleepTime;

			if (sleepTime > 0) { // some time left in this cycle
				try {
					Thread.sleep(sleepTime / 1000000L); // nano -> ms
				} catch (InterruptedException ex) {
				}
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			} else { // sleepTime <= 0; the frame took longer than the period
				excess -= sleepTime; // store excess time value
				overSleepTime = 0L;

				if (++noDelays >= NO_DELAYS_PER_YIELD) {
					Thread.yield(); // give another thread a chance to run
					noDelays = 0;
				}
			}

			beforeTime = System.nanoTime();

			/*
			 * If frame animation is taking too long, update the game state
			 * without rendering it, to get the updates/sec nearer to the
			 * required FPS.
			 */
			int skips = 0;
			while ((excess > Game.period) && (skips < MAX_FRAME_SKIPS)) {
				excess -= Game.period;
				gameUpdate(); // update state but don't render
				skips++;
			}
		}
		System.exit(0); // so window disappears
	}

	/**
	 * Updates this game's specific logic
	 */
	abstract protected void gameUpdate();

	/**
	 * Draws the game graphics specific to this panel
	 * 
	 * @param g
	 */
	abstract protected void draw(Graphics2D g);

	/**
	 * Performs the rendering of the game to a buffer.
	 * <p/>
	 * Ensures that the game has a buffer to render to
	 */
	final protected void gameRender() {
		if (dbImage == null) {
			dbImage = createImage(this.getPreferredSize().width,
					this.getPreferredSize().height);
			if (dbImage == null) {
				System.out.println("dbImage is null");
				return;
			} else
				dbg = dbImage.getGraphics();
		}

		draw((Graphics2D) dbg);
	}

	/**
	 * Draw's this panel's graphics buffer to the display
	 */
	final protected void paintScreen() {
		Graphics2D g;
		try {
			g = (Graphics2D) this.getGraphics();
			if ((g != null) && (dbImage != null)) {
				g.drawImage(dbImage, 0, 0, this.getWidth(), this.getHeight(), 0, 0, this.getPreferredSize().width, this.getPreferredSize().height, this);
			}
			// Sync the display on some systems.
			// (on Linux, this fixes event queue problems)
			Toolkit.getDefaultToolkit().sync();
			g.dispose();
		} catch (Exception e) {
			System.out.println("Graphics context error: " + e);
		}
	}

	/**
	 * @return the camera matrix of the panel
	 */
	public AffineTransform getMatrix() {
		return camMatrix;
	}

}

package revert.MainScene;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import revert.Entities.Player;

import com.kgp.core.AssetsManager;
import com.kgp.core.GameController;
import com.kgp.core.GameFrame;
import com.kgp.core.GamePanel;
import com.kgp.core.GameState;
import com.kgp.imaging.ImagesLoader;
import com.kgp.imaging.ImagesPlayerWatcher;
import com.kgp.level.BricksManager;
import com.kgp.level.RibbonsManager;

/**
 * JackPanel.java Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
 * 
 * The game's drawing surface. Uses active rendering to a JPanel with the help
 * of Java 3D's timer.
 * 
 * Set up the background and sprites, and update and draw them every period
 * nanosecs.
 * 
 * The background is a series of ribbons (wraparound images that move), and a
 * bricks ribbon which the JumpingSprite (called 'jack') runs and jumps along.
 * 
 * 'Jack' doesn't actually move horizontally, but the movement of the background
 * gives the illusion that it is.
 * 
 * There is a fireball sprite which tries to hit jack. It shoots out
 * horizontally from the right hand edge of the panel. After MAX_HITS hits, the
 * game is over. Each hit is accompanied by an animated explosion and sound
 * effect.
 * 
 * The game begins with a simple introductory screen, which doubles as a help
 * window during the course of play. When the help is shown, the game pauses.
 * 
 * The game is controlled only from the keyboard, no mouse events are caught.
 **/

public class Scene extends GamePanel implements Runnable, ImagesPlayerWatcher {
	private static final long serialVersionUID = -588578363027322258L;

	private static final int PWIDTH = 1280; // size of panel
	private static final int PHEIGHT = 720;

	// image, bricks map, clips loader information files
	private static final String BRICKS_INFO = "bricksInfo.txt";

	private static final int MAX_HITS = 50;
	// number of times jack can be hit by a fireball before the game is over

	private Player jack; // the sprites
	private Crosshair crosshair;

	private long gameStartTime; // when the game started
	private int timeSpentInGame;

	// used at game termination
	private volatile boolean gameOver = false;
	private int score = 0;

	// to display the title/help screen
	private BufferedImage helpIm;

	private int numHits = 0; // the number of times 'jack' has been hit

	private float zoom = 1.0f;

	private RibbonsManager parallaxBg;
	private RibbonsManager parallaxFg;

	World world;

	public Scene(GameFrame parent) {
		super(parent);

		// set up message font
		msgsFont = new Font("SansSerif", Font.BOLD, 24);
		metrics = this.getFontMetrics(msgsFont);

		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
	}

	protected void processKey(KeyEvent e)
	// handles termination, help, and game-play keys
	{
		int keyCode = e.getKeyCode();

		// termination keys
		// listen for esc, q, end, ctrl-c on the canvas to
		// allow a convenient exit from the full screen configuration
		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q)
				|| (keyCode == KeyEvent.VK_END)
				|| ((keyCode == KeyEvent.VK_C) && e.isControlDown()))
			this.stopGame();

		// help controls
		if (keyCode == KeyEvent.VK_H) {
			if (this.getState() == GameState.Help) { // help being shown
				this.setState(GameState.Active);
			} else { // help not being shown
				this.setState(GameState.Help);
			}
		}

		if (keyCode == KeyEvent.VK_PLUS) {
			zoom = (float) Math.min(3.0, zoom + .1);
		}
		if (keyCode == KeyEvent.VK_MINUS) {
			zoom = (float) Math.max(.1, zoom - .1);
		}
		if (keyCode == KeyEvent.VK_0) {
			zoom = 1.0f;
		}
	}

	protected void initGame() {
		AssetsManager.init();

		ImagesLoader images = AssetsManager.Images;

		BricksManager bricksMan = new BricksManager(PWIDTH, PHEIGHT,
				BRICKS_INFO, images);
		int brickMoveSize = bricksMan.getMoveSize();
		this.world = new World();
		this.world.setLevel(bricksMan);

		parallaxBg = new RibbonsManager(PWIDTH, PHEIGHT, brickMoveSize, images);
		parallaxFg = new RibbonsManager(PWIDTH, PHEIGHT, brickMoveSize, images);

		parallaxBg.add("skyline", 0f);
		parallaxBg.add("forest3", .35f);
		parallaxFg.add("grass", 1.1f);

		jack = new Player(this.world, images);
		this.world.setPlayer(jack);

		crosshair = new Crosshair(PWIDTH, PHEIGHT, jack, images);

		GameController g = new Controller(jack, crosshair, this, world);
		this.addKeyListener(g);
		this.addMouseListener(g);
		this.addMouseMotionListener(g);

		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				processKey(e);
			}
		});

		// prepare title/help screen
		helpIm = images.getImage("title");

		this.setState(GameState.Help);

	}

	// ------------- game life cycle methods ------------
	// called by the JFrame's window listener methods

	// ----------------------------------------------

	protected void gameUpdate() {
		if (this.getState() == GameState.Active) {
			// stop jack and scenery on collision
			world.update();
			jack.updateSprite();
			crosshair.updateSprite();
			if (!jack.isStill()) {
				parallaxBg.update(jack.getMovement());
				parallaxFg.update(jack.getMovement());
			}
			// transform a camera that follows the player around
			camera.cpy(jack.getPosn());
			camMatrix.setToTranslation(-camera.x, -camera.y);
			camMatrix.translate(0, PHEIGHT / 2);
			camMatrix.translate(PWIDTH / 2, 0);
			camMatrix.scale(zoom, zoom);
		}
	}

	protected void draw(Graphics2D dbg) {

		

		// draw a white background
		dbg.setColor(Color.white);
		dbg.fillRect(0, 0, PWIDTH, PHEIGHT);

		AffineTransform orig = dbg.getTransform();

		// draw the game elements: order is important
		parallaxBg.display(dbg); // the background ribbons

		dbg.setTransform(camMatrix);
		world.display(dbg);
		dbg.setTransform(orig);

		parallaxFg.display(dbg); // the background ribbons

		dbg.setTransform(camMatrix);
		crosshair.drawSprite(dbg);
		dbg.setTransform(orig);

		reportStats(dbg);

		if (this.getState() == GameState.GameOver)
			gameOverMessage(dbg);

		// draw the help at the very front (if switched on)
		if (this.getState() == GameState.Help)
			dbg.drawImage(helpIm, (PWIDTH - helpIm.getWidth()) / 2,
					(PHEIGHT - helpIm.getHeight()) / 2, null);
	}

	/**
	 * Report the number of hits, and time spent playing
	 * 
	 * @param g
	 */
	private void reportStats(Graphics g) {
		if (!gameOver) // stop incrementing the timer once the game is over
			timeSpentInGame = (int) ((System.nanoTime() - gameStartTime) / 1000000000L); // ns
																							// -->
																							// secs
		g.setColor(Color.red);
		g.setFont(msgsFont);
		g.drawString("Hits: " + numHits + "/" + MAX_HITS, 15, 25);
		g.drawString("Time: " + timeSpentInGame + " secs", 15, 50);
		g.setColor(Color.black);
	}

	/**
	 * Center the game-over message in the panel.
	 * 
	 * @param g
	 */
	private void gameOverMessage(Graphics g) {
		String msg = "Game Over. Your score: " + score;

		int x = (PWIDTH - metrics.stringWidth(msg)) / 2;
		int y = (PHEIGHT - metrics.getHeight()) / 2;
		g.setColor(Color.white);
		g.setFont(msgsFont);
		g.drawString(msg, x, y);
	}

	@Override
	public void sequenceEnded(String imageName) {
		// TODO Auto-generated method stub

	}

} // end of JackPanel class

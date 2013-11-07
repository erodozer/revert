package revert.game;


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

import com.kgp.audio.ClipsLoader;
import com.kgp.core.GameController;
import com.kgp.core.GameFrame;
import com.kgp.core.GamePanel;
import com.kgp.game.FireBallSprite;
import com.kgp.game.Projectile;
import com.kgp.game.JumperSprite;
import com.kgp.imaging.ImagesLoader;
import com.kgp.imaging.ImagesPlayer;
import com.kgp.imaging.ImagesPlayerWatcher;
import com.kgp.imaging.Sprite;
import com.kgp.level.BricksManager;
import com.kgp.level.RibbonsManager;

import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;

public class JackPanel extends GamePanel implements Runnable, ImagesPlayerWatcher {
	private static final long serialVersionUID = -588578363027322258L;
	
	private static final int PWIDTH = 1440; // size of panel
	private static final int PHEIGHT = 720;

	// image, bricks map, clips loader information files
	private static final String IMS_INFO = "imsInfo.txt";
	private static final String BRICKS_INFO = "bricksInfo.txt";
	private static final String SNDS_FILE = "clipsInfo.txt";

	// names of the explosion clips
	private static final String[] exploNames = { "explo1" };

	private static final int MAX_HITS = 50;
	// number of times jack can be hit by a fireball before the game is over

	private JumperSprite jack; // the sprites
	private Crosshair crosshair;
	
	private ArrayList<Projectile> projectiles;

	private BricksManager bricksMan; // the bricks manager

	private long gameStartTime; // when the game started
	private int timeSpentInGame;

	// used at game termination
	private volatile boolean gameOver = false;
	private int score = 0;

	// to display the title/help screen
	private boolean showHelp;
	private BufferedImage helpIm;

	// explosion-related
	private ImagesPlayer explosionPlayer = null;
	private boolean showExplosion = false;
	private int explWidth, explHeight; // image dimensions
	private int xExpl, yExpl; // coords where image is drawn

	private int numHits = 0; // the number of times 'jack' has been hit

	private boolean projLock = false;
	private float zoom = 1.0f;

	private RibbonsManager parallaxBg;
	private RibbonsManager parallaxFg;
	
	public JackPanel(GameFrame parent, long period) {
		super(parent, period);

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
			} 
			else { // help not being shown
				this.setState(GameState.Help);
			}
		}
		
		if (keyCode == KeyEvent.VK_PLUS){
			zoom = (float)Math.min(3.0, zoom + .1);
		}
		if (keyCode == KeyEvent.VK_MINUS){
			zoom = (float)Math.max(.1, zoom - .1);
		}
		if (keyCode == KeyEvent.VK_0){
			zoom = 1.0f;
		}
	}

	/**
	 * called by fireball sprite when it hits jack at (x,y)
	 * 
	 * @param x
	 * @param y
	 */
	public void showExplosion(int x, int y) {
		if (!showExplosion) { // only allow a single explosion at a time
			showExplosion = true;
			xExpl = x - explWidth / 2; // \ (x,y) is the center of the explosion
			yExpl = y - explHeight / 2;

			/*
			 * Play an explosion clip, but cycle through them. This adds
			 * variety, and gets round not being able to play multiple instances
			 * of a clip at the same time.
			 */
			// clipsLoader.play(exploNames[numHits % exploNames.length], false);
			numHits++;
		}
	} // end of showExplosion()

	public void sequenceEnded(String imageName)
	// called by ImagesPlayer when the explosion animation finishes
	{
		showExplosion = false;
		explosionPlayer.restartAt(0); // reset animation for next time

		if (numHits >= MAX_HITS) {
			gameOver = true;
			score = (int) ((System.nanoTime() - gameStartTime) / 1000000000L);
			sfx.play("applause", false);
		}
	} // end of sequenceEnded()

	protected void initGame() {
		images = new ImagesLoader(IMS_INFO);
		sfx = new ClipsLoader(SNDS_FILE);

		bricksMan = new BricksManager(PWIDTH, PHEIGHT, BRICKS_INFO, images);
		int brickMoveSize = bricksMan.getMoveSize();

		parallaxBg = new RibbonsManager(PWIDTH, PHEIGHT, brickMoveSize, images);
		parallaxFg = new RibbonsManager(PWIDTH, PHEIGHT, brickMoveSize, images);

		parallaxBg.add("skyline", 0f);
		parallaxBg.add("forest3", .35f);
		parallaxFg.add("grass", 1.1f);
		
		jack = new JumperSprite(PWIDTH, PHEIGHT, brickMoveSize, bricksMan, images, (int)(period/1000000L));
		crosshair = new Crosshair(PWIDTH, PHEIGHT, jack, images);
		
		GameController g = new JackController(jack, crosshair, this);
		this.addKeyListener(g);
		this.addMouseListener(g);
		this.addMouseMotionListener(g);
		
		this.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e)
			{
				processKey(e);
			}
		});
		
		projectiles = new ArrayList<Projectile>();
		this.add(new FireBallSprite(PWIDTH, PHEIGHT, images, this, jack));
		
		// prepare the explosion animation
		explosionPlayer = new ImagesPlayer("explosion", (int)period, 0.5, false, images);
		BufferedImage explosionIm = images.getImage("explosion");
		explWidth = explosionIm.getWidth();
		explHeight = explosionIm.getHeight();
		explosionPlayer.setWatcher(this);

		// prepare title/help screen
		helpIm = images.getImage("title");

		this.setState(GameState.Help);
		
	}

	// ------------- game life cycle methods ------------
	// called by the JFrame's window listener methods

	// ----------------------------------------------

	public void add(Projectile p) {
		while (projLock);

		projLock = true;
		projectiles.add(p);
		projLock = false;
	}

	protected void gameUpdate() {
		if (this.getState() == GameState.Active) {
			 // stop jack and scenery on collision
			bricksMan.update(jack.getWorldPosn());
			jack.updateSprite();
			crosshair.updateSprite();
			if (!jack.isStill()) {
				parallaxBg.update(jack.getDirection());
				parallaxFg.update(jack.getDirection());
			}
			
			while (projLock);

			projLock = true;

			for (int i = 0; i < projectiles.size();) {
				Projectile p = projectiles.get(i);
				p.updateSprite();
				if (p.isActive()) {
					i++;
				} else {
					projectiles.remove(i);
				}
			}

			projLock = false;

			if (jack.getYWorldPosn() > bricksMan.getHeight()) {
				gameOver = true;
			}

			if (showExplosion)
				explosionPlayer.updateTick(); // update the animation
		}
	}

	protected void draw(Graphics2D dbg) {

		//transform a camera that follows the player around
		camera.move(jack.getXWorldPosn(), jack.getYWorldPosn());
		//if (camera.y - PHEIGHT / 2 > 0 && camera.y + PHEIGHT/2 < bricksMan.height())
		//{
			camMatrix.setToTranslation(-camera.x, -camera.y);
			camMatrix.translate(0, PHEIGHT/2);
		//}
		//else
		//{
		//camMatrix.setToTranslation(-camera.x, 0);
		//}
		camMatrix.translate(PWIDTH/2, 0);
		camMatrix.scale(zoom, zoom);
		
		// draw a white background
		dbg.setColor(Color.white);
		dbg.fillRect(0, 0, PWIDTH, PHEIGHT);

		AffineTransform orig = dbg.getTransform();
		
		// draw the game elements: order is important
		parallaxBg.display(dbg); // the background ribbons
		
		dbg.setTransform(camMatrix);
		bricksMan.display(dbg); // the bricks
		jack.drawSprite(dbg); // the sprites
		dbg.setTransform(orig);
		
		parallaxFg.display(dbg); // the background ribbons
		
		while (projLock);
		projLock = true;

		for (int i = 0; i < projectiles.size(); i++) {
			projectiles.get(i).drawSprite(dbg);
		}

		projLock = false;

		if (showExplosion) // draw the explosion (in front of jack)
			dbg.drawImage(explosionPlayer.getCurrentImage(), xExpl, yExpl, null);

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
	 * @param g
	 */
	private void reportStats(Graphics g)
	{
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
	 * @param g
	 */
	private void gameOverMessage(Graphics g)
	{
		String msg = "Game Over. Your score: " + score;

		int x = (PWIDTH - metrics.stringWidth(msg)) / 2;
		int y = (PHEIGHT - metrics.getHeight()) / 2;
		g.setColor(Color.white);
		g.setFont(msgsFont);
		g.drawString(msg, x, y);
	}

	public AffineTransform getMatrix() {
		// TODO Auto-generated method stub
		return camMatrix;
	}
} // end of JackPanel class

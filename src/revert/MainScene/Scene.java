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
 * Based heavily on Andrew Davison's JackPanel, this scene is the core manager
 * for the game.  It extends the abstracted GamePanel, and can be switched out with
 * other game panels for different experiences.
 * 
 * In this game, Revert, you play as Royer, as detective in the early 1800s, who
 * after having worked on a case for 8 years, has started to see things changing 
 * with the world.  Upon talking with others, it appears he may be the only one who 
 * has a memory of how things used to be.
 * 
 * Creatures of the shadows start to rise up and terrorize him wherever he goes
 * as the world begins to devolve into a state of madness.  Some chase him and attack him
 * while others just bother him with their existance.  The shadow-beings take
 * a toll on his willpower, so he does what he can to eradicate them.
 * 
 * As he cleanses the world of these creatures, everything starts to revert back
 * to normal, and people's memories return.
 * 
 * In his struggles he has come to realize that the creatures are weak to 3 different
 * kinds of attacks, all corresponding to their personality.  He has crafted
 * a full arsenal of ammo for him to use when necessary.  Use this knowledge and power
 * and restore the world to its rightful state once more.
 * 
 * @author nhydock
 **/

public class Scene extends GamePanel {
	private static final long serialVersionUID = -588578363027322258L;

	private static final int PWIDTH = 1280; // size of panel
	private static final int PHEIGHT = 720;

	// image, bricks map, clips loader information files
	private static final String BRICKS_INFO = "bricksInfo.txt";

	private Player player; // the sprites
	private Crosshair crosshair;

	// to display the title/help screen
	private BufferedImage helpIm;

	private float zoom = 1.0f;

	private RibbonsManager parallaxBg;
	private RibbonsManager parallaxFg;

	World world;

	//display of in-game stats
	HUD hud;
	
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

		player = new Player(this.world, images);
		this.world.setPlayer(player);

		crosshair = new Crosshair(PWIDTH, PHEIGHT, player, images);
		
		GameController g = new Controller(player, crosshair, this, world);
		this.addKeyListener(g);
		this.addMouseListener(g);
		this.addMouseMotionListener(g);
		
		g.addObserver(player);

		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				processKey(e);
			}
		});

		hud = new HUD(new Dimension(PWIDTH, PHEIGHT));
		world.addObserver(hud);
		player.addObserver(hud);
		
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
			player.updateSprite();
			crosshair.updateSprite();
			if (!player.isStill()) {
				parallaxBg.update(player.getMovement());
				parallaxFg.update(player.getMovement());
			}
			// transform a camera that follows the player around
			camera.cpy(player.getPosn());
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

		hud.display(dbg);

		// draw the help at the very front (if switched on)
		if (this.getState() == GameState.Help)
			dbg.drawImage(helpIm, (PWIDTH - helpIm.getWidth()) / 2,
					(PHEIGHT - helpIm.getHeight()) / 2, null);
	}

} // end of JackPanel class

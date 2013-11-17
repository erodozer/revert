package com.kgp.imaging;

// ImagesPlayer.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* ImagesPLayer is aimed at displaying the sequence of images
 making up a 'n', 's', or 'g' image file, as loaded by
 ImagesLoader.

 The ImagesPlayer constructor is supplied with the
 intended duration for showing the entire sequence
 (seqDuration). This is used to calculate showPeriod,
 the amount of time each image should be shown before
 the next image is displayed.

 The animation period (animPeriod) input argument states
 how often the ImagesPlayer's updateTick() method will be
 called. The intention is that updateTick() will be called periodically
 from the update() method in the top-level animation framework.

 The current animation time is calculated when updateTick()
 is called, and used to calculate imPosition, imPosition
 specifies which image should be returned when getCurrentImage() 
 is called.

 The ImagesPlayer can be set to cycle, stop, resume, or restart
 at a given image position.

 When the sequence finishes, a callback, sequenceEnded(), can
 be invoked in a specified object implementing the 
 ImagesPlayerWatcher interface.

 */

import java.awt.image.BufferedImage;
import java.util.Observable;

public class ImagesPlayer extends Observable {
	private String imName;
	private boolean loop, done;
	private ImagesLoader imsLoader;

	// period used by animation loop (in ms)
	private float period;
	private float periodPerFrame;
	private float elapsedTime;
	private float seqDuration;

	private int numImages;
	private int imPosition; // position of current displayable image

	/**
	 * Create an animation player
	 * 
	 * @param nm
	 *            - name of the image group
	 * @param period
	 *            - avg rate of change of the game (in sec)
	 * @param duration
	 *            - length of time it should take to play the entire animation
	 *            (in sec)
	 * @param loop
	 *            - tell if the image should loop or play once
	 * @param il
	 *            - asset managed loader of images
	 */
	public ImagesPlayer(String nm, float period, float duration, boolean loop,
			ImagesLoader il) {
		imName = nm;
		seqDuration = duration;
		this.loop = loop;
		this.period = period;
		imsLoader = il;

		if (seqDuration < 0.5f) {
			System.out.println("Warning: minimum sequence duration is 0.5 sec.");
			seqDuration = 0.5f;
		}

		if (!imsLoader.isLoaded(imName)) {
			System.out.println(imName + " is not known by the ImagesLoader");
			numImages = 0;
			imPosition = -1;
			done = true;
		} else {
			numImages = imsLoader.numImages(imName);
			imPosition = 0;
			done = false;
			periodPerFrame = seqDuration / (float) numImages;
			elapsedTime = 0;
		}
	}

	/**
	 * Update's the image player
	 * <p/>
	 * We assume this is called with every update cycle of the game
	 */
	public void updateTick() {
		if (!done) {
			// update the timer
			elapsedTime += period;
			System.out.println(elapsedTime);
			if (elapsedTime > periodPerFrame) {
				imPosition += (int) (elapsedTime / periodPerFrame);
				elapsedTime %= periodPerFrame;

				if (imPosition >= numImages) {
					// loop back
					if (loop) {
						imPosition %= numImages;
					}
					// stop image if past end and not looping
					else {
						// force the imPosition to be of the last image
						imPosition = numImages - 1;
						done = true;
						// notify any watchers that it's done
						this.setChanged();
						this.notifyObservers(new SequenceEndNotification());
					}
				}
			}
		}
	}

	/**
	 * Gets the image of the currently visible frame
	 * @return BufferedImage
	 */
	public BufferedImage getCurrentImage() {
		if (numImages != 0)
			return imsLoader.getImage(imName, imPosition);
		else
			return null;
	}

	/**
	 * Gets the image set index of the currently visible frame
	 * @return int
	 */
	public int getCurrentPosition() {
		return imPosition;
	}

	/**
	 * Forcably end the animation so it no longer updates
	 */
	public void stop() {
		done = true;
		;
	}

	/**
	 * Get if the animation has been stopped
	 * @return boolean
	 */
	public boolean isStopped() {
		return done;
	}

	/**
	 * Get if the animation is at its end
	 * 
	 * @return boolean
	 */
	public boolean atSequenceEnd() {
		return ((imPosition == numImages - 1) && done);
	}

	/**
	 * Restart the animation to a specified frame
	 * 
	 * @param imPosn
	 */
	public void restartAt(int imPosn) {
		if (numImages != 0) {
			if ((imPosn < 0) || (imPosn > numImages - 1)) {
				System.err.println("Out of range restart, starting at 0");
				imPosn = 0;
			}

			imPosition = imPosn;
			elapsedTime = 0;
			done = false;
		}
	}

	/**
	 * Continue the animation from where it stopped
	 */
	public void resume() {
		if (numImages != 0) {
			if (imPosition < numImages) {
				done = false;
			}
		}
	}

	/**
	 * Notification to send out to observers that the animation has ended
	 * 
	 * @author nhydock
	 * 
	 */
	public static class SequenceEndNotification {
	}

}

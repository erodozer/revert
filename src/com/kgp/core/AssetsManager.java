package com.kgp.core;

import com.kgp.audio.ClipsLoader;
import com.kgp.imaging.ImagesLoader;

/**
 * Global manager of assets used in the game.
 * <p/>
 * That allows you to not have to worry so much about passing and maintaining
 * loaders for things, as there should only ever really need to be one
 * loader for each.
 * @author nhydock
 *
 */
public class AssetsManager {

	public static ImagesLoader Images;
	public static ClipsLoader Sounds;

	public static void init() {
		Images = new ImagesLoader("imsInfo.txt");
		Sounds = new ClipsLoader("clipsInfo.txt");
	}

}

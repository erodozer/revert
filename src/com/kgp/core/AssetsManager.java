package com.kgp.core;

import com.kgp.audio.ClipsLoader;
import com.kgp.imaging.ImagesLoader;

public class AssetsManager {

	public static ImagesLoader Images;
	public static ClipsLoader Sounds;
	
	public static void init()
	{
		Images = new ImagesLoader("imsInfo.txt");
		Sounds = new ClipsLoader("clipsInfo.txt");
		
	}
	
}

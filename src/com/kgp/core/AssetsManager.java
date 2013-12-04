package com.kgp.core;

import java.io.InputStream;

import revert.util.JsonBricksManager;
import revert.util.JsonBricksManager.JsonBricksDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    // Configure GSON
    public static Gson JsonParser;
    
	public static void init() {
		Images = new ImagesLoader("imsInfo.txt");
		Sounds = new ClipsLoader("clipsInfo.txt");
		
		GsonBuilder gsonBuilder = new GsonBuilder();
	   	gsonBuilder.registerTypeAdapter(JsonBricksManager.class, new JsonBricksDeserializer());
	    JsonParser = gsonBuilder.create();
	}
	
	public static InputStream getResource(String file)
	{
		return AssetsManager.class.getClassLoader().getResourceAsStream(file);
	}

}

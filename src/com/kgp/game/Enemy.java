package com.kgp.game;

import java.util.ArrayList;

import com.kgp.imaging.ImagesLoader;

public class Enemy extends Actor {

	public Enemy(int x, int y, int w, int h, ImagesLoader imsLd, String name, ArrayList<Actor> actors) {
		super(x, y, w, h, imsLd, name, actors);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getNextImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void attack() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void reactOnInView(Actor a) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void reactOnOutOfView(Actor a) {
		// TODO Auto-generated method stub

	}

}

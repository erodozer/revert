package com.kgp.core;

/**
 * General game states used
 * @author nhydock
 *
 */
public enum GameState {

	/**
	 * Entire game process is paused
	 */
	ProcessPaused,
	/**
	 * Game logic is paused
	 */
	Paused,
	/**
	 * Game is running
	 */
	Active,
	/**
	 * Player has lost
	 */
	GameOver,
	/**
	 * Show the help screen
	 */
	Help, 
	/**
	 * Show the title screen
	 */
	Start, 
	/**
	 * Show player statistics after a level is over
	 */
	Report;
	
}

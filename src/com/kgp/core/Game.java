package com.kgp.core;

public class Game {

	/**
	 * Timer resolution in nanoseconds
	 */
	protected static long period;
	/**
	 * Timer resolution in milliseconds
	 */
	protected static int periodMs;
	
	/**
	 * Timer resolution in seconds
	 */
	protected static float deltaTime;
	
	protected static void setPeriod(long period)
	{
		Game.period = period;
		Game.periodMs = (int)(period/1000000L);
		Game.deltaTime = periodMs/1000f;
		
		System.out.println(period);
		System.out.println(periodMs);
		System.out.println(deltaTime);
	}
	
	/**
	 * Timer resolution in nanoseconds
	 */
	public static long getPeriod()
	{
		return period;
	}
	
	/**
	 * Timer resolution in milliseconds
	 */
	public static int getPeriodInMSec()
	{
		return periodMs;
	}
	
	/**
	 * Timer resolution in seconds
	 */
	public static float getDeltaTime()
	{
		return deltaTime;
	}
}

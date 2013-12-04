package com.kgp.util;

import java.awt.geom.Point2D;

/**
 * Simple class for handling Vector math of vectors with a length of 2.
 * 
 * Not the most capable vector class, but it's slim and powerful enough
 *   to be sufficent for most 2D games.
 * 
 * @author Nicholas Hydock
 */
public class Vector2 extends Point2D.Float{

	/**
	 * Basic general vectors
	 */
	public static final Vector2 ZERO = new Vector2();
	public static final Vector2 UP = new Vector2(0, -1);
	public static final Vector2 DOWN = new Vector2(0, 1);
	public static final Vector2 LEFT = new Vector2(-1, 0);
	public static final Vector2 RIGHT = new Vector2(1, 0);
	
	public Vector2(float a, float b)
	{
		this.x = a;
		this.y = b;
	}
	
	public Vector2()
	{
		this.x = 0;
		this.y = 0;
	}
	
	/**
	 * Multiply this vector with another
	 * @param v
	 */
	public void mult(Vector2 v)
	{
		x = x*v.x + x*v.y;
		y = y*v.x + y*v.y;
	}
	
	/**
	 * Multiply this vector by a scalar
	 * @param s
	 */
	public void mult(float s)
	{
		x *= s;
		y *= s;
	}
	
	/**
	 * Add another vector's values to this one
	 * @param v
	 */
	public void add(Vector2 v)
	{
		x += v.x;
		y += v.y;
	}
	
	/**
	 * Creates a copy of this vector
	 */
	public Vector2 clone()
	{
		Vector2 v = new Vector2();
		v.x = x;
		v.y = y;
		return v;
	}
	
	/**
	 * Copy another vector's values to this one
	 * @param v
	 */
	public void cpy(Vector2 v)
	{
		x = v.x;
		y = v.y;
	}
	
	/**
	 * Subtract another vector's values from this one
	 * @param v
	 */
	public void sub(Vector2 v)
	{
		x -= v.x;
		y -= v.y;
	}
	
	/**
	 * Shifts this vector's values over
	 * @param f
	 * @param g
	 */
	public void translate(float f, float g)
	{
		x += f;
		y += g;
	}
	
	/**
	 * Get the normalized version of this vector
	 * @return Vector2
	 */
	public Vector2 normalize()
	{
		Vector2 v = new Vector2();
		float l = length();
		v.x = x / l;
		v.y = y / l;
		return v;
	}
	
	/**
	 * Get the vector between two points
	 * @param v
	 * @return
	 */
	public Vector2 to(Vector2 v)
	{
		Vector2 v2 = new Vector2();
		v2.x = v.x - x;
		v2.y = v.y - y;
		
		return v2;
	}
	
	/**
	 * Get the dot product multiplication between 2 vectors
	 * @param v - vector to operate against
	 * @return float
	 */
	public float dot(Vector2 v)
	{
		return x * v.x + y * v.y;
	}
	
	/**
	 * Get the length of the vector
	 * @return float
	 */
	public float length()
	{
		return (float)Math.sqrt((x * x) + (y * y)); 
	}
	
	/**
	 * Get the angle of this vector cast from 0,0
	 * @return float
	 */
	public float angle()
	{
		return (float)Math.atan2(y, x);
	}

	/**
	 * Sets both values of this vector at once
	 * @param x
	 * @param y
	 */
	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString()
	{
		return String.format("X: %f Y: %f\n", x, y);
	}
}

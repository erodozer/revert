package com.kgp.util;

/**
 * Simple class for handling Vector math of vectors with a length of 2.
 * 
 * @author Nicholas Hydock
 */
public class Vector2 {

	public float x;
	public float y;
	
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
		x -= v.x;
		y -= v.y;
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
	 * Get the normalized vector that spans from this vector to another vector
	 * @param v
	 * @return
	 */
	public Vector2 normalize(Vector2 v)
	{
		Vector2 v2 = new Vector2();
		float l = distance(v2);
		v2.x = x / l;
		v2.y = y / l;
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
	
	public float angle()
	{
		return (float)Math.atan2(x, y);
	}
	
	/**
	 * Get the distance between two vectors
	 * @param v
	 * @return float
	 */
	public float distance(Vector2 v)
	{
		return (float)Math.sqrt((v.x-x)*(v.x-x) + (v.y-y)*(v.y-y));
	}

	public void cpy(float x, float y) {
		this.x = x;
		this.y = y;
	}
}

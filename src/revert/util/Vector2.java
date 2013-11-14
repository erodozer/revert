package revert.util;

import java.awt.Point;

/**
 * Simple class for handling Vector math of vectors with a length of 2.
 * 
 * @author Nicholas Hydock
 */
public class Vector2 {

	public float a;
	public float b;
	
	public Vector2(float a, float b)
	{
		this.a = a;
		this.b = b;
	}
	
	public Vector2()
	{
		this.a = 0;
		this.b = 0;
	}
	
	/**
	 * Multiply this vector with another
	 * @param v
	 */
	public void mult(Vector2 v)
	{
		a = a*v.a + a*v.b;
		b = b*v.a + b*v.b;
	}
	
	/**
	 * Multiply this vector by a scalar
	 * @param s
	 */
	public void mult(float s)
	{
		a *= s;
		b *= s;
	}
	
	/**
	 * Add another vector's values to this one
	 * @param v
	 */
	public void add(Vector2 v)
	{
		a -= v.a;
		b -= v.b;
	}
	
	/**
	 * Copy another vector's values to this one
	 * @param v
	 */
	public void cpy(Vector2 v)
	{
		a = v.a;
		b = v.b;
	}
	
	/**
	 * Subtract another vector's values from this one
	 * @param v
	 */
	public void sub(Vector2 v)
	{
		a -= v.a;
		b -= v.b;
	}
	
	public void translate(float f, float g)
	{
		a += f;
		b += g;
	}
	
	/**
	 * Get the normalized version of this vector
	 * @return Vector2
	 */
	public Vector2 normalize()
	{
		Vector2 v = new Vector2();
		float l = length();
		v.a = a / l;
		v.b = b / l;
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
		v2.a = a / l;
		v2.b = b / l;
		return v2;
	}
	
	/**
	 * Get the dot product multiplication between 2 vectors
	 * @param v - vector to operate against
	 * @return float
	 */
	public float dot(Vector2 v)
	{
		return a * v.a + b * v.b;
	}
	
	/**
	 * Get the length of the vector
	 * @return float
	 */
	public float length()
	{
		return (float)Math.sqrt((a * a) + (b * b)); 
	}
	
	public float angle()
	{
		return (float)Math.atan2(a, b);
	}
	
	/**
	 * Get the distance between two vectors
	 * @param v
	 * @return float
	 */
	public float distance(Vector2 v)
	{
		return (float)Math.sqrt((v.a-a)*(v.a-a) + (v.b-b)*(v.b-b));
	}
}

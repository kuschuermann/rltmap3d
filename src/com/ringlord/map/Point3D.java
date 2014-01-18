package com.ringlord.map;

import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * An reference implementation of {@link Location3D} that you may extend with
 * additional functionality or use as-is.
 * 
 * @author K Udo Schuermann
 * @see Map3D
 * @see Location3D
 */
public class Point3D
  implements
    Location3D
{
  public Point3D()
  {
    super();
    this.x = 0.0d;
    this.y = 0.0d;
    this.z = 0.0d;
  }


  /**
   * @param x
   *          The x-coordinate, deemed on the horizontal plane
   * @param y
   *          The y-coordinate, deemed on the horizontal plane
   * @param z
   *          The z-coordinate, deemed on the vertical (positive is up)
   **/
  public Location3D setCartesian( final double x,
	                          final double y,
	                          final double z )
  {
    if( (x != getX()) || (y != getY()) || (z != getZ()) )
      {
	final double oldX = this.x;
	final double oldY = this.y;
	final double oldZ = this.z;

	this.x = x;
	this.y = y;
	this.z = z;

	fireMovementEvent( new MovementEvent<Location3D>( oldX,
	                                                  oldY,
	                                                  oldZ,
	                                                  this ) );
      }
    return this;
  }


  /**
   * @param result
   *          An optional array to be filled and returned: It must be non-null
   *          and whose <code>.length</code> must be precisely 3, otherwise a
   *          new array of those dimensions will be allocated for the return
   *          value, instead.
   * @return An array containing the values {@link #getX() [0]&nbsp;X},
   *         {@link #getY() [1]&nbsp;Y}, {@link #getZ() [2] Z}.
   */
  public double[] getCartesian( double[] result )
  {
    if( (result == null) || (result.length != 3) )
      {
	result = new double[3];
      }
    result[0] = x;
    result[1] = y;
    result[2] = z;

    return result;
  }


  @Override
  public double getX()
  {
    return x;
  }


  @Override
  public double getY()
  {
    return y;
  }


  @Override
  public double getZ()
  {
    return z;
  }


  /**
   * @param phi
   *          The angle around the equator (longitude, right ascension) in
   *          radians.
   * @param theta
   *          The angle above/below the equator (latitude, declination) in
   *          radians.
   * @param rho
   *          The radius/distance from the center in arbitrary distance units.
   **/
  public Point3D setSpherical( final double phi,
	                       final double theta,
	                       final double rho )
  {
    final double rVect = rho * Math.cos( theta );
    setCartesian( rVect * Math.cos( phi ),
	          rVect * Math.sin( phi ),
	          rho * Math.sin( theta ) );
    return this;
  }


  /**
   * @param result
   *          An optional array to be filled and returned: It must be non-null
   *          and whose <code>.length</code> must be precisely 3, otherwise a
   *          new array of those dimensions will be allocated for the return
   *          value, instead.
   * @return An array containing the values {@link #getPhi() [0]&nbsp;Phi},
   *         {@link #getTheta() [1]&nbsp;Theta}, {@link #getRho() [2] Rho}.
   * @see getPhi()
   * @see getTheta()
   * @see getRho()
   */
  public double[] getSpherical( double[] result )
  {
    if( (result == null) || (result.length != 3) )
      {
	result = new double[3];
      }
    result[0] = getPhi();
    result[1] = getTheta();
    result[2] = getRho();

    return result;
  }


  /**
   * Obtains the angle around the coordinate system's equator ("longitude" in
   * the terms of Earth's surface coordinates).
   * 
   * @return The angle around the equator (longitude) in radians.
   * @see #getSpherical(double[])
   */
  public double getPhi()
  {
    return Math.atan2( getY(),
	               getX() );
  }


  /**
   * Obtains the angle above the coordinate system's equator ("latitude" in the
   * terms of Earth's surface coordinates)
   * 
   * @return The angle above/below the equator (latitude) in radians.
   * @see #getSpherical(double[])
   */
  public double getTheta()
  {
    final double x = getX();
    final double y = getY();
    final double z = getZ();
    // Test for special case, which would otherwise result in NaN
    if( (x == 0) && (y == 0) )
      {
	return 0.0d;
      }
    return Math.atan( z / Math.sqrt( (x * x) + (y * y) ) );
  }


  /**
   * Obtains the distance from the coordinate center.
   * 
   * @return The radius/distance from the center in arbitrary distance units.
   * @see #getSpherical(double[])
   */
  public double getRho()
  {
    final double x = getX();
    final double y = getY();
    final double z = getZ();
    return Math.sqrt( (x * x) + (y * y) + (z * z) );
  }


  @Override
  public final double distanceTo( final Location3D location )
  {
    return distanceTo( location.getX(),
	               location.getY(),
	               location.getZ() );
  }


  /**
   * Obtains the distance from this Location3D to another expressed in (x,y,z)
   * coordinates.
   * 
   * @param x
   *          The other location's x-coordinate (along the flat plane)
   * @param y
   *          The other location's y-coordinate (along the flat plane)
   * @param z
   *          The other location's z-coordinate (perpendicular to x and y)
   * @return The distance between this and the other location in arbitrary
   *         distance units.
   */
  @Override
  public double distanceTo( final double x,
	                    final double y,
	                    final double z )
  {
    final double dx = (getX() - x);
    final double dy = (getY() - y);
    final double dz = (getZ() - z);
    return Math.sqrt( (dx * dx) + (dy * dy) + (dz * dz) );
  }


  /**
   * Computes the distance between two arbitrary locations in 3D space.
   * 
   * @param loc1
   *          One location (must not be null)
   * @param loc2
   *          The other location (must not be null)
   * @return The distance between the two locations in 3D space.
   */
  public static final double distance( final Location3D loc1,
	                               final Location3D loc2 )
  {
    return loc1.distanceTo( loc2 );
  }


  @Override
  public String toString()
  {
    return String.format( "<%1.4f,%1.4f,%1.4f>",
	                  x,
	                  y,
	                  z );
  }


  @SuppressWarnings("unchecked")
  @Override
  public final void addMovementListener( final MovementListener<? extends Location3D> l )
  {
    if( movementListeners == null )
      {
	movementListeners = new HashSet<>();
      }
    movementListeners.add( (MovementListener<Location3D>)l );
  }


  @Override
  public final void removeMovementListener( final MovementListener<? extends Location3D> l )
  {
    if( movementListeners != null )
      {
	if( movementListeners.remove( l ) && movementListeners.isEmpty() )
	  {
	    movementListeners = null;
	  }
      }
  }


  /**
   * Fires a {@link ChangeEvent} to all {@link ChangeListener}S, provided at
   * least one is listener.
   */
  private void fireMovementEvent( final Location3D.MovementEvent<Location3D> e )
  {
    if( movementListeners != null )
      {
	for( final MovementListener<Location3D> l : movementListeners )
	  {
	    l.locationMoved( e );
	  }
      }
  }
  private double x, y, z;
  private Set<MovementListener<Location3D>> movementListeners;
}

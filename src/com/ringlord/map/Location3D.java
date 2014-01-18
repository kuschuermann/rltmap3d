package com.ringlord.map;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * A coordinate in 3D space, of particular interest for {@link Map3D}. A default
 * implementation of this class is provided in {@link Point3D}.
 * 
 * @author K Udo Schuermann
 * @see Map3D
 * @see Point3D
 */
public class Location3D
{
  public Location3D()
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

	fireMovementEvent( new MovementEvent( oldX,
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


  /**
   * The distance along the X-axis (on the flat of the plane).
   * 
   * @return The distance along the X-axis in arbitrary distance units.
   */
  public double getX()
  {
    return x;
  }


  /**
   * The distance along the Y-axis (on the flat of the plane).
   * 
   * @return The distance along the Y-axis in arbitrary distance units.
   */
  public double getY()
  {
    return y;
  }


  /**
   * The distance along the Z-axis (up/down above/blow the X/Y plane).
   * 
   * @return The distance along the Z-axis in arbitrary distance units.
   */
  public double getZ()
  {
    return z;
  }


  /**
   * Obtains the distance from this Location3D to another.
   * 
   * @param location
   *          The other location
   * @return The distance between this and the other location in arbitrary
   *         distance units.
   */
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


  /**
   * Add a {@link MovementListener} that is notified whenever this location in
   * 3D space changes.
   * 
   * @param l
   *          The MovementListener to be added.
   * @see #removeMovementListener(ChangeListener)
   */
  public final void addMovementListener( final MovementListener l )
  {
    if( movementListeners == null )
      {
	movementListeners = new HashSet<MovementListener>();
      }
    movementListeners.add( l );
  }


  /**
   * Remove a {@link MovementListener} so that it is no longer notified whenever
   * this location in 3D space changes. When the last MovementListener is
   * removed, the internal structure is cleaned up to recover its storage.
   * 
   * @param l
   *          The MovementListener to be added.
   * @See {@link #addMovementListener(MovementListener)}
   */
  public final void removeMovementListener( final MovementListener l )
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
  protected void fireMovementEvent( final Location3D.MovementEvent e )
  {
    if( movementListeners != null )
      {
	for( final Location3D.MovementListener l : movementListeners )
	  {
	    l.locationMoved( e );
	  }
      }
  }


  public interface MovementListener
  {
    public void locationMoved( final MovementEvent e );
  }


  final class MovementEvent
    extends EventObject
  {
    final double x, y, z;
    final Location3D location3D;


    MovementEvent( final double x,
	           final double y,
	           final double z,
	           final Location3D location3D )
    {
      super( location3D );
      this.location3D = location3D;
      this.x = x;
      this.y = y;
      this.z = z;
    }
    private static final long serialVersionUID = -167877702239258661L;
  }

  private double x, y, z;
  private Set<MovementListener> movementListeners;
}

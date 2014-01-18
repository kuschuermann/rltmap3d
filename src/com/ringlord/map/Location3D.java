package com.ringlord.map;

import java.util.EventObject;

import javax.swing.event.ChangeListener;


/**
 * A coordinate in 3D space, of particular interest for {@link Map3D}. A default
 * implementation of this class is provided in {@link Point3D}.
 * 
 * @author K Udo Schuermann
 * @see Map3D
 * @see Point3D
 */
public interface Location3D
{
  /**
   * The distance along the X-axis (on the flat of the plane).
   * 
   * @return The distance along the X-axis in arbitrary distance units.
   */

  public double getX();


  /**
   * The distance along the Y-axis (on the flat of the plane).
   * 
   * @return The distance along the Y-axis in arbitrary distance units.
   */
  public double getY();


  /**
   * The distance along the Z-axis (up/down above/blow the X/Y plane).
   * 
   * @return The distance along the Z-axis in arbitrary distance units.
   */
  public double getZ();


  /**
   * Obtains the distance from this Location3D to another.
   * 
   * @param location
   *          The other location
   * @return The distance between this and the other location in arbitrary
   *         distance units.
   */
  public double distanceTo( final Location3D location );


  public double distanceTo( final double x,
	                    final double y,
	                    final double z );


  /**
   * Add a {@link MovementListener} that is notified whenever this location in
   * 3D space changes.
   * 
   * @param l
   *          The MovementListener to be added.
   * @see #removeMovementListener(ChangeListener)
   */
  public void addMovementListener( final MovementListener<? extends Location3D> l );


  /**
   * Remove a {@link MovementListener} so that it is no longer notified whenever
   * this location in 3D space changes. When the last MovementListener is
   * removed, the internal structure is cleaned up to recover its storage.
   * 
   * @param l
   *          The MovementListener to be added.
   * @See {@link #addMovementListener(MovementListener)}
   */
  public void removeMovementListener( final MovementListener<? extends Location3D> l );


  public interface MovementListener<T extends Location3D>
  {
    public void locationMoved( MovementEvent<T> e );
  }


  final class MovementEvent<T extends Location3D>
    extends EventObject
  {
    final double x, y, z;
    final T location3D;


    public MovementEvent( final double x,
	                  final double y,
	                  final double z,
	                  final T location3D )
    {
      super( location3D );
      this.location3D = location3D;
      this.x = x;
      this.y = y;
      this.z = z;
    }


    public T getLocation3D()
    {
      return location3D;
    }

    private static final long serialVersionUID = -167877702239258661L;
  }
}

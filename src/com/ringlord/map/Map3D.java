package com.ringlord.map;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.ringlord.map.Location3D.MovementEvent;
import com.ringlord.map.Location3D.MovementListener;


/**
 * <p>
 * A Map that operates in 3D space, allowing fast retrievals of elements at or
 * near a location in 3D space, without having to examine all stored objects for
 * distance. It accomplishes this using <i>compartments</i>, subspace cubes
 * within which only a subset of the total stored elements are stored. Depending
 * on the distribution of objects in 3D space many compartments will be empty
 * and consume no storage, some may store a few objects, and others may store a
 * large number. A search within any given compartment is linear.
 * </p>
 * 
 * <p>
 * The size of each compartment is defined at the time of construction. Moving a
 * {@link Location3D} object causes its location with the compartment map to be
 * adjusted as necessary (Map3D registers itself as a {@link MovementListener}
 * on each stored Location3D).
 * </p>
 * 
 * <p>
 * Example: One million objects distributed randomly in 200×200×200 unit space
 * (each item's coordinates randomly generated in the range [-200..+200]), using
 * a compartment size of 2.0, may search and compute distance to a reference
 * point for fewer than 100 objects, which is 10000× times faster than if the
 * entire 3D space had to be searched. Depending on the desired range of objects
 * in 3D space to be collected, more several thousand objects may need to be
 * searched, but that is still hundreds of times faster than a full space
 * search.
 * </p>
 * 
 * <p>
 * For optimum performance vs. storage space, it is suggested to experiment, as
 * no single compartment size can address every need: The smaller the size of
 * the compartments in relation to the entire range of coordinates used, the
 * larger the number of actual compartments that need storing, but each
 * compartment will also hold fewer elements that need searching (linearly). A
 * large compartment size in relation to the total mapped space can save some
 * space, but may result in increased time spent on (linear) searches.
 * </p>
 * 
 * @author K Udo Schuermann
 */
public class Map3D
  implements
    MovementListener,
    Iterable<Location3D>
{
  /**
   * Construct a Map3D object with an arbitrarily chosen compartment size of 1.5
   * 
   * @see #Map3D(double)
   */
  public Map3D()
  {
    this( 1.5d );
  }


  /**
   * Construct a Map3D object with a chosen compartment size.
   * 
   * @param compartmentSize
   *          The desired compartment size; for a discussion on the meaning of
   *          this value, please see the {@linkplain Map3D class-level
   *          description}.
   */
  public Map3D( final double compartmentSize )
  {
    super();
    this.compartmentSize = Math.abs( compartmentSize );
  }


  /**
   * Remove all {@link Location3D} elements from the map.
   */
  public synchronized void clear()
  {
    for( final List<Location3D> itemList : compartments.values() )
      {
	for( final Location3D item : itemList )
	  {
	    item.removeMovementListener( this );
	  }
	itemList.clear();
      }
    compartments.clear();
    size = 0;
    version++;
  }


  /**
   * How many {@link Location3D} elements are in the map.
   * 
   * @return The number of elements, a number 0 or greater.
   */
  public int size()
  {
    return size;
  }


  public boolean isEmpty()
  {
    return (size == 0);
  }


  /**
   * Add a {@link Location3D}. This causes the Map3D to add itself as a
   * {@link MovementListener} to the Location3D, allowing the Location3D to be
   * moved safely. It is safe (but pointless) to add an item multiple times.
   * 
   * @param item
   *          The item to add. This value must not be null.
   * @see #remove(Location3D)
   */
  public synchronized void store( final Location3D item )
  {
    final String key = makeKey( item.getX(),
	                        item.getY(),
	                        item.getZ() );
    List<Location3D> itemList = compartments.get( key );
    if( itemList == null )
      {
	itemList = new ArrayList<>();
	compartments.put( key,
	                  itemList );
      }
    if( itemList.add( item ) )
      {
	item.addMovementListener( this );
	size++;
	version++;
      }
  }


  /**
   * Remove a {@link Location3D}. This causes the Map3D to remove itself from
   * the Location3D. It is safe (but pointless) try to remove an item that is
   * not currently stored.
   * 
   * @param item
   *          The item to remove. This value must not be null.
   */
  public synchronized void remove( final Location3D item )
  {
    item.removeMovementListener( this );
    final String key = makeKey( item.getX(),
	                        item.getY(),
	                        item.getZ() );
    final List<Location3D> itemList = compartments.get( key );
    if( (itemList != null) && (itemList.remove( item )) )
      {
	// If the item is moved before we actually remove the MovementListener,
	// then our listener implementation will not find the item in the list
	// anymore. This is an unfortunate, but extremely rare situation, which
	// we accept in order to avoid an extra 'contains' method call.
	item.removeMovementListener( this );
	size--;
	version++;
      }
  }


  /**
   * <p>
   * Get all {@link Location3D} objects that are no farther than a certain
   * distance from a certain reference location.
   * </p>
   * 
   * <p>
   * NOTE: To retrieve a significant subset of elements from the map, it is far
   * more resource efficient to use {@link #iterator()} than a large 'range'
   * value with this method.
   * </p>
   * 
   * @param referenceLocation3D
   *          The reference location from which the distance of all desired
   *          elements will be computed.
   * @param range
   *          The maximum range for all elements to be retrieved.
   * @return A list of zero or more Location3D objects, all of which are no
   *         farther from the given reference location than the given range.
   * @see #nearestTo(double,double,double, double)
   */
  public synchronized List<Location3D> getAllWithin( final Location3D referenceLocation3D,
	                                             final double range )
  {
    return getAllWithin( referenceLocation3D.getX(),
	                 referenceLocation3D.getY(),
	                 referenceLocation3D.getZ(),
	                 range );
  }


  /**
   * <p>
   * Get all {@link Location3D} objects that are no farther than a certain
   * distance from a certain reference location.
   * </p>
   * 
   * <p>
   * NOTE: To retrieve a significant subset of elements from the map, it is far
   * more resource efficient to use {@link #iterator()} than a large 'range'
   * value with this method.
   * </p>
   * 
   * @param fromX
   *          x-coordinate of the reference location from which the distance of
   *          all desired elements will be computed.
   * @param fromY
   *          y-coordinate of the reference location from which the distance of
   *          all desired elements will be computed.
   * @param fromZ
   *          z-coordinate of the reference location from which the distance of
   *          all desired elements will be computed.
   * @param range
   *          The maximum range for all elements to be retrieved.
   * @return A list of zero or more Location3D objects, all of which are no
   *         farther from the given reference location than the given range.
   * @see #nearestTo(Location3D, double)
   */
  @SuppressWarnings("unused")
  public synchronized List<Location3D> getAllWithin( final double fromX,
	                                             final double fromY,
	                                             final double fromZ,
	                                             final double range )
  {
    final List<Location3D> result = new ArrayList<>();
    final double absError = (compartmentSize / 2.0d);

    final double startX = fromX - absError;
    final double startY = fromY - absError;
    final double startZ = fromZ - absError;

    final double endX = fromX + absError;
    final double endY = fromY + absError;
    final double endZ = fromZ + absError;

    // Unroll these loops to prioritize search for inner-most entries, and
    // search the farthest compartments last.as
    searchSet.clear();
    double x = startX;
    while( x <= endX )
      {
	double y = startY;
	while( y <= endY )
	  {
	    double z = startZ;
	    while( z <= endZ )
	      {
		final String key = makeKey( x,
		                            y,
		                            z );
		final List<Location3D> compartment = compartments.get( key );
		if( compartment != null )
		  {
		    searchSet.add( compartment );
		    if( TRACEABLE && trace )
		      {
			System.out.println( "{" + x + "," + y + "," + z + "} => Compartment " + key + ": POPULATED" );
		      }
		  }
		else if( TRACEABLE && trace )
		  {
		    System.out.println( "{" + x + "," + y + "," + z + "} => Compartment " + key + ": EMPTY" );
		  }
		z += absError;
	      }
	    y += absError;
	  }
	x += absError;
      }

    for( final List<Location3D> itemList : searchSet )
      {
	for( final Location3D item : itemList )
	  {
	    final double d = item.distanceTo( fromX,
		                              fromY,
		                              fromZ );
	    if( d <= range )
	      {
		result.add( item );
	      }
	  }
      }

    return result;
  }


  /**
   * Get the nearest object in 3D space to the given reference location, but no
   * farther than the given range.
   * 
   * @param referenceLocation3D
   *          The reference location from which the distance to the nearest
   *          element is computed.
   * @param range
   *          The maximum range beyond which elements will not be considered.
   * @return A {@link Location3D} that is no farther from the given reference
   *         location than the range, or null if no element could be found
   *         within that sphere.
   * @see #getAllWithin(double, double, double, double)
   */
  public synchronized Location3D nearestTo( final Location3D referenceLocation3D,
	                                    final double range )
  {
    return nearestTo( referenceLocation3D.getX(),
	              referenceLocation3D.getY(),
	              referenceLocation3D.getZ(),
	              range );
  }


  /**
   * Get the nearest object in 3D space to the given reference location, but no
   * farther than the given range.
   * 
   * @param fromX
   *          The x-coordinate of the reference location from which the distance
   *          to the nearest element is computed.
   * @param fromY
   *          The y-coordinate of the reference location from which the distance
   *          to the nearest element is computed.
   * @param fromZ
   *          The z-coordinate of the reference location from which the distance
   *          to the nearest element is computed.
   * @param range
   *          The maximum range beyond which elements will not be considered.
   * @return A {@link Location3D} that is no farther from the given reference
   *         location than the range, or null if no element could be found
   *         within that sphere.
   * @see #getAllWithin(Location3D, double)
   */
  public synchronized Location3D nearestTo( final double fromX,
	                                    final double fromY,
	                                    final double fromZ,
	                                    final double range )
  {
    Location3D nearestItem = null;
    double distance = Double.MAX_VALUE;

    for( final Location3D item : getAllWithin( fromX,
	                                       fromY,
	                                       fromZ,
	                                       range ) )
      {
	final double d = item.distanceTo( fromX,
	                                  fromY,
	                                  fromZ );
	if( d < distance )
	  {
	    distance = d;
	    nearestItem = item;
	  }
      }
    return nearestItem;
  }


  /**
   * Generates the hash key for a compartment.
   * 
   * @param x
   *          The x-coordinate for the Location3D to be stored.
   * @param y
   *          The y-coordinate for the Location3D to be stored.
   * @param z
   *          The z-coordinate for the Location3D to be stored.
   * @return A hash key that identifies the compartment uniquely.
   */
  private String makeKey( final double x,
	                  final double y,
	                  final double z )
  {
    final int cx = (int)(x / compartmentSize);
    final int cy = (int)(y / compartmentSize);
    final int cz = (int)(z / compartmentSize);

    return String.valueOf( cx ) + "," + String.valueOf( cy ) + "," + String.valueOf( cz );
  }


  /**
   * The implementation of the {@link MovementListener} which allows Map3D to
   * move Location3D elements from one compartment to another if required.
   */
  @Override
  public synchronized void locationMoved( final MovementEvent e )
  {
    final Location3D item = e.location3D;
    final String oldKey = makeKey( e.x,
	                           e.y,
	                           e.z );
    final String newKey = makeKey( item.getX(),
	                           item.getY(),
	                           item.getZ() );
    if( oldKey != newKey )
      {
	List<Location3D> itemList = compartments.get( oldKey );
	if( (itemList != null) && itemList.remove( item ) )
	  {
	    version++;
	    // This is MOST of what add(Location3D) does, but in the interest of
	    // performance we are skipping the things that add() does but
	    // don't need doing here.
	    itemList = compartments.get( newKey );
	    if( itemList == null )
	      {
		itemList = new ArrayList<>();
		compartments.put( newKey,
		                  itemList );
	      }
	    itemList.add( item );
	    version++;
	  }
	else
	  {
	    // We can get here in the rare (very rare) situation where the
	    // Location3D is removed from the list JUST BEFORE the listener
	    // is removed, too (see 'remove' method).
	  }
      }
  }


  /**
   * <p>
   * Produces an efficient iterator to expose all elements in this Map3D. The
   * order of the elements depends on a variety of internal structures and is
   * not predictable.
   * </p>
   * 
   * <p>
   * Note that the Map3D must not be modified, otherwise the next call to
   * {@link #next()} will throw a {@link ConcurrentModificationException()}.
   * </p>
   * 
   * @return An Iterator over all elements in this Map3D.
   */
  @Override
  public Iterator<Location3D> iterator()
  {
    return new Map3DIterator( this );
  }


  private class Map3DIterator
    implements
      Iterator<Location3D>
  {
    public Map3DIterator( final Map3D map3d )
    {
      super();
      this.map3d = map3d;
      this.version = map3d.version;
      this.valueSets = map3d.compartments.values().iterator();
      this.remaining = map3d.size();
    }


    @Override
    public boolean hasNext()
    {
      return (remaining > 0);
    }


    @Override
    public Location3D next()
    {
      if( remaining > 0 )
	{
	  synchronized( map3d )
	    {
	      if( version != map3d.version )
		{
		  throw new ConcurrentModificationException( "Must not modify Map3D while iterating over its elements!" );
		}

	      if( (values == null) || !values.hasNext() )
		{
		  values = valueSets.next().iterator();
		}
	      --remaining;
	      return values.next();
	    }
	}
      else
	{
	  throw new NoSuchElementException();
	}
    }


    @Override
    public void remove()
    {
      System.err.println( "Map3DIterator does not support remove() operation" );
    }

    private Iterator<Location3D> values;
    private int remaining;
    //
    private final Iterator<List<Location3D>> valueSets;
    private final Map3D map3d;
    private final long version;
  }

  private int size;
  private boolean trace;
  // By starting the counter a the smallest (negative) value, we should be able
  // to allow for ~2^64 rather than merely ~2^63 modifications, not that we
  // ever intend to run out with a 64-bit number, but why waste it?
  private transient long version = Long.MIN_VALUE;
  //
  private final Set<List<Location3D>> searchSet = new HashSet<>();
  private final Map<String,List<Location3D>> compartments = new HashMap<>();
  private final double compartmentSize;
  /**
   * Set this to 'true' to dump debugging output to the console.
   */
  private static final boolean TRACEABLE = false;
}

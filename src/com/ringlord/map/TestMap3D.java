package com.ringlord.map;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * Runs some tests on the Map3D class to test performance and ensure
 * correctness.
 * 
 * @author K Udo Schuermann
 */
public class TestMap3D
{
  public static void main( final String[] args )
  {
    int testCount;
    try
      {
	testCount = Integer.parseInt( args[0] );
      }
    catch( ArrayIndexOutOfBoundsException |
	   NumberFormatException x )
      {
	testCount = 10;
      }
    final Random random = new Random();
    int failures = 0;
    for( int i = 0; i < testCount; i++ )
      {
	System.out.println( new Date() + ": Test #" + (i + 1) );
	if( !test( random ) )
	  {
	    failures++;
	  }
      }
    System.out.println( "Total tests run:  " + testCount );
    System.out.println( "Total tests OK:   " + (testCount - failures) );
    System.out.println( "Total tests FAIL: " + failures );
  }


  public static boolean test( final Random random )
  {
    boolean testResult = true;
    final int COUNT = 1_000_000;
    final double SEARCH = 5.0d;

    final Set<Location3D> stored = new HashSet<>();
    final Map3D<Location3D> map3d = new Map3D<>( 1.5d );
    for( int i = 0; i < COUNT; i++ )
      {
	final double x = (random.nextDouble() * 100.0d) - 50.0d;
	final double y = (random.nextDouble() * 100.0d) - 50.0d;
	final double z = (random.nextDouble() * 100.0d) - 50.0d;

	final Location3D n = new Point3D().setCartesian( x,
	                                                 y,
	                                                 z );
	map3d.store( n );
	stored.add( n );
      }

    final Point3D CENTER = new Point3D();
    CENTER.setCartesian( random.nextDouble() * 100.0d - 50.0d,
	                 random.nextDouble() * 100.0d - 50.0d,
	                 random.nextDouble() * 100.0d - 50.0d );
    final long searchStart = System.nanoTime();
    final List<Location3D> result = map3d.getAllWithin( CENTER,
	                                                SEARCH );
    final long searchTime = (System.nanoTime() - searchStart);
    System.out.println( "\tLocate " +
	                result.size() +
	                " from " +
	                map3d.size() +
	                " elements no more than " +
	                SEARCH +
	                " units of " +
	                CENTER +
	                ": " +
	                String.format( "%1.3f ms",
	                               searchTime / 1_000_000.0d ) );

    int found = 0;
    final long iterateStart = System.nanoTime();
    // Yes, there is a faster way to count items, but we really WANT iteration
    // here to time it:
    for( @SuppressWarnings("unused") final Location3D item : map3d )
      {
	found++;
      }
    final long iterateTime = System.nanoTime() - iterateStart;
    if( (found == map3d.size()) && (found == COUNT) )
      {
	System.out.println( "\tIterate all items: " + String.format( "%1.3f ms",
	                                                             iterateTime / 1_000_000.0d ) );
      }
    else
      {
	System.out.println( "\tFAIL: Iterated " +
	                    found +
	                    " instead of " +
	                    COUNT +
	                    " items: " +
	                    String.format( "%1.3f ms",
	                                   iterateTime / 1_000_000.0d ) );
	testResult = false;
      }

    int failedNull = 0;
    int failedWrong = 0;
    final List<Location3D> missing = new ArrayList<>();
    final double ACCURACY;
    switch( (int)(Math.random() * 7) )
      {
      case 0:
	ACCURACY = 0.000_001d;
	break;
      case 1:
	ACCURACY = 0.000_01d;
	break;
      case 2:
	ACCURACY = 0.000_1d;
	break;
      case 3:
	ACCURACY = 0.001d;
	break;
      case 4:
	ACCURACY = 0.01d;
	break;
      case 5:
	ACCURACY = 0.1;
	break;
      case 6:
	ACCURACY = 1.0d;
	break;
      default:
	ACCURACY = 10.0d;
      }
    final long verifyStart = System.nanoTime();
    for( final Location3D item : map3d )
      {
	final Location3D check = map3d.nearestTo( item,
	                                          ACCURACY );
	if( check == null )
	  {
	    failedNull++;
	    missing.add( item );
	  }
	else if( check != item )
	  {
	    failedWrong++;
	  }
      }
    final long verifyTime = System.nanoTime() - verifyStart;
    if( (failedNull == 0) && (failedWrong == 0) )
      {
	System.out.println( "\tAll elements located by proximity searches (range " +
	                    ACCURACY +
	                    "): " +
	                    String.format( "%1.3f ms",
	                                   verifyTime / 1_000_000.d ) );
      }
    else
      {
	testResult = false;
	if( failedNull > 0 )
	  {
	    System.out.println( "\tFAIL: " + failedNull + " elements could not be located by proximity search" );
	  }
	if( failedWrong > 0 )
	  {
	    System.out.println( "\tFAIL: " + failedWrong + " elements were not the expected promixity search result" );
	  }
	/*
	 * map3d.trace = true; for( Location3D item : missing ) {
	 * map3d.nearestTo( item, ACCURACY ); } map3d.trace = false;
	 */
      }

    for( final Location3D item : map3d )
      {
	stored.remove( item );
      }
    if( stored.isEmpty() )
      {
	System.out.println( "\tIterator reproduced all expected elements." );
      }
    else
      {
	System.err.println( "\tFAIL: Iterator did not find " + stored.size() + " expected elements." );
	testResult = false;
      }

    return testResult;
  }
}

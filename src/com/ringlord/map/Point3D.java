package com.ringlord.map;

/**
 * An implementation of {@link Location3D} that you may extend with
 * functionality of your own.
 * 
 * @author K Udo Schuermann
 * @see Map3D
 * @see Location3D
 */
public class Point3D
  extends Location3D
{
  public Point3D()
  {
    super();
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
}

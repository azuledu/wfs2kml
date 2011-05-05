/**
 * 
 */
package es.uva.idelab;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

/**
 * @author edurie
 * 
 */
class PrismService implements CoordinateFilter {


	public void filter(Coordinate c) {
		c.z = 0;
	}

	
	/*		
	// Coordinate z
	while (featuresIterator.hasNext()) {
		// Validate zAttribute
		if (zAttribute != null) { // .length() != 0 ) { // If the user has selected the height attribute
			SimpleFeatureType featureType = f.getFeatureType();

			int attrPos = featureType.indexOf(zAttribute);
			if (attrPos == -1) { // If the attribute doesn't exist
				zCoord = 0;
			} else { // If the attribute exist
				// Define zCoord
				if (!(f.getAttribute(attrPos) instanceof Number)) {
					if (logger.isDebugEnabled()) logger.debug("Height attribute isn't a number");
					zCoord = 0;
				}
				if ((f.getAttribute(attrPos) instanceof Float) || (f.getAttribute(attrPos) instanceof Double)) {
					Double zCoordDouble = (Double) f.getAttribute(attrPos);
					zCoord = zCoordDouble.doubleValue();
				} else {
					Long zCoordLong = (Long) f.getAttribute(attrPos);
					zCoord = zCoordLong.longValue();
				}
			}

			for (int j = 0; j < coord_simp.length; j++) {
				kmlout.write(coord_simp[j].x + "," + coord_simp[j].y
						+ "," + zCoord / scale + "\n");
			}
		} else { // If the user hasn't selected the height attribute
					// (Geometries with 3 coordinates)
			for (int j = 0; j < coord_simp.length; j++) {
				kmlout.write(coord_simp[j].x + "," + coord_simp[j].y
						+ "," + coord_simp[j].z / scale + "\n");
			} // TODO sustituir NaN en coord z por 0
		}
	}*/
		
	
	
	
}

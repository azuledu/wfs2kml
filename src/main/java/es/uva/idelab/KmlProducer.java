package es.uva.idelab;

import org.apache.log4j.Logger;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.SimpleGeometryPrecisionReducer;

/**
 * Utilities to convert features to kml (partially taken from geotools
 * testcases).
 */
public class KmlProducer {

	@SuppressWarnings("nls")
	public static final String[] IGNORED_ATTR = { "LookAt", "Style", "Region" };
	static Logger logger = Logger.getLogger("WFS2KMLServlet.class");

	/**
	 * Writes the {@link FeatureCollection} to disk in KML format.
	 * 
	 * @param kmlFile
	 *            the file to write.
	 * @param featureCollection
	 *            the collection to transform.
	 * @throws Exception
	 */
	public static SimpleFeatureCollection Simplify(SimpleFeatureCollection featureCollection) throws Exception {

		
		System.setProperty("org.geotools.referencing.forceXY", "true");
		if (logger.isDebugEnabled()) 
			logger.debug("org.geotools.referencing.forceXY Property: " + System.getProperty("org.geotools.referencing.forceXY"));
		
//        // make sure we output in 4326 since that's what KML mandates
//		CoordinateReferenceSystem sourceCrs = featureCollection.getSchema().getCoordinateReferenceSystem();
//        MathTransform mtrans = CRS.findMathTransform(sourceCrs, WGS84, true);
		
		SimpleFeatureCollection newCollection = FeatureCollections.newCollection();
		SimpleFeatureIterator featuresIterator = featureCollection.features();
		while (featuresIterator.hasNext()) {
			SimpleFeature f = featuresIterator.next();
			Geometry g = (Geometry) f.getDefaultGeometry();
//			if (!mtrans.isIdentity()) {   
//                g = JTS.transform(g, mtrans);   
//			}
			// Simplify
			// TODO http://docs.geotools.org/latest/userguide/guide/library/data/pregeneralized.html
			int numDecPlaces = 5; // 5 decimal digits
			double scale = Math.pow(10, numDecPlaces);
			PrecisionModel pm = new PrecisionModel(scale);
			g = SimpleGeometryPrecisionReducer.reduce(g, pm);
			double tolerance = 0.01; // TODO Tolerance=0.01
			g = TopologyPreservingSimplifier.simplify(g, tolerance); // Douglas-Peucker algorithm.

			// Coordinate[] coord = g.getCoordinates();
			// Coordinate[] coord_simp =
			// TopologyPreservingSimplifier.simplify(coord, 0.01);

			/*
			 * PrismService toprism = new PrismService(); g.apply(toprism);
			 * 
			 * */ 
			 f.setDefaultGeometry(g); 
			 newCollection.add(f);
			 
			 // TODO Revisar-Creo q no hace falta hacer newCollection, se puede modificar featureCollection y devolverla.
			 // VER este ejemplo:
			 /*  FeatureIterator iterator=collection.features();
				 try {
				     while( iterator.hasNext()  ){
				          Feature feature = iterator.next();
				          System.out.println( feature.getID() );
				     }
				 }
				 finally {
				     collection.close( iterator );
				 } */
		}
		// LinearRingTypeBinding lin = new
		// org.geotools.kml.bindings.LinearRingTypeBinding();
		// PlacemarkTypeBinding pl=new PlacemarkTypeBinding();
		// pl.
		
		return (newCollection);
	}

}
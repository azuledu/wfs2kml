package es.uva.idelab;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.servlet.http.*;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.SimpleGeometryPrecisionReducer;

/**
 * Utilities to convert features to kml (partially taken from geotools testcases).
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
	public static String generateKml(SimpleFeatureCollection featureCollection, String zAttribute) throws Exception {

		double zCoord;

		System.setProperty("org.geotools.referencing.forceXY", "true");
		
		// Reproject to Google CRS
		CoordinateReferenceSystem epsg4326 = DefaultGeographicCRS.WGS84;  // TODO 900913 ??
		CoordinateReferenceSystem crs = featureCollection.getSchema().getCoordinateReferenceSystem();
		MathTransform mtrans = CRS.findMathTransform(crs, epsg4326, true);

		FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();
		FeatureIterator<SimpleFeature> featuresIterator = featureCollection.features();
		while (featuresIterator.hasNext()) {
			SimpleFeature f = featuresIterator.next();
			Geometry g = (Geometry) f.getDefaultGeometry();
			if (!mtrans.isIdentity()) {   
				g = JTS.transform(g, mtrans);   
			}
		// Simplify
		// TODO http://docs.geotools.org/latest/userguide/guide/library/data/pregeneralized.html
		int numDecPlaces = 5; // 5 decimal digits
		double scale = Math.pow(10, numDecPlaces);
		PrecisionModel pm = new PrecisionModel(scale); 
		g = SimpleGeometryPrecisionReducer.reduce(g,pm);
		double tolerance=0.01;  // TODO Tolerance=0.01
		g = TopologyPreservingSimplifier.simplify(g, tolerance); // Douglas-Peucker algorithm. 

		//Coordinate[] coord = g.getCoordinates();
		//Coordinate[] coord_simp = TopologyPreservingSimplifier.simplify(coord, 0.01);

		PrismService toprism = new PrismService();
		g.apply(toprism);
		
		
		f.setDefaultGeometry(g);
		newCollection.add(f);
		}
		// Encode to XML 
		Encoder encoder = new Encoder(new KMLConfiguration());
		encoder.setIndenting(true);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(newCollection, KML.kml, out);

		String kmlString = new String(out.toByteArray());
		return(kmlString);
	}
	
	public static void previewKml(HttpServletResponse response, String kmlString) throws Exception {
		response.setContentType("application/xml");
		PrintWriter kmlout = response.getWriter();
		kmlout.write(kmlString);
	}
	
	public static void downloadKml(String kmlString) throws Exception {
		String filePath = "~/kmlout.kml";
		File kmlFP = new File(filePath);
		FileWriter kmlFW = new FileWriter(kmlFP);
		BufferedWriter bW = null;
		try {
			bW = new BufferedWriter(kmlFW);
			bW.write(kmlString);
		} finally {
			if (bW != null)
				bW.close();
		}
	}
}
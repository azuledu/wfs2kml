package es.uva.idelab;

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

/**
 * Utilities to convert features to kml (taken from geotools testcases).
 */
public class KmlProducer {

	@SuppressWarnings("nls")
	public static final String[] IGNORED_ATTR = { "LookAt", "Style", "Region" };

	/**
	 * Writes the {@link FeatureCollection} to disk in KML format.
	 * 
	 * @param kmlFile
	 *            the file to write.
	 * @param featureCollection
	 *            the collection to transform.
	 * @throws Exception
	 */
	public static String generateKml(SimpleFeatureCollection featureCollection) throws Exception {
		CoordinateReferenceSystem epsg4326 = DefaultGeographicCRS.WGS84;  // TODO 900913 ??
		CoordinateReferenceSystem crs = featureCollection.getSchema().getCoordinateReferenceSystem();
		MathTransform mtrans = CRS.findMathTransform(crs, epsg4326, true);

		FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();
		FeatureIterator<SimpleFeature> featuresIterator = featureCollection.features();
		while (featuresIterator.hasNext()) {
			SimpleFeature f = featuresIterator.next();
			Geometry g = (Geometry) f.getDefaultGeometry();
		/*	if (!mtrans.isIdentity()) {   // Si lo descomento funciona con geoserver pero no con Mapserver.
				g = JTS.transform(g, mtrans);   // o bien, funciona con 4326 pero no con 4258
			}*/
			f.setDefaultGeometry(g);
			newCollection.add(f);
		}

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
		String filePath = "/home/edurie/kmlout.kml";
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
package es.uva.idelab;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.DouglasPeuckerLineSimplifier;
import com.vividsolutions.jts.geom.Envelope;

public class KMLProducer_bak {

	private static final Log logger = LogFactory.getLog(WFS2KMLServlet.class);
	private PrintWriter kmlout;
	private String typeName;
	private String zAttribute;
	private double scale;
	private double tolerance;

	public KMLProducer_bak(PrintWriter kmlout, String typeName, double tolerance, String zAttribute, double scale) {
		this.kmlout = kmlout;
		this.typeName = typeName;
		this.tolerance = tolerance;
		this.zAttribute = zAttribute;
		this.scale = scale;
		if (logger.isDebugEnabled())
			logger.debug("KMLProducer: zAttribute=" + this.zAttribute
					+ ", scale=" + this.scale);

	}

	public void createFile(
			PrintWriter kmlout,
			FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
			Envelope bbox, CoordinateReferenceSystem geomCRS) {
		try {
			kmlout.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			kmlout.write("<kml xmlns=\"http://earth.google.com/kml/2.2\">\n\n");
			kmlout.write("<Document>\n");
			kmlout.write("<name>" + typeName + "</name>\n");

			kmlout.write("\n<Folder>\n");
			kmlout.write("<name>" + typeName + "</name>\n");
			kmlout.write("<open>1</open>\n");
			kmlout.write("<description>test file</description>\n");

			kmlStyle();
			kmlRegion(bbox);
			kmlPlacemarks(featureCollection, geomCRS);

			kmlout.write("</Folder>\n");
			kmlout.write("</Document>\n");
			kmlout.write("</kml>\n");

			kmlout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Define the style associated to the features
	 */
	private void kmlStyle() {
		kmlout.write("\n<Style id=\"default\">\n");
		kmlout.write("	<LineStyle>\n");
		kmlout.write("		<width>1.5</width>\n");
		kmlout.write("	</LineStyle>\n");
		kmlout.write("	<PolyStyle>\n");
		kmlout.write("		<color>7d00ffff</color>\n");
		kmlout.write("	</PolyStyle>\n");
		kmlout.write("</Style>\n");
	}

	/**
	 * @param bbox
	 *            Space region that defines the KML viewer focus. All the
	 *            features are inside of this Bounding Box
	 */
	private void kmlRegion(Envelope bbox) {

		kmlout.write("\n<Region>\n");
		kmlout.write("	<LatLonAltBox>\n");
		kmlout.write("		<north>" + bbox.getMaxY() + "</north>\n");
		kmlout.write("		<south>" + bbox.getMinY() + "</south>\n");
		kmlout.write("		<east>" + bbox.getMaxX() + "</east>\n");
		kmlout.write("		<west>" + bbox.getMinX() + "</west>\n");
		kmlout.write("		<altitudeMode>relativeToGround</altitudeMode>\n");
		kmlout.write("	</LatLonAltBox>\n");
		kmlout.write("	<Lod>\n");
		kmlout.write("		<minLodPixels>0</minLodPixels>\n"); // TODO Revisar
		kmlout.write("	</Lod>\n");
		kmlout.write("</Region>\n");
	}

	/**
	 * @param featureCollection
	 *            All the features to be represented
	 */
	private void kmlPlacemarks(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,CoordinateReferenceSystem geomCRS) {
		FeatureIterator<SimpleFeature> iterator = featureCollection.features(); // Feature
		try {
			// for( int f=0; iterator.hasNext(); f++) {
			// SimpleFeature feature = (SimpleFeature) iterator.next();
			while (iterator.hasNext()) {
				SimpleFeature feature = (SimpleFeature) iterator.next();
				kmlout.write("\n<Placemark>\n");
				kmlout.write("<name>" + feature.getID() + "</name>\n");
				kmlout.write("<styleUrl>default</styleUrl>\n");

				kmlGeometries(feature, geomCRS);

				kmlout.write("</Placemark>\n");
			}
		} finally {
			featureCollection.close(iterator);
		}
	}

	/**
	 * @param feature
	 *            Feature to extract their geometries
	 */
	private void kmlGeometries(SimpleFeature feature, CoordinateReferenceSystem geomCRS) {

		boolean multiGeometry = false;
		double zCoord;
		CoordinateReferenceSystem kmlCRS;

		try {

			kmlCRS = CRS.decode("EPSG:4326");
			Geometry defaultGeometry = (Geometry) feature.getDefaultGeometry();
			if (defaultGeometry.getNumGeometries() > 1) {
				kmlout.write("<MultiGeometry>\n");
				multiGeometry = true;
			}
			for (int g = 0; g < defaultGeometry.getNumGeometries(); g++) { // Geometry
				kmlout.write("<Polygon>\n"); // TODO geometrias diferentes de "poligon"
				kmlout.write("<extrude>1</extrude>\n");
				// kmlout.write("<tessellate>1</tessellate>\n"); TODO Activar para poligonos grandes.
				kmlout.write("<altitudeMode>relativeToGround</altitudeMode>\n"); // TODO Otros altitudeMode

				kmlout.write("<outerBoundaryIs>\n"); // TODO prever InnerBoundaryIS

				kmlout.write("<LinearRing>\n");
				kmlout.write("<coordinates>\n");
				// JTSUtilities

				// zMinMax(Coordinate[] cs)
				// Determine the min and max "z" values in an array of Coordinates.

				// guessCoorinateDims(Coordinate[] cs)
				// Returns:
				// 2 for 2d (default)
				// 4 for 3d - one of the coordinates has a non-NaN z value
				// (3 is for x,y,m but thats not supported yet)

				// findBestGeometryType(Geometry geom)
				// Determine the best ShapeType for a given Geometry.

				Geometry geomGeometry = defaultGeometry.getGeometryN(g);

				MathTransform transform = CRS.findMathTransform(geomCRS, kmlCRS);
				Geometry kmlGeometry = JTS.transform(geomGeometry, transform);

				Coordinate[] coord = kmlGeometry.getCoordinates();
				// DouglasPeuckerLineSimplifier simplifier = new DouglasPeuckerLineSimplifier(coord);
				Coordinate[] coord_simp = DouglasPeuckerLineSimplifier.simplify(coord, tolerance);

				if (zAttribute != null) { // .length() != 0 ) { // If the user has selected the height attribute
					SimpleFeatureType featureType = feature.getFeatureType();

					int attrPos = featureType.indexOf(zAttribute);
					if (attrPos == -1) { // If the attribute doesn't exist
						zCoord = 0;
					} else { // If the attribute exist
						if (!(feature.getAttribute(attrPos) instanceof Number)) {
							System.out.print("Height attribute isn't a number");
							zCoord = 0;
						}
						if ((feature.getAttribute(attrPos) instanceof Float)
								|| (feature.getAttribute(attrPos) instanceof Double)) {
							Double zCoordDouble = (Double) feature
									.getAttribute(attrPos);
							zCoord = zCoordDouble.doubleValue();
						} else {
							Long zCoordLong = (Long) feature
									.getAttribute(attrPos);
							zCoord = zCoordLong.longValue();
						}

					}

					for (int j = 0; j < coord_simp.length; j++) {
						kmlout.write(coord_simp[j].x + "," + coord_simp[j].y
								+ "," + zCoord / scale + "\n");
					}
				} else { // If the user hasn't selected the height attribute (Geometries with 3 coordinates)
					for (int j = 0; j < coord_simp.length; j++) {
						kmlout.write(coord_simp[j].x + "," + coord_simp[j].y
								+ "," + coord_simp[j].z / scale + "\n");
					} // TODO sustituir NaN en coord z por 0
				}
				kmlout.write("\n</coordinates>\n");
				kmlout.write("</LinearRing>\n");
				kmlout.write("</outerBoundaryIs>\n");
				kmlout.write("</Polygon>\n");
			}
			if (multiGeometry)
				kmlout.write("</MultiGeometry>\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
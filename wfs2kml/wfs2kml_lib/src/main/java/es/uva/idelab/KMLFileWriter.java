package es.uva.idelab;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.io.IOException;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.geometry.jts.JTS;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Translate the geographic features information into a file in KML format.
 * 
 * @author Eduardo Riesco
 *
 */
public class KMLFileWriter {
	
	private PrintWriter kmlout;
	private String kmlFileName;
	private String zAttribute;  // z coordinate
	private double scale;		// heightParameter = zAttribute/scale
	private String typeName;	// Feature Type Name
	
	/**
	 * @param kmlFileName	Name of the output KML file
	 * @param zAttribute	Name of the feature attribute used as height 
	 * @param scale			HeightParameter = zAttribute/scale
	 * @param typeName		Feature Type Name. 
	 */
	public KMLFileWriter(String kmlFileName, String zAttribute, double scale, String typeName) {
		try{
			this.kmlFileName = kmlFileName;
			this.zAttribute = zAttribute;
			this.scale = scale;
			this.typeName = typeName;
			FileWriter fw = new FileWriter(kmlFileName);
			BufferedWriter bw = new BufferedWriter(fw);
			kmlout = new PrintWriter (bw);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Output KML file
	 * 
	 * @param featureCollection	Features to be translated into a KML format
	 * @param bbox				Space region that defines the KML viewer focus. 
	 * 							All the features are inside of this Bounding Box
	 * @param geomCRS			Primary Geometry Local Name
	 */
	public void createFile(FeatureCollection featureCollection, Envelope bbox, CoordinateReferenceSystem geomCRS){  
		try{				
			kmlout.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			kmlout.write("<kml xmlns=\"http://earth.google.com/kml/2.2\">\n\n");
			kmlout.write("<Document>\n");
			kmlout.write("<name>"+kmlFileName+"</name>\n");
			
			kmlout.write("\n<Folder>\n");
			kmlout.write("<name>"+typeName+"</name>\n"); 
			kmlout.write("<open>1</open>\n");
			kmlout.write("<description></description>\n");
		
			kmlStyle( );
			kmlRegion( bbox );
			kmlPlacemarks( featureCollection, geomCRS );
			
			kmlout.write("</Folder>\n");
			kmlout.write("</Document>\n");
			kmlout.write("</kml>\n");
			
			kmlout.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Define the style asociated to the features
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
	 * @param bbox 	Space region that defines the KML viewer focus. 
	 * 				All the features are inside of this Bounding Box
	 */
	private void kmlRegion(Envelope bbox) {

		kmlout.write("\n<Region>\n");
		kmlout.write("	<LatLonAltBox>\n");
		kmlout.write("		<north>"+bbox.getMaxY()+"</north>\n");
		kmlout.write("		<south>"+bbox.getMinY()+"</south>\n");
		kmlout.write("		<east>"+bbox.getMaxX()+"</east>\n");
		kmlout.write("		<west>"+bbox.getMinX()+"</west>\n");
//		kmlout.write("		<minAltitude>0</minAltitude>\n");
//		kmlout.write("		<maxAltitude>10000</maxAltitude>\n");   // Z coordinate attribute 
		kmlout.write("		<altitudeMode>relativeToGround</altitudeMode>\n");	
		kmlout.write("	</LatLonAltBox>\n"); 
		kmlout.write("	<Lod>\n");
		kmlout.write("		<minLodPixels>0</minLodPixels>\n");		//TODO Revisar
//		kmlout.write("		<maxLodPixels>-1</maxLodPixels>\n");
//		kmlout.write("		<minFadeExtent>0</minFadeExtent>\n");
//		kmlout.write("		<maxFadeExtent>0</maxFadeExtent>\n");
		kmlout.write("	</Lod>\n");
		kmlout.write("</Region>\n");
	}
	
	/**
	 * @param featureCollection	All the features to be represented
	 */
	private void kmlPlacemarks(FeatureCollection featureCollection, CoordinateReferenceSystem geomCRS) {
		Iterator iterator = featureCollection.iterator();			// Feature
        try {
            for( int f=0; iterator.hasNext(); f++) {
                Feature feature = (Feature) iterator.next();
				kmlout.write("\n<Placemark>\n");  						
				kmlout.write("<name>"+feature.getID() +"</name>\n");
				kmlout.write("<styleUrl>default</styleUrl>\n");
				
				kmlGeometries( feature, geomCRS );
				
				kmlout.write("</Placemark>\n");
            }
        }
        finally {
            	featureCollection.close( iterator );
        }
	}
	
	/**
	 * @param feature	Feature to extract their geometries
	 */
	private void kmlGeometries(Feature feature, CoordinateReferenceSystem geomCRS) {	
		boolean multiGeometry = false;
		double zCoord;
		CoordinateReferenceSystem kmlCRS;
		
		try {
			
		kmlCRS = CRS.decode("EPSG:4326");
		if (feature.getDefaultGeometry().getNumGeometries()>1){	
			kmlout.write("<MultiGeometry>\n");
			multiGeometry = true;
		}
		for(int g=0; g<feature.getDefaultGeometry().getNumGeometries(); g++){  // Geometry
			kmlout.write("<Polygon>\n");					// 	TODO geometrias diferentes de "poligon" 
			kmlout.write("<extrude>1</extrude>\n");
			//kmlout.write("<tessellate>1</tessellate>\n");  	TODO Activar para poligonos grandes.
			kmlout.write("<altitudeMode>relativeToGround</altitudeMode>\n");	// TODO Otros altitudeMode
			
			kmlout.write("<outerBoundaryIs>\n");  // TODO prever InnerBoundaryIS
			
			kmlout.write("<LinearRing>\n");
			kmlout.write("<coordinates>\n");
			// JTSUtilities
			
			// zMinMax(Coordinate[] cs)
	        //  Determine the min and max "z" values in an array of Coordinates.
			
			// guessCoorinateDims(Coordinate[] cs)
//	          Returns:
//	        	  2 for 2d (default)
//	        	  4 for 3d - one of the oordinates has a non-NaN z value
//	        	  (3 is for x,y,m but thats not supported yet) 
			
//			findBestGeometryType(Geometry geom)
//	          Determine the best ShapeType for a given Geometry.
			
			Geometry geomGeometry = feature.getDefaultGeometry().getGeometryN(g);
			
			MathTransform transform = CRS.findMathTransform(geomCRS, kmlCRS);
			Geometry kmlGeometry = JTS.transform( geomGeometry, transform);
				
			Coordinate coord[] = kmlGeometry.getCoordinates();
			
			if (zAttribute.length() > 0 ) {	// If the user has selected the height attribute
				FeatureType featureType = feature.getFeatureType();
				
				int attrPos = featureType.find(zAttribute);	
				if (attrPos == -1) {			// If the attribute doesn't exist
					zCoord = 0;
				}
				else {							// If the attribute exist
					if (!(feature.getAttribute(attrPos) instanceof Number)) {
						System.out.print("Height attribute isn't a number");
						zCoord = 0;
					}					
					if ((feature.getAttribute(attrPos) instanceof Float) || (feature.getAttribute(attrPos) instanceof Double)) {
						Double zCoordDouble = (Double)feature.getAttribute(attrPos); 
						zCoord = zCoordDouble.doubleValue();
					} else {
						Long zCoordLong = (Long)feature.getAttribute(attrPos); 
						zCoord = zCoordLong.longValue();
					}
					
				}		
	        	for(int j=0;j<coord.length;j++){
	        		kmlout.write( coord[j].x +"," + coord[j].y + "," + zCoord/scale + " "); 
	        	}
	        } else {	// If the user hasn't selected the height attribute (Geometries with 3 coordinates)
        		for(int j=0;j<coord.length;j++){
	        		kmlout.write( coord[j].x +"," + coord[j].y + "," + coord[j].z/scale + " ");
	        	}
        	}
	        
			kmlout.write("\n</coordinates>\n");
			kmlout.write("</LinearRing>\n");
			kmlout.write("</outerBoundaryIs>\n");
			kmlout.write("</Polygon>\n");
		}	
		if (multiGeometry) kmlout.write("</MultiGeometry>\n");
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
	


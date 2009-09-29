package es.uva.idelab;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.geometry.jts.ReferencedEnvelope; 
import org.geotools.referencing.CRS;

import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Entry point of the application
 * 
 * @author Eduardo Riesco
 *
 */
public class WFS_KML_gw_CLI {

	static String server;
	static String layer;
	static String zAttribute;  		// z coordinate
	static double scale = 1;		// heightParameter = zAttribute/scale
	static String kmlFileName = "test.kml";
	/**
	 * Entry point of the application
	 * 
	 * @param args	
	 */
	public static void main(String[] args) { 

		switch (args.length) {
		case 9:
			kmlFileName = args[8];
		case 8:
			scale = Double.valueOf( args[7] ).doubleValue();
		case 7:
			zAttribute = args[6];
			break;
		default:
			System.out.println("Usage: java es.uva.pfc.eduardoRiesco.WFS_KML_gw_CLA server namespace:layer xMin xMax yMin yMax [attribute] [scale] [KML_File_Name]");
			System.exit(0);
		}
		
		server = args[0];
		layer = args[1];
		double xMin = Double.valueOf( args[2] ).doubleValue();
		double xMax = Double.valueOf( args[3] ).doubleValue();
		double yMin = Double.valueOf( args[4] ).doubleValue();
		double yMax = Double.valueOf( args[5] ).doubleValue();
		
		try {
			supressInfo();
			
			// Connection
			WFSClient wfs = new WFSClient();
			wfs.getCapabilities( server ); 								
			// TODO No hay que elegir el layer, solo el Bbox y aparecen todos los layer con datos en ese Bbox.
			// Quiz� sea bueno que no se recuperen los datos, solo el nombre del layer y que los datos se 
			// recuperen cuando se seleccione la carpeta que representa ese layer en GEarth. 
			// ACTUALIZACION Un solo layer para que se pueda elegir el height attribute de ese layer
			String typeName = layer; 
			FeatureType schema = wfs.describeFeatureType( typeName ); 	// Feature
			
			// Query
			CoordinateReferenceSystem kmlCRS = CRS.decode("EPSG:4326");
			ReferencedEnvelope bbox = new ReferencedEnvelope( xMin, xMax, yMin, yMax, kmlCRS );			
			
			String geomName = schema.getPrimaryGeometry().getLocalName();
			CoordinateReferenceSystem geomCRS = schema.getPrimaryGeometry().getCoordinateSystem(); 
			
			CreateQuery query;
			if (!kmlCRS.equals(geomCRS)) { 	// Transform data CRS into KML standart CRS (4326) 
				ReferencedEnvelope geomBbox = bbox.transform( geomCRS, true, 10 ); // Sample 10 points around the envelope
				query = new CreateQuery(geomName, geomBbox);
			} else {
				query = new CreateQuery(geomName, bbox);
			}
			
			Query featuresIntersectsBbox = query.queryIntersects(typeName);
			
			//De momento s�lo devuelve una coleccion de features de un �nico FeatureType
			//habr�a que pasarle como argumento featureType[] -> NO, UN UNICO FEATURE PARA ELEGIR EL HEIGHT ATTRIBUTE			
			FeatureCollection featureCollection = wfs.getFeature( typeName, featuresIntersectsBbox );
			//Info.getFeature(schema);
			
            KMLFileWriter kmlFile = new KMLFileWriter( kmlFileName, zAttribute, scale, typeName );
            kmlFile.createFile( featureCollection, bbox, geomCRS );
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public static void supressInfo(){
		Logger.getLogger("org.geotools.gml").setLevel( Level.SEVERE );
		Logger.getLogger("net.refractions.xml").setLevel( Level.SEVERE);
	}
}

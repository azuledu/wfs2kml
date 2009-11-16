package es.uva.idelab;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Map;

//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;
import javax.servlet.http.*;

import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Servlet implementation.
 *
 */
 public class WFS_KML_gw_Servlet extends HttpServlet {
   static final long serialVersionUID = 1L;
   
	private PrintWriter kmlout;
	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;
	private String layer; // USA States
	private String server; // ="http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities";
	private String zAttribute;  // z coordinate
	private double scale = 1;	// heightParameter = zAttribute/scale
	private String typeName;	// Feature Type Name
   
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	//public WFS_KML_gw_Servlet() {
	//	super();
	//}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/xml");
		kmlout = response.getWriter();

		Map map = request.getParameterMap();

		if(map.containsKey("bbox")) {
			String[] bboxParam = (String[])map.get("bbox");  //else {index.jsp}
			String[] bboxParams = bboxParam[0].split(",");
			xMin = Double.valueOf( bboxParams[0] ).doubleValue();
			xMax = Double.valueOf( bboxParams[2] ).doubleValue();
			yMin = Double.valueOf( bboxParams[1] ).doubleValue();
			yMax = Double.valueOf( bboxParams[3] ).doubleValue();
		} else {
			response.sendRedirect("index.jsp"); 
		}
		
		if(map.containsKey("layer")) layer = ((String[])map.get("layer"))[0]; //TODO layer=Typename
		if(map.containsKey("server")) server = ((String[])map.get("server"))[0];
		if(map.containsKey("zAttribute")) zAttribute = ((String[])map.get("zAttribute"))[0];
		if(map.containsKey("scale")) scale= Double.valueOf( ((String[])map.get("scale"))[0] ).doubleValue();
		
		try {
			supressInfo();
			WFSClient wfs = new WFSClient();
			wfs.getCapabilities( server ); 								// Connection
			// TODO No hay que elegir el layer, solo el Bbox y aparecen todos los layer con datos en ese Bbox.
			// Quiz� sea bueno que no se recuperen los datos, solo el nombre del layer y que los datos se 
			// recuperen cuando se seleccione la carpeta que representa ese layer en GEarth. 
			// Si no se especifica layer se recuperan todos. Si se especifica, s�lo ese layer.
			// ACT Un solo layer para que se pueda elegir el height attribute de ese layer
			typeName = layer; 
			FeatureType schema = wfs.describeFeatureType( typeName ); 	// Feature
			
			// Query   //TODO El CRS de KML no es s�lo 4326.Hay que especificar m�s.
			CoordinateReferenceSystem kmlCRS = CRS.decode("EPSG:4326"); 
			ReferencedEnvelope bbox = new ReferencedEnvelope( xMin, xMax, yMin, yMax, kmlCRS );			
			
			String geomName = schema.getDefaultGeometry().getLocalName();
			CoordinateReferenceSystem geomCRS = schema.getDefaultGeometry().getCoordinateSystem(); 
			
			CreateQuery query;
			if (!kmlCRS.equals(geomCRS)) {
				ReferencedEnvelope geomBbox = bbox.transform( geomCRS, true, 10 ); // Sample 10 points around the envelope
				query = new CreateQuery(geomName, geomBbox);
			} else {
				query = new CreateQuery(geomName, bbox);
			}
			
			Query featuresIntersectsBbox = query.queryIntersects(typeName);
			
			
			//De momento s�lo devuelve una coleccion de features de un �nico FeatureType
			//habr�a que pasarle como argumento featureType[] -> NO, UN UNICO FEATURE PARA ELEGIR EL HEIGHT ATTRIBUTE
			//(�y query[]? �o en main?)			
			FeatureCollection featureCollection = wfs.getFeature( typeName , featuresIntersectsBbox );
			//Info.getFeature(schema);
			
			
//			FileWriter fw = new FileWriter(kmlFileName);
//			BufferedWriter bw = new BufferedWriter(fw);
//			kmlout = new PrintWriter (bw);

            createFile( featureCollection, bbox, geomCRS );
            
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
	


		/**
		 * Output KML file
		 * 
		 * @param featureCollection	Features to be translated into a KML format
		 * @param bbox				Space region that defines the KML viewer focus. 
		 * 							All the features are inside of this Bounding Box
		 */
		public void createFile(FeatureCollection featureCollection, Envelope bbox, CoordinateReferenceSystem geomCRS){  
			try{				
				kmlout.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				kmlout.write("<kml xmlns=\"http://earth.google.com/kml/2.2\">\n\n");
				kmlout.write("<Document>\n");
				kmlout.write("<name>"+typeName+"</name>\n");
				
				kmlout.write("\n<Folder>\n");
				kmlout.write("<name>"+typeName+"</name>\n"); 
				kmlout.write("<open>1</open>\n");
				kmlout.write("<description>test file</description>\n");
			
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
			kmlout.write("		<altitudeMode>relativeToGround</altitudeMode>\n");	
			kmlout.write("	</LatLonAltBox>\n"); 
			kmlout.write("	<Lod>\n");
			kmlout.write("		<minLodPixels>0</minLodPixels>\n");		//TODO Revisar
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
//		          Returns:
//		        	  2 for 2d (default)
//		        	  4 for 3d - one of the oordinates has a non-NaN z value
//		        	  (3 is for x,y,m but thats not supported yet) 
				
//				findBestGeometryType(Geometry geom)
//		          Determine the best ShapeType for a given Geometry.
				
				Geometry geomGeometry = feature.getDefaultGeometry().getGeometryN(g);
				
				MathTransform transform = CRS.findMathTransform(geomCRS, kmlCRS);
				Geometry kmlGeometry = JTS.transform( geomGeometry, transform);
					
				Coordinate coord[] = kmlGeometry.getCoordinates();
				
				if (zAttribute != null) { //.length() != 0 ) {	// If the user has selected the height attribute
					FeatureType featureType = feature.getFeatureType();
					
					int attrPos = featureType.find(zAttribute);	
					if (attrPos == -1) {			// If the attribute doesn't exist
						zCoord = 0;
					} else {						// If the attribute exist
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
		        	} // TODO sustituir NaN en coord z por 0
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
	



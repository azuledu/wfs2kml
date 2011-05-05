package es.uva.idelab;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.geotools.data.oracle.OracleNGDataStoreFactory;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
//import org.geotools.data.FeatureSource;
//import org.geotools.data.Query;
//import org.geotools.data.CachingFeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.referencing.CRS;

//import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.geometry.BoundingBox;
import org.opengis.filter.Filter;

/**
 * Servlet implementation.
 * 
 */
public class WFS2KMLServlet extends HttpServlet {

	static Logger logger = Logger.getLogger("WFS2KMLServlet.class");

	private double xMin = -180;
	private double xMax = 180;
	private double yMin = -90;
	private double yMax = 90;

	private String dataSource; //WFS, DATABASE
	private String server; // http://geoserver.idelab.uva.es/geoserver/wfs?service=WFS&request=GetCapabilities
	private double tolerance = 0;
	private String zAttribute; // z coordinate (Height Parameter)
	private double scale = 1; // height = zAttribute/scale
	private String layer; // Feature Type Name
	private String kml_file_action; 

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		logger.info("********************** ToKML ***************************");
		
		Map<String, Object> ConnectionParameters = request.getParameterMap();
		//setParameters(request);
		
		try {
			// DataStore
			//Map<String, String> ConnectionParameters = new HashMap<String, String>();
//			if (dataSource.equalsIgnoreCase("WFS")) {
//				ConnectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", server);
//			} else if (dataSource.equalsIgnoreCase("DATABASE")) {
			/*	ConnectionParameters.put("OracleNGDataStoreFactory:DBTYPE ", "oracle");
				ConnectionParameters.put("OracleNGDataStoreFactory:HOST", "chddb.idelab.uva.es");
				ConnectionParameters.put("OracleNGDataStoreFactory:PORT", "1521");
				ConnectionParameters.put("OracleNGDataStoreFactory:SCHEMA ", "public");
				ConnectionParameters.put("OracleNGDataStoreFactory:DATABASE ", "gisduero");				
				ConnectionParameters.put("OracleNGDataStoreFactory:USER", "gisduero_09");
				ConnectionParameters.put("OracleNGDataStoreFactory:PASSWD", "gisduero_09");*/
//			}
			DataStore dataStore = DataStoreFinder.getDataStore(ConnectionParameters);
			if (dataStore == null) {
				if (logger.isDebugEnabled()) logger.debug("Could not connect - check parameters");
			}
			
			// SimpleFeatureType and SimpleFeatureSource
			SimpleFeatureType schema = dataStore.getSchema(layer);
			SimpleFeatureSource featureSource = dataStore.getFeatureSource(layer);
	        //CachingFeatureSource cache = new CachingFeatureSource(featureSource);

			// Query
			Query query = new Query(schema.getTypeName(), Filter.INCLUDE); 
			// TODO rehacer la query para bbox,typename y atributo. Mirar "Query"
			SimpleFeatureCollection collection = featureSource.getFeatures(query);
			BoundingBox bounds = featureSource.getBounds(query);
			if (bounds == null) {
				bounds = collection.getBounds();
				if (logger.isDebugEnabled()) {
					logger.debug("Feature (Schema):");
					logger.debug("Schema TypeName:" + schema.getTypeName());
					logger.debug("Schema Attributes:" + schema.getAttributeCount());
					logger.debug("Attributes:");
					List<AttributeType> attributes = schema.getTypes();
					for (int i = 0; i < schema.getAttributeCount(); i++)
						logger.debug(i + " " + attributes.get(i));
				}
			}
			if (logger.isDebugEnabled()) logger.debug("The features are contained within " + bounds);
/*			CoordinateReferenceSystem kmlCRS = CRS.decode("EPSG:900913");
			ReferencedEnvelope bbox = new ReferencedEnvelope(xMin, xMax, yMin, yMax, kmlCRS);

			String geomName = schema.getGeometryDescriptor().getLocalName();
			CoordinateReferenceSystem geomCRS = schema.getCoordinateReferenceSystem();

			CreateQuery query;
			if (!kmlCRS.equals(geomCRS)) {
				// Sample 10 points around the envelope
				ReferencedEnvelope geomBbox = bbox.transform(geomCRS, true, 10);
				query = new CreateQuery(geomName, geomBbox);
			} else {
				query = new CreateQuery(geomName, bbox);
			}

			Query featuresIntersectsBbox = query.queryIntersects(typeName);
*/
			// De momento solo devuelve una coleccion de features de un unico FeatureType
			// habria que pasarle como argumento featureType[] -> NO, UN UNICO
			// FEATURE PARA ELEGIR EL HEIGHT ATTRIBUTE (y query[]? no en main?)
			// FeatureCollection featureCollection = wfs.getFeature( typeName , featuresIntersectsBbox );
			
			//FeatureSource source = dataStore.getFeatureSource(typeName);
			//SimpleFeatureCollection features = source.getFeatures(featuresIntersectsBbox);
			


			String kmlString = KmlProducer.generateKml(collection, zAttribute);
			
			if (("Download KML file".equalsIgnoreCase(this.kml_file_action)) || ("Descargar fichero KML".equalsIgnoreCase(this.kml_file_action))) {
				KmlProducer.downloadKml(kmlString);
			} else {   // if (("Preview KML file".equals(this.kml_file_action))) || ("Previsualizar KML".equals(this.kml_file_action))) 
				KmlProducer.previewKml(response, kmlString);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setParameters(HttpServletRequest request) {
		// Required parameters
		// if((!("".equals(connectionParameters.get("server")))))
		// if(!("".equals(connectionParameters.get("layer")))) {
		dataSource = request.getParameter("dataSource");
		server = request.getParameter("server");
		layer = request.getParameter("layer");
		// } else {
		// response.sendRedirect("index.jsp");
		// }
		if (logger.isDebugEnabled()) logger.debug("server=" + server + ", layer=" + layer);

		// Optional parameters
		zAttribute = request.getParameter("zAttribute"); // TODO toUpper?
		scale = Double.parseDouble(request.getParameter("scale").toString());
		if (logger.isDebugEnabled()) logger.debug("zAttribute=" + zAttribute + ", scale=" + scale);

		String bboxParam = request.getParameter("bbox"); 
		String[] bboxParams = bboxParam.split(",");
		xMin = Double.valueOf(bboxParams[0]).doubleValue();
		xMax = Double.valueOf(bboxParams[2]).doubleValue();
		yMin = Double.valueOf(bboxParams[1]).doubleValue();
		yMax = Double.valueOf(bboxParams[3]).doubleValue();
		if (logger.isDebugEnabled())
			logger.debug("BoundingBox: xMin=" + xMin + ", xMax=" + xMax	+ ", yMin=" + yMin + ", yMax=" + yMax);

		tolerance = Double.parseDouble(request.getParameter("tolerance").toString());
		kml_file_action = request.getParameter("kml_file_action");
		if (logger.isDebugEnabled()) logger.debug("kml_file_action=" + kml_file_action );
	}
}

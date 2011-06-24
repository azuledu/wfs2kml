package es.uva.idelab;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.geotools.data.crs.ReprojectFeatureResults;
import org.geotools.data.oracle.OracleNGDataStoreFactory;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
//import org.geotools.data.FeatureSource;
//import org.geotools.data.Query;
//import org.geotools.data.CachingFeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.Encoder;
//import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.referencing.CRS;

//import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Servlet implementation.
 * 
 */
public class WFS2KMLServlet extends HttpServlet {

	static Logger logger = Logger.getLogger("WFS2KMLServlet.class");
    
	private static final CoordinateReferenceSystem WGS84;
    
    static {
        try {
            WGS84 = CRS.decode("EPSG:4326");
        } catch(Exception e) {
            throw new RuntimeException("Cannot decode EPSG:4326, the CRS subsystem must be badly broken...");
        }
    }
    
    private static FilterFactory filterFactory = (FilterFactory) CommonFactoryFinder.getFilterFactory(null);
    
	private double xMin = -180;
	private double xMax = 180;
	private double yMin = -90;
	private double yMax = 90;

	private String dataSource="WFS"; //WFS, DATABASE
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
		
		//Map<String, Object> ConnectionParameters = request.getParameterMap();
		setParameters(request);
		
		try {
			// DataStore
			Map<String, String> ConnectionParameters = new HashMap<String, String>();
			if (dataSource.equalsIgnoreCase("WFS")) {
				ConnectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", server);
			} else if (dataSource.equalsIgnoreCase("DATABASE")) {
				ConnectionParameters.put("OracleNGDataStoreFactory:DBTYPE ", "oracle");
				ConnectionParameters.put("OracleNGDataStoreFactory:HOST", "chddb.idelab.uva.es");
				ConnectionParameters.put("OracleNGDataStoreFactory:PORT", "1521");
				ConnectionParameters.put("OracleNGDataStoreFactory:SCHEMA ", "public");
				ConnectionParameters.put("OracleNGDataStoreFactory:DATABASE ", "gisduero");				
				ConnectionParameters.put("OracleNGDataStoreFactory:USER", "gisduero_09");
				ConnectionParameters.put("OracleNGDataStoreFactory:PASSWD", "gisduero_09");
			}
			DataStore dataStore = DataStoreFinder.getDataStore(ConnectionParameters);
			if (dataStore == null) {
				if (logger.isDebugEnabled()) logger.debug("Could not connect - check parameters");
			}
			
			// SimpleFeatureType and SimpleFeatureSource
			SimpleFeatureType schema = dataStore.getSchema(layer);
			SimpleFeatureSource featureSource = dataStore.getFeatureSource(layer);
	        //CachingFeatureSource cache = new CachingFeatureSource(featureSource);

			// Query
			ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(xMin, xMax, yMin, yMax, WGS84);
	        Filter filter = createBBoxFilter(schema, referencedEnvelope);
			Query query = new Query(schema.getTypeName());
			query.setFilter(filter);

			SimpleFeatureCollection collection = featureSource.getFeatures(query); 

	        // make sure we output in 4326 since that's what KML mandates
			CoordinateReferenceSystem sourceCrs = schema.getCoordinateReferenceSystem();
			if (sourceCrs != null && !CRS.equalsIgnoreMetadata(WGS84, sourceCrs)) {
	        	collection = new ReprojectFeatureResults(featureSource.getFeatures(query), WGS84);
	        }
	       	        
			BoundingBox bounds = featureSource.getBounds(query);
			if (logger.isDebugEnabled()) logger.debug("The features are contained within " + bounds);

			if (logger.isDebugEnabled()) {
				logger.debug("Feature (Schema):");
				logger.debug("Schema TypeName:" + schema.getTypeName());
				logger.debug("Schema Attributes:" + schema.getAttributeCount());
				logger.debug("Attributes:");
				List<AttributeType> attributes = schema.getTypes();
				for (int i = 0; i < schema.getAttributeCount(); i++)
					logger.debug(i + " " + attributes.get(i));

			}
			//TODO para esta parte, revisar geoserver-trunk/src/wms/src/main/java/org/geoserver/kml/KMLUtils.java/loadFeatureCollection()
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
			


			collection = KmlProducer.Simplify(collection);
			
			
			// Encode to XML
			Encoder encoder = new Encoder(new KMLConfiguration());
			encoder.setIndenting(true);

			
			
			if (("Download KML file".equalsIgnoreCase(this.kml_file_action)) || ("Descargar fichero KML".equalsIgnoreCase(this.kml_file_action))) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				encoder.encode(collection, KML.kml, out);

				String kmlString = new String(out.toByteArray());

				try {
					response.setContentType("application/vnd.google-earth.kml+xml;charset=UTF-8");
					response.setHeader("Content-Disposition", "attachment;filename=tematico.kml");
				} catch (Exception e) {
					logger.error("Error configuring response" + e.getMessage(), e);
				}
				
				PrintWriter kmlout = response.getWriter();
				kmlout.write(kmlString);
				kmlout.close();

			} else { // if (("Preview KML file".equals(this.kml_file_action))) ||
						// ("Previsualizar KML".equals(this.kml_file_action)))
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				encoder.encode(collection, KML.kml, out);

				String kmlString = new String(out.toByteArray());

				response.setContentType("application/xml;charset=UTF-8");
				PrintWriter kmlout = response.getWriter();
				kmlout.write(kmlString);
				kmlout.close();

			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setParameters(HttpServletRequest request) {
		// Required parameters
		// if((!("".equals(connectionParameters.get("server")))))
		// if(!("".equals(connectionParameters.get("layer")))) {
		//dataSource = request.getParameter("dataSource");
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
	
    private static Filter createBBoxFilter(SimpleFeatureType schema, Envelope bbox) throws IllegalFilterException {
        List filters = new ArrayList();
        for (int j = 0; j < schema.getAttributeCount(); j++) {
            AttributeDescriptor attType = schema.getDescriptor(j);

            if (attType instanceof GeometryDescriptor) {
                Filter gfilter = filterFactory.bbox(attType.getLocalName(),
                        bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox
                                .getMaxY(), null);
                filters.add(gfilter);
            }
        }

        if (filters.size() == 0)
            return Filter.INCLUDE;
        else if (filters.size() == 1)
            return (Filter) filters.get(0);
        else
            return filterFactory.or(filters);
    }
}

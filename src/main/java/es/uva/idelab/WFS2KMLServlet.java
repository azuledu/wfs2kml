package es.uva.idelab;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataStore;
//import org.geotools.data.FeatureSource;
//import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.referencing.CRS;

//import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

/**
 * Servlet implementation.
 * 
 */
public class WFS2KMLServlet extends HttpServlet {

	static Logger logger = Logger.getLogger(WFS2KMLServlet.class);

	private double xMin = -180;
	private double xMax = 180;
	private double yMin = -90;
	private double yMax = 90;
	private String layer;
	private String server; // http://geoserver.idelab.uva.es/geoserver/wfs?service=WFS&request=GetCapabilities
	private double tolerance = 0;
	private String zAttribute; // z coordinate (Height Parameter)
	private double scale = 1; // height = zAttribute/scale
	private String typeName; // Feature Type Name
	private String kml_file_action; 

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		logger.info("********************** WFS-KML Gateway ***************************");
		
		Map<String, Object> connectionParameters = request.getParameterMap();
		setParameters(connectionParameters);
		
		try {
			//supressInfo();
			// TODO No hay que elegir el layer, solo el Bbox y aparecen todos los layer con datos en ese Bbox.
			// Quiza sea bueno que no se recuperen los datos, solo el nombre del layer y que los datos se
			// recuperen cuando se seleccione la carpeta que representa ese layer en GEarth.
			// Si no se especifica layer se recuperan todos. Si se especifica, solo ese layer.
			// ACT Un solo layer para que se pueda elegir el height attribute de ese layer
			typeName = layer;

			// DataStore
			Map<String, String> wfsConnectionParameters = new HashMap<String, String>();
			wfsConnectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", server);
			DataStore dataStore = DataStoreFinder.getDataStore(wfsConnectionParameters);
			if (dataStore == null) {
				if (logger.isDebugEnabled())
					logger.debug("Could not connect - check parameters");
			}
			// SimpleFeatureType
			SimpleFeatureType schema = dataStore.getSchema(typeName);

			// Query
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
			
	        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
	        SimpleFeatureCollection features = source.getFeatures();
			
			if (logger.isDebugEnabled()) {
				logger.debug("Feature (Schema):");
				logger.debug("Schema TypeName:" + schema.getTypeName());
				logger.debug("Schema Attributes:" + schema.getAttributeCount());
				logger.debug("Attributes:");
				List<AttributeType> attributes = schema.getTypes();
				for (int i = 0; i < schema.getAttributeCount(); i++)
					logger.debug(i + " " + attributes.get(i));
			}

			String kmlString = KmlProducer.generateKml(features);
			
			if (("Download KML file".equals(this.kml_file_action)) || ("Descargar fichero KML".equals(this.kml_file_action))) {
                //KMLProducer_bak kml = new KMLProducer_bak (kmlout, typeName, tolerance, zAttribute, scale);
                //kml.createFile( kmlout, features, bbox, geomCRS );
				KmlProducer.downloadKml(kmlString);
			} else {   // if (("Preview KML file".equals(this.kml_file_action))) || ("Previsualizar KML".equals(this.kml_file_action))) 
				KmlProducer.previewKml(response, kmlString);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setParameters(Map<String, Object> connectionParameters) {
		// Required parameters
		// if((!("".equals(connectionParameters.get("server")))))
		// if(!("".equals(connectionParameters.get("layer")))) {
		server = ((String[]) connectionParameters.get("server"))[0];
		layer = ((String[]) connectionParameters.get("layer"))[0]; // TODO layer=Typename
		// } else {
		// response.sendRedirect("index.jsp");
		// }
		if (logger.isDebugEnabled())
			logger.debug("server=" + server + ", layer=" + layer);

		// Optional parameters
		// if(!("".equals((String[])connectionParameters.get("zAttribute"))[0]))
		zAttribute = ((String[]) connectionParameters.get("zAttribute"))[0]; // TODO toUpper?
		// if(!("".equals(connectionParameters.get("scale"))))
		scale = Double.valueOf(((String[]) connectionParameters.get("scale"))[0]).doubleValue();
		if (logger.isDebugEnabled())
			logger.debug("zAttribute=" + zAttribute + ", scale=" + scale);

		// if(("".equals(connectionParameters.get("bbox")))) {
		String[] bboxParam = (String[]) connectionParameters.get("bbox"); // else {index.jsp}
		String[] bboxParams = bboxParam[0].split(",");
		xMin = Double.valueOf(bboxParams[0]).doubleValue();
		xMax = Double.valueOf(bboxParams[2]).doubleValue();
		yMin = Double.valueOf(bboxParams[1]).doubleValue();
		yMax = Double.valueOf(bboxParams[3]).doubleValue();
		if (logger.isDebugEnabled())
			logger.debug("BoundingBox: xMin=" + xMin + ", xMax=" + xMax
					+ ", yMin=" + yMin + ", yMax=" + yMax);

		tolerance = Double.valueOf(((String[]) connectionParameters.get("tolerance"))[0]).doubleValue();
		kml_file_action = ((String[]) connectionParameters.get("kml_file_action"))[0];
		if (logger.isDebugEnabled())
			logger.debug("kml_file_action=" + kml_file_action );
		// }
	}

	/**
	 * 
	 */
/*	public static void supressInfo() {
		Logger.getLogger("org.geotools.gml").setLevel(Level.SEVERE);
		Logger.getLogger("net.refractions.xml").setLevel(Level.SEVERE);
	}*/

}

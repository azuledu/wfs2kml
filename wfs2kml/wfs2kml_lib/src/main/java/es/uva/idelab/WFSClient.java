package es.uva.idelab;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;

import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Web Feature Service client used to connect with a geographic server and retrieve the geographic features.
 * Names, parameters and responses try to emulate those defined in the WFS OGC standard definition.
 * 
 * @author Eduardo Riesco
 *
 */
public class WFSClient {

	DataStore data; 
	
	/**
	 * Retrieve the capabilities of a geographic server and the features stored in it
	 * 
	 * @param server	Geographic Server URL
	 * @return			Return the names of the feature types stored in the server
	 * @throws Exception
	 */
	public String[] getCapabilities (String server) throws Exception {
		// Connection parameters
		Map connectionParameters = new HashMap();
		connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", server );	

		// Connection
		data = DataStoreFinder.getDataStore( connectionParameters );
		String typeNames[] = data.getTypeNames();
		
		return (typeNames);
	}

	/**
	 * Return the description of a feature type
	 * 
	 * @param typeName		Feature type name  
	 * @return				Feature type schema
	 * @throws Exception
	 */
	public SimpleFeatureType describeFeatureType ( String typeName) throws Exception {	
		SimpleFeatureType schema = data.getSchema( typeName );
		
		return( schema );		
	}

	//De momento solo devuelve una coleccion de features de un unico FeatureType
	//habr�a que pasarle como argumento featureType[] (�y query[]? �o en main?)
	/**
	 * @param typeName		Feature type name 
	 * @param query			Conditions accomplished by the features returned
	 * @return				Feature collection
	 * @throws Exception
	 */
	public FeatureCollection getFeature (String typeName, Query query) throws Exception {
		FeatureSource source = data.getFeatureSource( typeName );
		FeatureCollection features = source.getFeatures( query );
		
		return(features);
	}
}

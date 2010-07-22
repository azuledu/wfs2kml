package es.uva.idelab;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;

//import com.vividsolutions.jts.geom.Envelope;

/**
 * Creates a query with the input parameters.
 * 
 * @author Eduardo Riesco
 *
 */
public class CreateQuery {
	
	String geomName;	// Primary Geometry Local Name
	BoundingBox bbox;
	
	public CreateQuery ( String geomName, BoundingBox bbox ) {
		this.geomName = geomName;
		this.bbox = bbox;
	}
	
	/**
	 * Create a Query object to find the features who intersects the bbox for a give Feature Type (typeName)
	 * 
	 * @param typeName Feature Type in which the query will be created
	 * @return 
	 */
	public Query queryIntersects (String typeName) {	
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
		Filter filter = ff.bbox( ff.property( geomName ), bbox );
		Query query = new DefaultQuery( typeName, filter);
		return(query);
	}
    

}

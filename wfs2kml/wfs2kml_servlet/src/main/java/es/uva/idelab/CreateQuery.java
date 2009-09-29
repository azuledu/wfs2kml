package es.uva.pfc.eduardoRiesco;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.JTS;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Creates a query with the input parameters.
 * 
 * @author Eduardo Riesco
 *
 */
public class CreateQuery {
	
	String geomName;	// Primary Geometry Local Name
	Envelope bbox;
	
	public CreateQuery ( String geomName, Envelope bbox ) {
		this.geomName = geomName;
		this.bbox = bbox;
	}
	
	/**
	 * Create a Query object to find the features who intersects the bbox for a give Feature Type (typeName)
	 * 
	 * @param typeName Feature Type in which the query will be created
	 * @return 
	 */
	public Query queryIntersects (String typeName) {	//TODO Usar opengis en vez de JTS
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
		Object polygon = JTS.toGeometry( bbox );
	    Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );
		
		Query query = new DefaultQuery( typeName, filter); // new String[]{ geomName } );
		
		return(query);
	}
    

}

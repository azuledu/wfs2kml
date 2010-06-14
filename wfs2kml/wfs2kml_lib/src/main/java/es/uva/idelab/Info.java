package es.uva.idelab;

import java.util.Iterator;
import java.util.List;

import org.geotools.feature.FeatureCollection;

import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * For test purposes only. 
 * Methods from this class can be used to see the feature types and their schemas
 * 
 * @author Eduardo Riesco
 *
 */
public class Info {

	/**
	 * Print out Feature Types
	 * 
	 * @param typeNames
	 */
	public static void getFeatureTypes(String[] typeNames) {
		System.out.println( "FeatureTypes (TypeNames):\n" );		
		for(int i=0;i<typeNames.length;i++)
			System.out.println( "TypeNames:"+typeNames[i] );
	}
	
	/**
	 * Print out the first 10 features (non geometric attributes)
	 * 
	 * @param featureCollection
	 */
	public static void getFeatures(FeatureCollection featureCollection) {
	    System.out.println("\nFirst 10 features (non geometric attribute):");
	    Iterator iterator = featureCollection.iterator();
	    try {
	        for( int count=0; iterator.hasNext(); count++) {
	        	SimpleFeature feature = (SimpleFeature) iterator.next();
	            System.out.print("ID:"+feature.getID() + "\t");
	
	            for (int i = 0; i < feature.getAttributeCount(); i++) {
	                Object attribute = feature.getAttribute(i);
	
	                if (!(attribute instanceof Geometry)) {
	                    System.out.print(attribute + "\t");
	                }
	            }
	            System.out.println();
	            if( count == 10) break; // only 10
	        }
	    }
	    finally {
	    	featureCollection.close( iterator );
	    }
	}
	
	/**
	 * Print out Feature
	 * 
	 * @param schemaorg.opengis.feature.type 
	 */
	public static void getFeature(SimpleFeatureType schema) {
		System.out.println( "\nFeature (Schema):\n" );		
		System.out.println( "Schema TypeName:"+schema.getTypeName() );
		System.out.println( "Schema Attributes:"+schema.getAttributeCount()+"\n" );
		System.out.println( "Attributes:\n" );
		List attributes = schema.getTypes();
		for(int i=0;attributes.listIterator().hasNext();i++)
			System.out.println( attributes.get(i) );
	}
	
    /**
     * Print out non geometry attributes
     * 
     * @param schema
     */
    public static void getAttributes(SimpleFeatureType schema) {
        System.out.println("\nNon geometry attributes:");
    	for (int i = 0; i < schema.getAttributeCount(); i++) {
	        AttributeType attributeType = schema.getType(i);
	
        if ( !(attributeType instanceof GeometryType)) {
            System.out.print(attributeType.getName() + "\n");
        }
	    }
    }
    
    /**
     * Print out the geometry attributes
     * 
     * @param schema
     */
    public static void getGeometryAttributes(SimpleFeatureType schema) {
        System.out.println("\nGeometry attributes:");    
    	for (int i = 0; i < schema.getAttributeCount(); i++) {
	        AttributeType at = schema.getType(i);
	        if ( at instanceof GeometryType) {
	        	System.out.print(at.getName() + "\t");
	        }
    	}
    }
    
	/**
	 * Print out the coordinates
	 * 
	 * @param featureCollection
	 */
	public static void getCoordinates(FeatureCollection featureCollection) {			
		Iterator iterator = featureCollection.iterator();
	    try {
	        for( int count=0; iterator.hasNext(); count++) {
	        	SimpleFeature feature = (SimpleFeature) iterator.next();
	            System.out.print("\n\n\n" + feature.getID() + "\t");
	            //System.out.println(feature.getPrimaryGeometry() + "\t");
				Geometry defaultGeometry=(Geometry)feature.getDefaultGeometry();
	            System.out.println(defaultGeometry.getNumGeometries() + "\n");
	            for(int i=0; i<defaultGeometry.getNumGeometries(); i++){
	            	Coordinate coord[] = defaultGeometry.getGeometryN(i).getCoordinates();
	            	for(int j=0;j<coord.length;j++){
	            		System.out.print( coord[j].x + "," + coord[j].y + " " );
	            	}
	            	System.out.println("\n");
	            }
	            if( count == 4) break; // only 4 features
	        }
	    }
	    finally {
	    	featureCollection.close( iterator );
	    }
    }
          
	
	
	
}

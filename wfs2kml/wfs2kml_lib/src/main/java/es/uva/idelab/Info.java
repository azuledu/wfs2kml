package es.uva.idelab;

import java.util.Iterator;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.AttributeType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.FeatureCollection;

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
	            Feature feature = (Feature) iterator.next();
	            System.out.print("ID:"+feature.getID() + "\t");
	
	            for (int i = 0; i < feature.getNumberOfAttributes(); i++) {
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
	 * @param schema
	 */
	public static void getFeature(FeatureType schema) {
		System.out.println( "\nFeature (Schema):\n" );		
		System.out.println( "Schema TypeName:"+schema.getTypeName() );
		System.out.println( "Schema Attributes:"+schema.getAttributeCount()+"\n" );
		System.out.println( "Attributes:\n" );
		AttributeType attributes[] = schema.getAttributeTypes();
		for(int i=0;i<attributes.length;i++)
			System.out.println( attributes[i] );
	}
	
    /**
     * Print out non geometry attributes
     * 
     * @param schema
     */
    public static void getAttributes(FeatureType schema) {
        System.out.println("\nNon geometry attributes:");
    	for (int i = 0; i < schema.getAttributeCount(); i++) {
	        AttributeType attributeType = schema.getAttributeType(i);
	
        if ( !(attributeType instanceof GeometryAttributeType)) {
            System.out.print(attributeType.getLocalName() + "\n");
        }
	    }
    }
    
    /**
     * Print out the geometry attributes
     * 
     * @param schema
     */
    public static void getGeometryAttributes(FeatureType schema) {
        System.out.println("\nGeometry attributes:");    
    	for (int i = 0; i < schema.getAttributeCount(); i++) {
	        AttributeType at = schema.getAttributeType(i);
	        if ( at instanceof GeometryAttributeType) {
	        	System.out.print(at.getLocalName() + "\t");
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
	            Feature feature = (Feature) iterator.next();
	            System.out.print("\n\n\n" + feature.getID() + "\t");
	            //System.out.println(feature.getPrimaryGeometry() + "\t");
	            System.out.println(feature.getDefaultGeometry().getNumGeometries() + "\n");
	            for(int i=0; i<feature.getDefaultGeometry().getNumGeometries(); i++){
	            	Coordinate coord[] = feature.getDefaultGeometry().getGeometryN(i).getCoordinates();
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

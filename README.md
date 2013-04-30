**This _WFS-KML Gateway_ project was my first contact with GIS, GeoTools, OGC standards, etc. It is just a proof of concept now totally remplaced by _FeaturePub_ project.**


WFS-KML Gateway
===============

The _WFS-KML Gateway_ project aims to develop a gateway to transform WFS requests in KML responses.
  
  A request, done to any OWS (OGC Web Service) using WFS (Web Feature Service, a standar OGC protocol) will recive a response in KML format. KML is a XML format used by Google to represent geographic information in Google Earth and Google Maps.
  
  To visualize this geographic information represented in a KML file, it is necesary a KML viewer. Google Maps is a web based 2D viewer and Google Earth is a Desktop application 3D viewer.
  
  The WFS-KML gateway is specially oriented to work with 3D representation of geographic features. We can represent buildings or any other geographic feature with a height parameter.
  
  It is also possible to use any other parameter as a height parameter. For example, Earth countries can be represented with a height proportional to their population.

  As we can see, WFS-KML Gateway makes possible to translate between the OGC standart protocols and KML Google protocol, adding value to this translation allowing to configurate the way we visualize geographic features and their parameters.


Features
--------

  * Transform WFS requests in KML responses.
  
  * Allow to select the Bounding Box.
  
  * Automatic conversion between diferent Coordinate Reference Systems (CRS). KML format use the WSG84 CRS, the standart longitude-latitude system. The bounding box must be given in this format. But if data are stored in a diferent format, automatic conversion will be performed.
  
  * 3D representation of geographic features.
  
  * Allow to configurate how geographic features are visualized:
  
    * Any feature parameter can be used as a height parameter.
    
    * Height parameter can be scaled.


Usage
-----

  The _WFS-KML Gateway Servlet_ can be deployed into any Servlet container (ex. Apache Tomcat) to offer the WFS-KML Gateway service.

  It can be used from a **web browser** generating a _.kml_ file which can be opened with Google Earth.

  It can be used directly from **Google Earth** too.
  
  In the Add menu: Add -> Network Link. In the link box, use the address:

    http://MyServer/wfs2kml?
    SERVER=GeographicServer&
    LAYER=namespace:layer&
    BBOX=xMin,yMin,xMax,yMax&
    ZATTRIBUTE=attribute&
    SCALE=scale

  _MyServer_ - Servlet container where the _.war_ file is deployed.
  
  _GeographicServer_ - Public WFS Geographic server from where we want to get the data.
  
  _namespace:layer_ - Namespace and name of one of the layers offered by the WFS server.
  
  _xMin,yMin,xMax,yMax_ - Bounding Box. 2D rectangle which defines the visible area. Expresed in standar latitude-longitude system.
  
  _zAttribute_ - (Optional) - Layer's attribute used as height attribute. If it is not set, 3D feature's height attribute is used or, in 2D features, it will be shown as 2D picture.
  
  _scale_ - (Optional) - The height will be divided by this number. Default scale is 1.


Glossary
--------

  **OWS** - _OGC Web Service_.
  
  **OGC** - _Open Geospatial Consortium_. International voluntary consensus standards organization. Develop and implements standards for geospatial content and services, GIS data processing and exchange.
  
  **WFS** - _Web Feature Service_. Allow requests for geographical features across the web. It is a OGC specification.
  
  **KML** - _Keyhole Markup Language_. XML format used by Google to represent 3D geographic information.
  
  **Feature** - Entity with a geographic location, typically describable by (for example) points, arcs, or polygons.
  
  **CRS** - _Coordinate Reference System_

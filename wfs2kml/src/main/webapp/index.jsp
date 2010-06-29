<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>WFS to KML Gateway</title>
        <link rel="stylesheet" href="css/wfs2kml.css" type="text/css" />
    </head>
    <body>
      	<div id="header"><a id="logo" href="/wfs2kml">&nbsp;</a></div>
      	<div id="content">
	        <form action="wfs2kml" method="get">
	        <fieldset><legend>Server</legend><br/>
	        Server: <input type="text" name="server" size="100" VALUE="http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities" /><br/><br/>
			Bounding Box: <input type="text" name="bbox" size="25" VALUE="-90,-180,90,180" /><br/>	<br/>
<!--  			xmin <input type="text" name="xmin" size="5" VALUE="-180">
			xmax <input type="text" name="xmax" size="5" VALUE="180">
			ymin <input type="text" name="ymin" size="5" VALUE="-90">
			ymax <input type="text" name="ymax" size="5" VALUE="90"></p>	-->
			Layer: <input type="text" name="layer" size="50" VALUE="topp:states" /><br/><br/>
				
			</fieldset><br/>
			<fieldset><legend>3D Style</legend><br/>
					zAttribute: <input type="text" name="zAttribute" size="20" VALUE="population" /><br/><br/>
					Scale: <input type="text" name="scale" size="10" VALUE="1" /><br/><br/>
			</fieldset><br/>
			<input type="submit" value="Enviar">
			</form>
		</div>	
    </body>
</html>

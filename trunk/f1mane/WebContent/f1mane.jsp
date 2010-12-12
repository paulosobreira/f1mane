<%
    response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
    response.setHeader("Pragma","no-cache"); //HTTP 1.0
    response.setDateHeader ("Expires", -1); //evita o caching no servidor proxy
%>

<html>
<head>
<title>F1-Mane</title>
<META NAME="description"
	CONTENT="Formula 1,FIA,F1,Esportes,Sport,Ferrari,Mercedes,Renault,RedBull">

<style type="text/css">
div {
	cursor: pointer;
	font-family: sans-serif;
}

A:link {text-decoration: none; 
		color: #0084B4;}
A:visited {text-decoration: none;
		color: #0084B4;} 
A:active {text-decoration: none;
		color: #0084B4;}
A:hover {text-decoration: underline overline; color: black;}

#link {
	border-style: solid;
	border-width: 1px;
	border-color: #B8CFE5;
	text-align: left;
	padding: 3px;
	padding-left: 10px;
	padding-right: 10px;
	cursor: pointer;
	font-family: sans-serif;
	color: #0084B4;
	text-decoration: none;
}


img {
	border-style: none;
	border-width: 0px;
	border-color: #5C5E5D;
	padding: 10px;
	padding-left: 0px;
	padding-right: 0px;
}

#title {
	border-style: none;
	text-align: left;
	font-family: Arial, sans-serif;
	font-size: 24px;
	font-weight: bold;
	line-height: 24px;
	height: 100px;
}

#adds {
	font-family: Arial, sans-serif;
	font-size: 24px;
	font-weight: bold;
	position: relative;
	float :left;
}

#main{
	padding: 3px;
	padding-left: 20px;
	padding-right: 20px;
	border-style: none;
	position: relative;
	float: right;	
}

#shots{
	border-style: solid;
	border-width: 1px;
	border-color: #B8CFE5;
	text-align: center;
	padding: 3px;
	padding-left: 20px;
	padding-right: 20px;
	cursor: pointer;
	font-family: sans-serif;
}

#shotsPromo{
	border-style: solid;
	border-width: 1px;
	border-color: #B8CFE5;
	text-align: center;
	padding: 15px;
}

#description {
	color: #666666;
	font-size: 13px;
	font-style: italic;
}
</style>
<script type="text/javascript" src="highslide/highslide-full.js"></script>
<link rel="stylesheet" type="text/css" href="highslide/highslide.css" />

<!--
	2) Optionally override the settings defined at the top
	of the highslide.js file. The parameter hs.graphicsDir is important!
-->

<script type="text/javascript">
	
	hs.graphicsDir = 'highslide/graphics/';
	hs.align = 'center';
	hs.transitions = ['expand', 'crossfade'];
	hs.outlineType = 'rounded-white';
	hs.fadeInOut = true;
	hs.dimmingOpacity = 0.75;

	// define the restraining box
	hs.useBox = true;
	hs.width = 800;
	hs.height = 600;

	// Add the controlbar
	hs.addSlideshow({
		//slideshowGroup: 'group1',
		interval: 5000,
		repeat: false,
		useControls: true,
		fixedControls: 'fit',
		overlayOptions: {
			opacity: 1,
			position: 'bottom center',
			hideOnMouseOut: true
		}
	});


	
</script>

</head>
<body>
<center>
<table >
	<tr> 
	<td>
	<div id="adds">
		<script type="text/javascript"><!--
		google_ad_client = "pub-1471236111248665";
		/* 120x600, criado 14/06/10 */
		google_ad_slot = "5219714006";
		google_ad_width = 120;
		google_ad_height = 600;
		//-->
		</script>
		<script type="text/javascript"
		src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
		</script>
	</div>
	</td>
	<td>
	<div id="main">
		<div id="title">	
			<span >F1-Mane
				<h1 id="description">MANager & Engineer</h1>
			</span>
			<a id="link" style="position: absolute; left: 150px; top: 10px;font-size: 16px;"
				href="http://twitter.com/f1mane" 
				target="_BLANK">Twitter</a>
			<a id="link" style="position: absolute; left: 250px; top: 10px;font-size: 16px;"
				href="http://sowbreira.appspot.com/" 
				target="_BLANK">Site Autor</a>
			<a id="link" style="position: absolute; left: 370px; top: 10px;font-size: 16px;"
				href="mailto:sowbreira@gmail.com" 
				target="_BLANK">Reportar Bugs</a>		
			<a id="link" style="position: absolute; left: 220px; top: 50px;font-size: 16px;"
				href="http://www.java.com/" 
				target="_BLANK">Instale o Java</a>
			<a id="link" style="position: absolute; left: 370px; top: 50px;font-size: 16px;"
				href="F1Mane.html" 
				target="_BLANK">Versao 1 Jogador</a>	
			<br>					
		</div>

		<div id="shots" class="highslide-gallery">
			<a href="fm1.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="fm1.jpg" width="130" height="120" /></a>
			<a href="fm2.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="fm2.jpg" width="130" height="120" /></a>
			<a href="fm3.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="fm3.jpg" width="130" height="120" /></a><br>
			<a href="fm4.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="fm4.jpg" width="130" height="120" /></a>
			<a href="fm5.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="fm5.jpg" width="130" height="120" /></a>
			<a href="fm6.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="fm6.jpg" width="130" height="120" /></a><br>
		</div>
		<div style="text-align: center;">
		<a id="link" href="f1mane.jnlp" style="text-align: left;">
			Jogar Offline
			<img src="http://java.sun.com/products/jfc/tsc/articles/swing2d/webstart.png" border="0">
		</a> 
		&nbsp;&nbsp;&nbsp;&nbsp;
		<a id="link"  href="webpaddock.jnlp" style="text-align: left;">
			Jogar Online  
			<img src="http://java.sun.com/products/jfc/tsc/articles/swing2d/webstart.png" border="0">
		</a>
		</div>
		<br>
		<div id="shotsPromo" class="highslide-gallery">
			Veja Tambem 
			<a id="link" 
				href="http://www.f1mane.com/mesa11" style="text-align: center;"
				target="_BLANK">  Mesa-11 </a><br><br>
			<a href="http://www.f1mane.com/mesa11/m11-1.jpg" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="http://www.f1mane.com/mesa11/m11-1.jpg" width="130" height="120" /></a>
			<a href="http://www.f1mane.com/mesa11/m11-2.jpg" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="http://www.f1mane.com/mesa11/m11-2.jpg" width="130" height="120" /></a>
			<a href="http://www.f1mane.com/mesa11/m11-4.jpg" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="http://www.f1mane.com/mesa11/m11-4.jpg" width="130" height="120" /></a>
			<a href="http://www.f1mane.com/mesa11/m11-6.jpg" onclick="if (!(navigator.userAgent.indexOf('Firefox')==-1)) {return hs.expand(this)}"> <img src="http://www.f1mane.com/mesa11/m11-6.jpg" width="130" height="120" /></a>
		</div>	
		
	</div>
	</td>
	</tr>
</table>
</center>
</body>
</html>
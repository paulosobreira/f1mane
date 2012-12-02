<%
    response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
    response.setHeader("Pragma","no-cache"); //HTTP 1.0
    response.setDateHeader ("Expires", -1); //evita o caching no servidor proxy
%>

<html>
<head>
<title>F1-Mane</title>
<META NAME="description"
	CONTENT="Formula 1,FIA,F1,Esportes,Sport,Jogo,Game,Manager,Gerenciamento">

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

#pop{
     display:none;
     position:absolute;
	 top:28%;
	 left:30%;
	 margin-left:-150px;
	 margin-top:-100px;
	 padding-left: 20px;
	 padding-right: 20px;
	 padding:10px;
	 width:550px;
	 height:450px;
	 border:1px solid #B8CFE5;
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
			
			<a id="link" style="position: absolute; left: 160px; top: 10px;font-size: 16px;"
				href="http://sowbra.com.br" style="text-align: right;"
				target="_BLANK">Sowbra Games</a>
			<a id="link" style="position: absolute; left: 320px; top: 10px;font-size: 16px;"
				href="http://sowbreira.appspot.com/" style="text-align: right;"
				target="_BLANK">Sowbreira</a>
			<a id="link" style="position: absolute; left: 450px; top: 10px;font-size: 16px;"
				href="f1mane.jsp">Portugues</a>
			<a id="link" style="position: absolute; left: 160px; top: 50px;font-size: 16px;"
				style="text-align: right;" onclick="document.getElementById('pop').style.display='block';">
				How to Play</a>				
			<a id="link" style="position: absolute; left: 310px; top: 50px;font-size: 16px;"
				href="http://www.java.com/" style="text-align: right;"
				target="_BLANK">Install Java</a>			
			<br>					
		</div>
		<div id="pop" style="background-color: white;">
	    	F1 Mane is a strategy game of F1 racing
	    	<a href="#" style="position:absolute; left:92%" onclick="document.getElementById('pop').style.display='none';">[X]</a>
			<p style="color: #0084B4;">
				Inside the game:	
			</p>
			<UL>
			   <LI >Your pilot is in the blue cell in the table on right side of screen</LI>
			   <LI >You can follow the strategy of the other riders in the table</LI>			   
			   <LI >Use F1, F2, F3 to control the rotation of the engine</LI>
			   <LI >Use F4 to quickly switch between driving aggressive and normal</LI>
			   <LI >Use F5, F6, F7 to control the aggressiveness of the pilot</LI>
			   <LI >Use F8 to turn off the automatic trackside choose </LI>
			   <LI >Use F9 to toggle between the pilots if you have chosen several</LI>
			   <LI >Use F12 to select or cancel pit stop </LI>
			   <LI >Use the mouse scroll wheel for zooming </LI>
			   <LI >Use the arrow keys to choose the path </LI>			   
			</UL>
			<p style="color: #0084B4;">
				Online game:	
			</p>
			<UL>
			   <LI >Players can create and edit teams and drivers</LI>
			   <LI >A game can have up to 24 simultaneous players</LI>
			   <LI >The player can evolve with your driver and team points earned </LI>
			   <LI >You can see ranking of drivers, teams and players</LI>			   
			   <LI >You can create Championships </LI>
			   <LI >26 circuits available to play </LI>
			</UL>				
		</div>		
		

		<div id="shots" class="highslide-gallery">
			<a href="fm1.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="return hs.expand(this)"> <img src="fm1.jpg" width="130" height="120" /></a>
			<a href="fm2.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="return hs.expand(this)"> <img src="fm2.jpg" width="130" height="120" /></a>
			<a href="fm3.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="return hs.expand(this)"> <img src="fm3.jpg" width="130" height="120" /></a>
			<a href="fm4.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="return hs.expand(this)"> <img src="fm4.jpg" width="130" height="120" /></a><br>
			<a href="fm5.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="return hs.expand(this)"> <img src="fm5.jpg" width="130" height="120" /></a>
			<a href="fm6.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="return hs.expand(this)"> <img src="fm6.jpg" width="130" height="120" /></a>
			<a href="fm7.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="return hs.expand(this)"> <img src="fm7.jpg" width="130" height="120" /></a>
			<a href="fm8.jpg" style="padding-left: 10px; padding-right: 10px;" onclick="return hs.expand(this)"> <img src="fm8.jpg" width="130" height="120" /></a><br>
		</div>
		<br>
		<div style="text-align: center;">
				<a id="link"  href="f1maneonline_en.jnlp" style="text-align: left;">
					PLay Online		
					<img src="webstart.png" border="0">
				</a>	
				&nbsp;&nbsp;&nbsp;&nbsp;
				<a id="link" href="f1mane_en.jnlp" style="text-align: left;">
					Play Offline
					<img src="webstart.png" border="0">
				</a>
		</div>
		<br>
		<div id="shotsPromo" class="highslide-gallery">
			<table style="text-align: center;padding: 5px">
			<tr>
			<td colspan="2">See Also</td>
			</tr>
			<tr>
			<td  style="text-align: center;padding: 10px">
				<a id="link" 
					href="../../mesa11" style="text-align: center;"
					target="_BLANK">  Mesa-11 </a><br><br>
				<a href="./../mesa11/m11-1.jpg" style="padding: 5px" onclick="return hs.expand(this)"> <img  src="./../mesa11/m11-1.jpg" width="130" height="120" /></a>
				<a href="./../mesa11/m11-2.jpg" style="padding: 5px" onclick="return hs.expand(this)"> <img  src="./../mesa11/m11-2.jpg" width="130" height="120" /></a>
			</td>
			<td  style="text-align: center;padding: 10px">				
				<a id="link" 
					href="../../topwar" style="text-align: center;"
					target="_BLANK">  Top-War </a><br><br>
				<a href="../../topwar/tw1.jpg" style="padding: 5px" onclick="return hs.expand(this)"> <img  src="../../topwar/tw1.jpg" width="130" height="120" /></a>
				<a href="../../topwar/tw2.jpg" style="padding: 5px" onclick="return hs.expand(this)"> <img  src="../../topwar/tw2.jpg" width="130" height="120" /></a>
			</td></tr>
			</table>
		</div>		
		
	</div>
	</td>
	</tr>
</table>
</center>
</body>
</html>
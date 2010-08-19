<%
    response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
    response.setHeader("Pragma","no-cache"); //HTTP 1.0
    response.setDateHeader ("Expires", -1); //evita o caching no servidor proxy
%>

<html>
<head>
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
	text-align: center;
	padding: 3px;
	padding-left: 20px;
	padding-right: 20px;
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
}

#adds {
	border-style: none;
	text-align: left;
	font-family: Arial, sans-serif;
	font-size: 24px;
	font-weight: bold;
	line-height: 24px;
}

#main{
	border-style: solid;
	border-width: 1px;
	border-color: #B8CFE5;
	text-align: center;
	padding: 3px;
	padding-left: 20px;
	padding-right: 20px;
	border-style: none;
	left: 120px;
	top : 0px;
	position: absolute;
	height: 300px;
	background-image: url("logo.jpg");
	background-repeat: no-repeat;
	
}

#shots{
	text-align: left;
	padding: 0px;
	width: 1000px;
	height: 200px;
}

#description {
	color: #666666;
	font-size: 13px;
	font-style: italic;
}
</style>
</head>
<body>
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

<div id="main">
	<div id="title">	
		<span >F1-Mane
			<h1 id="description">MANager & Engineer</h1>
		</span>
		<a id="link" style="position: absolute; left: 150px; top: 10px;font-size: 16px;"
			href="http://twitter.com/f1mane" style="text-align: right;"
			target="_BLANK">Twitter</a>
		<a id="link" style="position: absolute; left: 250px; top: 10px;font-size: 16px;"
			href="http://sowbreira.appspot.com/" style="text-align: right;"
			target="_BLANK">Site Autor</a>
		<a id="link" style="position: absolute; left: 370px; top: 10px;font-size: 16px;"
			href="mailto:sowbreira@gmail.com" style="text-align: right;"
			target="_BLANK">Sugestões, Bugs ,Colaborar ...</a>		
		<a id="link" style="position: absolute; left: 220px; top: 50px;font-size: 16px;"
			href="http://www.java.com/" style="text-align: right;"
			target="_BLANK">Instale o Java</a>
		<a id="link" style="position: absolute; left: 370px; top: 50px;font-size: 16px;"
			href="http://sowbreira.appspot.com/f1mane/F1Mane.html" style="text-align: right;"
			target="_BLANK">Versao 1 Jogador</a>	
		<br>					
	</div>
	<div style="text-align: left;">
		<applet code="sowbreira.f1mane.paddock.applet.AppletPaddock.class"	archive="f1mane.jar" width="800px" height="380px"> 
		</applet>
	</div>
	<div id="shots" >
		<a href="fm1.jpg" target="_BLANK"> <img src="fm1.jpg" width="130" height="120" /></a>
		<a href="fm2.jpg" target="_BLANK"> <img src="fm2.jpg" width="130" height="120" /></a>
		<a href="fm3.jpg" target="_BLANK"> <img src="fm3.jpg" width="130" height="120" /></a>
		<a href="fm4.jpg" target="_BLANK"> <img src="fm4.jpg" width="130" height="120" /></a>
		<a href="fm5.jpg" target="_BLANK"> <img src="fm5.jpg" width="130" height="120" /></a>
		<a href="fm6.jpg" target="_BLANK"> <img src="fm6.jpg" width="130" height="120" /></a>
	</div>
</div>
</body>
</html>
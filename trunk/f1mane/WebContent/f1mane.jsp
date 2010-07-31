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
	position: relative;
	left: 0px;
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
	left: 150px;
	top : 0px;
	position: absolute;
}

#shots{
	padding: 3px;
	left: 910px;
	top : 10px;
	position: absolute;
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
		<span >Paulo Sobreira F1 Mane
			<h1 id="description">MANager & Engineer</h1>
		</span>
		<a id="link" style="position: absolute; left: 343px; top: 5%;font-size: 16px;"
			href="http://twitter.com/PauloSobreira" style="text-align: right;"
			target="_BLANK">Paulo Sobreira no Twitter</a>
	</div>
	<div>
		<br>
		<applet code="sowbreira.f1mane.paddock.applet.AppletPaddock.class"	archive="f1mane.jar" width="800px" height="500px"> 
	</div>
	<div>
		<a	href="http://sowbreira.appspot.com/">Viste http://sowbreira.appspot.com/</a>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<a href="http://sowbreira.appspot.com/f1mane/F1Mane.html">
				Vers&atilde;o do Jogo modo 1 jogador 
		<br>
			<a href="http://www.java.com/pt_BR/">
				Se abaixo nao aparecer nada Clique Aqui e instale a versao mais nova do
				Java. 
			</a>
		<br>
		<a href="mailto:sowbreira@gmail.com">
			Duvidas, Criticas, Sugestões, Bugs ,Colaborar ...</a> 
	</div>
</div>
<div id="shots" style="text-align: center;	padding: 3px; padding-left: 5px; padding-right: 5px;">
	<a href="fm1.jpg" target="_BLANK"> <img src="fm1.jpg" width="160" height="120" />
	<a href="fm2.jpg" target="_BLANK"> <img src="fm2.jpg" width="160" height="120" /><br>
	<a href="fm3.jpg" target="_BLANK"> <img src="fm3.jpg" width="160" height="120" />
	<a href="fm4.jpg" target="_BLANK"> <img src="fm4.jpg" width="160" height="120" /><br>
	<a href="fm5.jpg" target="_BLANK"> <img src="fm5.jpg" width="160" height="120" />
	<a href="fm6.jpg" target="_BLANK"> <img src="fm6.jpg" width="160" height="120" /><br>
</div>
</body>
</html>
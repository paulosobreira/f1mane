<%
    response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
    response.setHeader("Pragma","no-cache"); //HTTP 1.0
    response.setDateHeader ("Expires", -1); //evita o caching no servidor proxy
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
		<META HTTP-EQUIV="Expires" CONTENT="-1">
		<title>
			F1-Mane Online
		</title>
		<style type="text/css">
			.class1 A:link {text-decoration: none;color:white;}
			.class1 A:visited {text-decoration: none;color:white;}
			.class1 A:active {text-decoration: none;color:white;}
			.class1 A:hover {text-decoration: underline; color: black;}
			
			.class2 A:link {text-decoration: none;color:black;}
			.class2 A:visited {text-decoration: none;color:black;}
			.class2 A:active {text-decoration: none;color:black;}
			.class2 A:hover {text-decoration: underline; color: black;}
			
		</style>
	</head>
	<body style="background-color:white">
	<center>
	<div>
	<span class="class1">
	<a  href="http://br.geocities.com/sowbreira/" > <img src="f1maneonline.jpg"> </a>
	<a  href="http://www.mozilla.com/firefox/" > <img src="trust.gif"> </a><br>
	</span>
	<span class="class2">
	<!--Versao do Java no Servidor: <%= (pageContext.getServletContext().getAttribute("java.vm.version") !=null?pageContext.getServletContext().getAttribute("java.vm.version") :pageContext.getServletContext().getAttribute("java.runtime.version") ) %>-->
	<a href="http://www.java.com/pt_BR/" >
	Se abaixo nao aparecer nada Clique Aqui e instale a versao mais nova do Java.
	</a>
	<a href="http://sowbreira.googlepages.com/F1Mane.html" >
	Vers&atilde;o do Jogo modo 1 jogador
	</a>
	<a href="ServletBaseDados?tipo=" >
	 Bkp dados
	</a>
	
	</span>
	</div>
	<div>
	 <applet code="sowbreira.f1mane.paddock.applet.AppletPaddock.class" archive="f1mane.jar"
 				width="100%" height="80%">
	 
	</div>				

	<center>
	</body>
</html>

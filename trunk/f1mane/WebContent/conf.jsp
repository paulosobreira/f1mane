<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
	response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", -1); //evita o caching no servidor proxy
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>F1Mane Conf</title>
</head>
<body>
<br>
<a href="hibernate?act=create_schema"> Create Schema </a>
<br>
<a href="hibernate?act=update_schema"> Update Schema </a>
<br>
<a href="ServletBackUp"> Back Up </a>
<br>
<br>
<a href="ServletBaseDados?tipo=x"> Exceptions </a>
<br>
<br>
<a href="ServletBaseDados?tipo=C"> Contrutores </a>
<br>
<br>
<a href="ServletBaseDados?tipo="> Data File </a>
<br>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*,java.io.*"%>
<%@ page import="java.text.DecimalFormat"%>
<%@ page import="java.net.*"%>
<%
	response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", -1); //evita o caching no servidor proxy
	String hostName = request.getServerName();
%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<title>F1Mane Conf</title>
<style type="text/css">
body{
    font-family: sans-serif;
}
A:link {text-decoration: none; 
		color: black;}
A:visited {text-decoration: none;
		color: black;} 
A:active {text-decoration: none;
		color: black;}
A:hover {text-decoration: underline overline; color: black;}

h4 {
    border: solid;
    border-color: black;
    margin: 0px;
    text-align: center;
}

.c2 {
    font-weight: bolder;
}

</style>
<title>F1-Mane</title>
</head>
<body>
<table>
	<thead>
		<tr>
			<th colspan="2" align="left" ><h3>F1Mane
			Configuration Environment</h3></th>
		</tr>
	</thead>
	<tbody>
		<tr>
		<td class="c1">Java Version:</td>
		</tr>
		<tr>
			<td class="c2">
			<%
				String vmName = System.getProperty("java.vm.name");
				if (vmName == null) {
					vmName = "";
				} else {
					vmName = " -- " + vmName;
				}
			%> <%=System.getProperty("java.version")%> <%=System.getProperty("java.vendor")%><%=vmName%>
			</td>
		</tr>
		<tr>
			<td class="c1">Appserver:</td>
		</tr>		
		<tr>
			<td class="c2"><%=application.getServerInfo()%></td>
		</tr>
		<tr>
			<td class="c1">Host Name:</td>
		</tr>
		<tr>
			<td class="c2"><%=hostName%></td>
		</tr>
		<tr>
			<td class="c1">OS / Hardware:</td>
		</tr>		
		<tr>
			<td class="c2"><%=System.getProperty("os.name")%> / <%=System.getProperty("os.arch")%>
			</td>
		</tr>
		<tr>
			<td class="c1">Locale / Timezone:</td>
		</tr>			
		<tr>
			<td class="c2"><%=Locale.getDefault()%> / <%=TimeZone.getDefault().getDisplayName(
									Locale.getDefault())%> (<%=(TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60)%>
			GMT)</td>
		</tr>
		<tr>
			<td class="c1">Java Memory :</td>
		</tr>			
		<tr>
			<td>
			<%
				// The java runtime
				Runtime runtime = Runtime.getRuntime();

				double freeMemory = (double) runtime.freeMemory() / (1024 * 1024);
				double maxMemory = (double) runtime.maxMemory() / (1024 * 1024);
				double totalMemory = (double) runtime.totalMemory() / (1024 * 1024);
				double usedMemory = totalMemory - freeMemory;
				double percentFree = ((maxMemory - usedMemory) / maxMemory) * 100.0;
				double percentUsed = 100 - percentFree;
				int percent = 100 - (int) Math.round(percentFree);

				DecimalFormat mbFormat = new DecimalFormat("#0.00");
				DecimalFormat percentFormat = new DecimalFormat("#0.0");
			%>

			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr valign="middle">
				  <td width="100%" valign="middle">
					<div class="bar">
						<table cellpadding="0" cellspacing="0" border="0" width="100%"
							style="border: 1px #666 solid;">
							<tr>
								<%
									if (percent == 0) {
								%>
	
								<td width="100%"><img src="images/percent-bar-left.gif"
									width="100%" height="4" border="0" alt=""></td>
	
								<%
									} else {
								%>
	
								<%
									if (percent >= 90) {
								%>
	
								<td width="<%=percent%>%"
									background="images/percent-bar-used-high.gif"><img
									src="images/blank.gif" width="1" height="4" border="0" alt=""></td>
	
								<%
									} else {
								%>
	
								<td width="<%=percent%>%"
									background="images/percent-bar-used-low.gif"><img
									src="images/blank.gif" width="1" height="4" border="0" alt=""></td>
	
								<%
									}
								%>
								<td width="<%=(100 - percent)%>%"
									background="images/percent-bar-left.gif"><img
									src="images/blank.gif" width="1" height="4" border="0" alt=""></td>
								<%
									}
								%>
							</tr>
						</table>
					  </div>
						<div style="padding-left: 6px;" class="c2"><%=mbFormat.format(usedMemory)%>
							MB of <%=mbFormat.format(maxMemory)%> MB (<%=percentFormat.format(percentUsed)%>%)
						used</div>
					</td>
				</tr>
			</table>
			</td>
		</tr>
	</tbody>
</table>
<br>
<div style="padding: 10px">
	<input type="password" id="senha" style="width: 100%"/>
</div>
<br>
<h4><a href="#" onclick="criar();">Criar Schema</a></h4><br>
<h4><a href="#" onclick="atualizar()">Atualizar Schema</a></h4><br>
<h4><a href="ServletPaddock?tipo=X">Exceptions</a></h4><br>
<h4><a href="ServletPaddock?tipo=S">Sess&otilde;es</a></h4><br>
</body>
<script>
	function criar(){
		window.location = "ServletPaddock?tipo=create_schema&senha=" + document.getElementById('senha').value;
	}
	function atualizar(){
		window.location = "ServletPaddock?tipo=update_schema&senha=" + document.getElementById('senha').value;
	}
</script>
</html>


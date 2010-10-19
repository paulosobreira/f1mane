<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*,java.io.*"%>
<%@ page import="java.text.DecimalFormat"%>
<%@ page import="java.net.*"%>
<%
	response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", -1); //evita o caching no servidor proxy
	String hostName = request.getServerName();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>F1Mane Conf</title>
<style type="text/css">
A:link {text-decoration: none; 
		color: #0084B4;}
A:visited {text-decoration: none;
		color: #0084B4;} 
A:active {text-decoration: none;
		color: #0084B4;}
A:hover {text-decoration: underline overline; color: black;}
</style>
<title>F1-Mane</title>
</head>
<body>
<table>
	<thead>
		<tr>
			<th colspan="2" align="left" class="info-header">F1Mane
			Configuration Environment</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td class="c1">Java Version:</td>
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
			<td class="c2"><%=application.getServerInfo()%></td>
		</tr>
		<tr>
			<td class="c1">Host Name:</td>
			<td class="c2"><%=hostName%></td>
		</tr>
		<tr>
			<td class="c1">OS / Hardware:</td>
			<td class="c2"><%=System.getProperty("os.name")%> / <%=System.getProperty("os.arch")%>
			</td>
		</tr>
		<tr>
			<td class="c1">Locale / Timezone:</td>

			<td class="c2"><%=Locale.getDefault()%> / <%=TimeZone.getDefault().getDisplayName(
									Locale.getDefault())%> (<%=(TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60)%>
			GMT)</td>
		</tr>
		<tr>
			<td class="c1">Java Memory</td>
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

			<table cellpadding="0" cellspacing="0" border="0" width="300">
				<tr valign="middle">
					<td width="99%" valign="middle">
					<div class="bar">
					<table cellpadding="0" cellspacing="0" border="0" width="100%"
						style="border: 1px #666 solid;">
						<tr>
							<%
								if (percent == 0) {
							%>

							<td width="100%"><img src="images/percent-bar-left.gif"
								width="100%" height="8" border="0" alt=""></td>

							<%
								} else {
							%>

							<%
								if (percent >= 90) {
							%>

							<td width="<%=percent%>%"
								background="images/percent-bar-used-high.gif"><img
								src="images/blank.gif" width="1" height="8" border="0" alt=""></td>

							<%
								} else {
							%>

							<td width="<%=percent%>%"
								background="images/percent-bar-used-low.gif"><img
								src="images/blank.gif" width="1" height="8" border="0" alt=""></td>

							<%
								}
							%>
							<td width="<%=(100 - percent)%>%"
								background="images/percent-bar-left.gif"><img
								src="images/blank.gif" width="1" height="8" border="0" alt=""></td>
							<%
								}
							%>
						</tr>
					</table>
					</div>
					</td>
					<td width="1%" nowrap>
					<div style="padding-left: 6px;" class="c2"><%=mbFormat.format(usedMemory)%>
					MB of <%=mbFormat.format(maxMemory)%> MB (<%=percentFormat.format(percentUsed)%>%)
					used</div>
					</td>
				</tr>
			</table>
			</td>
		</tr>
		<tr>
			<td class="c1"><a href="ServletPaddock?tipo=create_schema"> Create
			Schema </a></td>
			<td class="c2"><a href="ServletPaddock?tipo=update_schema"> Update
			Schema </a></td>
		</tr>
		<tr>
			<td class="c1"><a href="ServletPaddock?tipo=x"> Exceptions
			</a></td>
			<td class="c2"><a href="ServletPaddock?tipo=C">
			Contrutores </a></td>
		</tr>
	</tbody>
</table>
</body>
</html>


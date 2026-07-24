package br.flmane.servidor.servlet;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Gera em Java o mesmo HTML que {@code conf.jsp} produzia via Jasper/JSP —
 * versão Java, appserver, host, OS, locale/timezone e uso de memória —
 * servido agora sem depender de um motor de JSP (não há mais Tomcat).
 */
public final class PaginaConf {

    private PaginaConf() {
    }

    public static String gerar(String hostName) {
        String vmName = System.getProperty("java.vm.name");
        vmName = (vmName == null) ? "" : " -- " + vmName;

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

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n<html>\n<head>\n");
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no\">\n");
        html.append("<title>FlMane Conf</title>\n");
        html.append("<style type=\"text/css\">\n");
        html.append("body{\n    font-family: sans-serif;\n}\n");
        html.append("A:link {text-decoration: none; \n\t\tcolor: black;}\n");
        html.append("A:visited {text-decoration: none;\n\t\tcolor: black;} \n");
        html.append("A:active {text-decoration: none;\n\t\tcolor: black;}\n");
        html.append("A:hover {text-decoration: underline overline; color: black;}\n\n");
        html.append("h4 {\n    border: solid;\n    border-color: black;\n    margin: 0px;\n    text-align: center;\n}\n\n");
        html.append(".c2 {\n    font-weight: bolder;\n}\n\n");
        html.append("</style>\n");
        html.append("<title>Fl-Mane</title>\n");
        html.append("</head>\n<body>\n");
        html.append("<table>\n\t<thead>\n\t\t<tr>\n");
        html.append("\t\t\t<th colspan=\"2\" align=\"left\" ><h3>FlMane\n\t\t\tConfiguration Environment</h3></th>\n");
        html.append("\t\t</tr>\n\t</thead>\n\t<tbody>\n");

        html.append("\t\t<tr>\n\t\t<td class=\"c1\">Java Version:</td>\n\t\t</tr>\n");
        html.append("\t\t<tr>\n\t\t\t<td class=\"c2\">\n\t\t\t")
                .append(System.getProperty("java.version")).append(" ")
                .append(System.getProperty("java.vendor")).append(vmName)
                .append("\n\t\t\t</td>\n\t\t</tr>\n");

        html.append("\t\t<tr>\n\t\t\t<td class=\"c1\">Appserver:</td>\n\t\t</tr>\t\t\n");
        html.append("\t\t<tr>\n\t\t\t<td class=\"c2\">Fl-Mane embedded Netty server</td>\n\t\t</tr>\n");

        html.append("\t\t<tr>\n\t\t\t<td class=\"c1\">Host Name:</td>\n\t\t</tr>\n");
        html.append("\t\t<tr>\n\t\t\t<td class=\"c2\">").append(hostName).append("</td>\n\t\t</tr>\n");

        html.append("\t\t<tr>\n\t\t\t<td class=\"c1\">OS / Hardware:</td>\n\t\t</tr>\t\t\n");
        html.append("\t\t<tr>\n\t\t\t<td class=\"c2\">")
                .append(System.getProperty("os.name")).append(" / ")
                .append(System.getProperty("os.arch"))
                .append("\n\t\t\t</td>\n\t\t</tr>\n");

        html.append("\t\t<tr>\n\t\t\t<td class=\"c1\">Locale / Timezone:</td>\n\t\t</tr>\t\t\t\n");
        html.append("\t\t<tr>\n\t\t\t<td class=\"c2\">")
                .append(Locale.getDefault()).append(" / ")
                .append(TimeZone.getDefault().getDisplayName(Locale.getDefault()))
                .append(" (").append(TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60)
                .append("\n\t\t\tGMT)</td>\n\t\t</tr>\n");

        html.append("\t\t<tr>\n\t\t\t<td class=\"c1\">Java Memory :</td>\n\t\t</tr>\t\t\t\n");
        html.append("\t\t<tr>\n\t\t\t<td>\n\t\t\t");
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n");
        html.append("\t\t\t\t<tr valign=\"middle\">\n\t\t\t\t  <td width=\"100%\" valign=\"middle\">\n");
        html.append("\t\t\t\t\t<div class=\"bar\">\n");
        html.append("\t\t\t\t\t\t<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"\n");
        html.append("\t\t\t\t\t\t\tstyle=\"border: 1px #666 solid;\">\n\t\t\t\t\t\t\t<tr>\n");
        if (percent == 0) {
            html.append("\t\t\t\t\t\t\t\t<td width=\"100%\"><img src=\"images/percent-bar-left.gif\"\n");
            html.append("\t\t\t\t\t\t\t\t\twidth=\"100%\" height=\"4\" border=\"0\" alt=\"\"></td>\n");
        } else {
            String cor = percent >= 90 ? "percent-bar-used-high.gif" : "percent-bar-used-low.gif";
            html.append("\t\t\t\t\t\t\t\t<td width=\"").append(percent).append("%\"\n");
            html.append("\t\t\t\t\t\t\t\t\tbackground=\"images/").append(cor).append("\"><img\n");
            html.append("\t\t\t\t\t\t\t\t\tsrc=\"images/blank.gif\" width=\"1\" height=\"4\" border=\"0\" alt=\"\"></td>\n");
            html.append("\t\t\t\t\t\t\t\t<td width=\"").append(100 - percent).append("%\"\n");
            html.append("\t\t\t\t\t\t\t\t\tbackground=\"images/percent-bar-left.gif\"><img\n");
            html.append("\t\t\t\t\t\t\t\t\tsrc=\"images/blank.gif\" width=\"1\" height=\"4\" border=\"0\" alt=\"\"></td>\n");
        }
        html.append("\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t  </div>\n");
        html.append("\t\t\t\t\t\t<div style=\"padding-left: 6px;\" class=\"c2\">")
                .append(mbFormat.format(usedMemory)).append("\n\t\t\t\t\t\t\tMB of ")
                .append(mbFormat.format(maxMemory)).append(" MB (")
                .append(percentFormat.format(percentUsed))
                .append("%)\n\t\t\t\t\t\tused</div>\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\t\t</table>\n\t\t\t</td>\n\t\t</tr>\n");

        html.append("\t</tbody>\n</table>\n<br>\n");
        html.append("<h4><a href='ServletPaddock?tipo=X'>Exceptions</a></h4><br>\n");
        html.append("<h4><a href='ServletPaddock?tipo=S'>Sess&otilde;es</a></h4><br>\n");
        html.append("</body>\n</html>");
        return html.toString();
    }
}

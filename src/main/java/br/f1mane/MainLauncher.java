package br.f1mane;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class MainLauncher {

    public static void main(String[] args) {

        try {

            // Porta HTTP
            int port = 8080;

            // Diretório webapp
            String webappDir = "src/main/webapp";

            File base = new File(webappDir);

            System.out.println("WEBAPP: " + base.getAbsolutePath());

            if (!base.exists()) {
                throw new RuntimeException(
                        "Diretorio webapp nao encontrado: "
                                + base.getAbsolutePath());
            }

            // Instancia Tomcat
            Tomcat tomcat = new Tomcat();

            // Porta
            tomcat.setPort(port);

            // Necessário para o Tomcat embedded
            tomcat.getConnector();

            // Contexto da aplicação
            Context context = tomcat.addWebapp(
                    "/flmane",
                    base.getAbsolutePath());

            // web.xml
            File webXml = new File(base, "WEB-INF/web.xml");

            if (webXml.exists()) {
                context.setConfigFile(webXml.toURI().toURL());
                System.out.println("WEB.XML: " + webXml.getAbsolutePath());
            } else {
                System.out.println("WEB.XML nao encontrado");
            }

            // Inicializa
            tomcat.start();

            System.out.println("=================================");
            System.out.println("SERVER STARTED");
            System.out.println("URL:");
            System.out.println("http://localhost:8080/flmane/html5/index.html");
            System.out.println("=================================");

            // Mantém JVM viva
            tomcat.getServer().await();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
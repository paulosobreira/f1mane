package br.f1mane.servidor.servlet;

import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.servidor.MonitorAtividade;
import br.f1mane.servidor.PaddockConstants;
import br.f1mane.servidor.PaddockServer;
import br.f1mane.servidor.controles.ControlePaddockServidor;
import br.f1mane.servidor.entidades.TOs.SessaoCliente;
import br.f1mane.servidor.entidades.persistencia.*;
import br.f1mane.servidor.util.ZipUtil;
import br.nnpe.FormatDate;
import br.nnpe.Logger;
import br.nnpe.Util;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author paulo.sobreira
 */
public class ServletPaddock extends HttpServlet {
    private final static String lock = "lock";
    private ControlePaddockServidor controlePaddock;
    private static MonitorAtividade monitorAtividade;
    String senha;

    @Override
    public void init() throws ServletException {
        Logger.logar("Init");
        PaddockServer.init(getServletContext().getRealPath(""));
        controlePaddock = PaddockServer.getControlePaddock();
        monitorAtividade = PaddockServer.getMonitorAtividade();
        try {
            Properties properties = new Properties();
            properties.load(CarregadorRecursos.recursoComoStream("application.properties"));
            senha = properties.getProperty("senha");
            String createSchema = properties.getProperty("createSchema");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(15000);
                        Logger.logar("createSchema(null);");
                        createSchema(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            if("true".equalsIgnoreCase(createSchema)){
                thread.start();
            }
        } catch (Exception e) {
            Logger.logarExept(e);
        }
    }

    public void destroy() {
        monitorAtividade.setAlive(false);
        super.destroy();
    }

    public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        doGet(arg0, arg1);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            ObjectInputStream inputStream = null;
            try {
                inputStream = new ObjectInputStream(req.getInputStream());
            } catch (Exception e) {
                Logger.logar("inputStream null - > doGetHtml");
            }
            if (inputStream != null) {
                Object object = null;

                object = inputStream.readObject();

                Object escrever = controlePaddock
                        .processarObjetoRecebido(object);

                if (PaddockConstants.modoZip) {
                    dumparDadosZip(ZipUtil.compactarObjeto(
                            PaddockConstants.dumparDados, escrever,
                            res.getOutputStream()));
                } else {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(escrever);
                    oos.flush();
                    dumparDados(escrever);
                    res.getOutputStream().write(bos.toByteArray());
                }

                return;
            } else {
                doGetHtml(req, res);
                return;
            }
        } catch (Exception e) {
            Logger.topExecpts(e);
        }
    }

    private void dumparDadosZip(ByteArrayOutputStream byteArrayOutputStream)
            throws IOException {
        if (PaddockConstants.dumparDados) {
            String basePath = getServletContext().getRealPath("")
                    + File.separator + "WEB-INF" + File.separator;
            FileOutputStream fileOutputStream = new FileOutputStream(
                    basePath + "Pack-" + System.currentTimeMillis() + ".zip");
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.close();

        }

    }

    private void dumparDados(Object escrever) throws IOException {
        if (PaddockConstants.dumparDados && (escrever != null)) {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    arrayOutputStream);
            objectOutputStream.writeObject(escrever);
            String basePath = getServletContext().getRealPath("")
                    + File.separator + "WEB-INF" + File.separator;
            FileOutputStream fileOutputStream = new FileOutputStream(
                    basePath + escrever.getClass().getSimpleName() + "-"
                            + System.currentTimeMillis() + ".txt");
            fileOutputStream.write(arrayOutputStream.toByteArray());
            fileOutputStream.close();

        }

    }

    public void doGetHtml(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        PrintWriter printWriter = res.getWriter();
        res.setContentType("text/html");
        try {
            html5(printWriter);
            printWriter.println("<body>");
            String tipo = req.getParameter("tipo");

            String senhaP = req.getParameter("senha");

            boolean precisaSenha = "create_schema".equals(tipo)
                    || "update_schema".equals(tipo);

            if (precisaSenha
                    && (senhaP == null || !senha.equals(Util.md5(senhaP)))) {
                printWriter.println("<br/><a href='conf.jsp'>Voltar</a>");
                printWriter.println("</body></html>");
                return;
            }

            if (tipo == null) {
                return;
            } else if ("X".equals(tipo)) {
                topExceptions(res);
            } else if ("S".equals(tipo)) {
                sessoesAtivas(res);
            } else if ("create_schema".equals(tipo)) {
                createSchema(printWriter);
            }
            printWriter.println("<br/> ");
        } catch (Exception e) {
            printWriter.println(e.getMessage());
        }
        printWriter.println("<br/><a href='conf.jsp'>back</a>");
        printWriter.println("</body></html>");
        res.flushBuffer();
    }

    private void createSchema(PrintWriter printWriter)
            throws Exception {
        SchemaExport export = new SchemaExport();
        export.create(EnumSet.of(TargetType.DATABASE), getMetaData().buildMetadata());
    }

    private MetadataSources getMetaData() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(CarregadorRecursos.recursoComoStream("META-INF/persistence.xml"));
        NodeList list = doc.getElementsByTagName("property");
        String url = null, pass = null, user = null, driver = null;
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String attr = element.getAttribute("name");
                if ("javax.persistence.jdbc.url".equals(attr)) {
                    url = element.getAttribute("value");
                } else if ("javax.persistence.jdbc.user".equals(attr)) {
                    user = element.getAttribute("value");
                } else if ("javax.persistence.jdbc.password".equals(attr)) {
                    pass = element.getAttribute("value");
                } else if ("javax.persistence.jdbc.driver".equals(attr)) {
                    driver = element.getAttribute("value");
                }
            }
        }
        Class.forName(driver);
        Connection connection =
                DriverManager.getConnection(url, user, pass);
        MetadataSources metadata = new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect")
                        .applySetting(AvailableSettings.CONNECTION_PROVIDER, new MyConnectionProvider(connection))
                        .build());
        metadata.addAnnotatedClass(F1ManeDados.class);
        metadata.addAnnotatedClass(JogadorDadosSrv.class);
        metadata.addAnnotatedClass(CampeonatoSrv.class);
        metadata.addAnnotatedClass(CarreiraDadosSrv.class);
        metadata.addAnnotatedClass(CorridaCampeonatoSrv.class);
        metadata.addAnnotatedClass(CorridasDadosSrv.class);
        metadata.addAnnotatedClass(DadosCorridaCampeonatoSrv.class);
        metadata.addAnnotatedClass(PaddockDadosSrv.class);
        return metadata;
    }

    private void topExceptions(HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        PrintWriter printWriter = res.getWriter();
        html5(printWriter);
        printWriter.write("<body>");
        printWriter.write("<h2>Fl-Mane Exceptions</h2><br><hr>");
        synchronized (lock) {
            Set top = Logger.topExceptions.keySet();
            for (Iterator iterator = top.iterator(); iterator.hasNext(); ) {
                String exept = (String) iterator.next();
                printWriter.write(
                        "Quantidade : " + Logger.topExceptions.get(exept));
                printWriter.write("<br>");
                printWriter.write(exept);
                printWriter.write("<br><hr>");
            }
        }
        res.flushBuffer();
    }

    private void sessoesAtivas(HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        PrintWriter printWriter = res.getWriter();
        html5(printWriter);
        printWriter.write("<body>");
        printWriter.write("<h2>Fl-Mane Sess&otilde;es</h2><br>");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        printWriter.write("Hora Servidor : " + FormatDate.format(timestamp));
        printWriter.write("<br><hr>");
        List<SessaoCliente> clientes = controlePaddock.getDadosPaddock().getClientes();
        int cont = 0;
        for (Iterator iterator = clientes.iterator(); iterator.hasNext(); ) {
            SessaoCliente sessaoCliente = (SessaoCliente) iterator.next();
            printWriter.write("<br>");
            printWriter.write("Jogador : " + sessaoCliente.getNomeJogador());
            printWriter.write("<br>");
            timestamp = new Timestamp(sessaoCliente.getUlimaAtividade());
            printWriter.write("&Uacute;ltima Atividade : " + FormatDate.format(timestamp));
            printWriter.write("<br>");
            printWriter.write("Jogo Atual : " + sessaoCliente.getJogoAtual());
            printWriter.write("<hr>");
            cont++;
        }
        printWriter.write("<br>");
        printWriter.write("Total : " + cont);
        printWriter.write("<br>");
        res.flushBuffer();
    }

    public void html5(PrintWriter printWriter) {
        printWriter.write("<!doctype html>");
        printWriter.write("<html><head>");
        printWriter.write("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
        printWriter.write("<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no'>");
        printWriter.write("</head>");
    }


    private static class MyConnectionProvider implements ConnectionProvider {
        private final Connection connection;

        public MyConnectionProvider(Connection connection) {
            this.connection = connection;
        }

        @Override
        public boolean isUnwrappableAs(Class unwrapType) {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> unwrapType) {
            return null;
        }

        @Override
        public Connection getConnection() {
            return connection; // Interesting part here
        }

        @Override
        public void closeConnection(Connection conn) throws SQLException {
        }

        @Override
        public boolean supportsAggressiveRelease() {
            return true;
        }
    }
}

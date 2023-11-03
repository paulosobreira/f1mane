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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.UnknownHostException;
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
    String host = "localhot";
    String senha;
    int port = 80;

    public void init() throws ServletException {
        super.init();
        PaddockServer.init(getServletContext().getRealPath(""));
        controlePaddock = PaddockServer.getControlePaddock();
        monitorAtividade = PaddockServer.getMonitorAtividade();
        try {
            Properties properties = new Properties();
            properties.load(CarregadorRecursos.recursoComoStream("application.properties"));
            host = properties.getProperty("host");
            senha = properties.getProperty("senha");
            port = Integer.parseInt(properties.getProperty("port"));
        } catch (Exception e) {
            Logger.logarExept(e);
        }

    }

    private String obterHost() throws UnknownHostException {
        return host + ":" + port;
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
                Object object;

                object = inputStream.readObject();

                Object escrever = controlePaddock
                        .processarObjetoRecebido(object);

                if (PaddockConstants.modoZip) {
                    dumaparDadosZip(ZipUtil.compactarObjeto(
                            PaddockConstants.dumparDados, escrever,
                            res.getOutputStream()));
                } else {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(escrever);
                    oos.flush();
                    dumaparDados(escrever);
                    res.getOutputStream().write(bos.toByteArray());
                }

            } else {
                doGetHtml(req, res);
            }
            return;
        } catch (Exception e) {
            Logger.topExecpts(e);
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

    private MetadataSources getMetaData() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        //TODO ler do src/main/resources/META-INF/persistence.xml
        Connection connection =
                DriverManager.getConnection("jdbc:mysql://db:3306/f1mane?characterEncoding=utf8", "root", "f1mane");
        MetadataSources metadata = new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect")
                        .applySetting(AvailableSettings.CONNECTION_PROVIDER, new ConnectionProvider() {
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
                        })
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

    private void dumaparDadosZip(ByteArrayOutputStream byteArrayOutputStream)
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

    private void dumaparDados(Object escrever) throws IOException {
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
}

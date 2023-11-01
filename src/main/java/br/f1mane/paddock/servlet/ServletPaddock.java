package br.f1mane.paddock.servlet;

import br.f1mane.paddock.PaddockConstants;
import br.f1mane.paddock.PaddockServer;
import br.f1mane.paddock.ZipUtil;
import br.f1mane.paddock.entidades.TOs.SessaoCliente;
import br.f1mane.recursos.CarregadorRecursos;
import br.nnpe.FormatDate;
import br.nnpe.HibernateUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
                Object object = null;

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

                return;
            } else {
                doGetHtml(req, res);
                return;
            }
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
            } else if ("update_schema".equals(tipo)) {
                updateSchema(printWriter);
            }
            printWriter.println("<br/> ");
        } catch (Exception e) {
            printWriter.println(e.getMessage());
        }
        printWriter.println("<br/><a href='conf.jsp'>back</a>");
        printWriter.println("</body></html>");
        res.flushBuffer();
    }

    private void updateSchema(PrintWriter printWriter) throws Exception {
        AnnotationConfiguration cfg = new AnnotationConfiguration();
        cfg.configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Dialect dialect = Dialect.getDialect(cfg.getProperties());
        InitialContext cxt = new InitialContext();

        DataSource ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/MySQLDS");

        if (ds == null) {
            throw new Exception("Data source not found!");
        }
        DatabaseMetadata meta = new DatabaseMetadata(ds.getConnection(),
                dialect);
        String[] strings = cfg.generateSchemaUpdateScript(dialect, meta);
        executeStatement(sessionFactory, strings, printWriter);

    }

    private void executeStatement(SessionFactory sessionFactory,
                                  String[] strings, PrintWriter printWriter) throws Exception {

        AnnotationConfiguration cfg = new AnnotationConfiguration();
        cfg.configure("hibernate.cfg.xml");
        Dialect dialect = Dialect.getDialect(cfg.getProperties());

        InitialContext cxt = new InitialContext();

        DataSource ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/MySQLDS");

        if (ds == null) {
            throw new Exception("Data source not found!");
        }
        DatabaseMetadata meta = new DatabaseMetadata(ds.getConnection(),
                dialect);

        Connection connection = ds.getConnection();
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            java.sql.Statement statement = connection
                    .createStatement();
            statement.execute(string);
            printWriter.println("<br/> " + string);
        }
        connection.close();
    }

    private void createSchema(PrintWriter printWriter)
            throws Exception {
        AnnotationConfiguration cfg = new AnnotationConfiguration();
        cfg.configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Dialect dialect = Dialect.getDialect(cfg.getProperties());
        String[] strings = cfg.generateSchemaCreationScript(dialect);
        executeStatement(sessionFactory, strings, printWriter);
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

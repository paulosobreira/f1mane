package br.f1mane.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Piloto;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.SpriteSheet;

/**
 * Utilitário visual pra editar carros (cores, nome-homenagem) e pilotos
 * (nome-homenagem, habilidade) de uma temporada. Lê e salva diretamente nos
 * .properties da temporada selecionada.
 *
 * Uso: execute a partir da raiz do projeto (working directory), pois grava
 * diretamente em src/main/resources/properties — mesmo padrão de SpriteSheet.main.
 * java -cp target/classes br.f1mane.editor.EditorCarrosPilotos
 */
public class EditorCarrosPilotos extends JFrame {

    private static final int PREVIEW_CIMA_W  = 90;
    private static final int PREVIEW_CIMA_H  = 90;
    private static final int PREVIEW_LADO_W  = 180;
    private static final int PREVIEW_LADO_H  = 40;
    private static final int PREVIEW_CAP_W   = 55;
    private static final int PREVIEW_CAP_H   = 55;
    private static final int CARD_W          = 340;
    private static final int CARD_H_CARRO    = 266;
    private static final int CARD_H_PILOTO   = 124;

    private enum Modo { CARROS, PILOTOS }

    // ── estado ───────────────────────────────────────────────────────────────
    private String temporadaAtual;
    private Modo modoAtual = Modo.CARROS;
    private List<CarroEntry> entradasCarros = new ArrayList<>();
    private List<PilotoEntry> entradasPilotos = new ArrayList<>();
    private final List<String> temporadas = new ArrayList<>();
    private int indiceTemporada = -1;

    // ── componentes principais ────────────────────────────────────────────────
    private final JLabel lblTemporada = new JLabel();
    private final JButton btnTemporadaAnterior = new JButton("◀ Anterior");
    private final JButton btnTemporadaProxima = new JButton("Próxima ▶");
    private final JComboBox<String> comboModo = new JComboBox<>(new String[]{"Carros", "Pilotos"});
    private final JPanel painelItens = new JPanel();
    private final JScrollPane scroll;

    public EditorCarrosPilotos() {
        super("Editor de Carros e Pilotos (Homenagem) — FlMane");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1500, 950);
        setLocationRelativeTo(null);

        // ── barra superior ────────────────────────────────────────────────────
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topo.setBorder(new EmptyBorder(4, 8, 4, 8));
        topo.add(new JLabel("Temporada:"));
        btnTemporadaAnterior.addActionListener(e -> navegarTemporada(-1));
        topo.add(btnTemporadaAnterior);
        topo.add(lblTemporada);
        btnTemporadaProxima.addActionListener(e -> navegarTemporada(1));
        topo.add(btnTemporadaProxima);

        topo.add(new JLabel("   Modo:"));
        comboModo.addActionListener(e -> trocarModo());
        topo.add(comboModo);

        JButton btnSalvarTodos = new JButton("Salvar Todos");
        btnSalvarTodos.addActionListener(e -> salvarTodos());
        topo.add(btnSalvarTodos);

        // ── painel de itens (carros ou pilotos, conforme o modo) ────────────────
        painelItens.setLayout(new GridLayout(0, 4, 12, 12));
        painelItens.setBorder(new EmptyBorder(8, 8, 8, 8));
        scroll = new JScrollPane(painelItens,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        setLayout(new BorderLayout());
        add(topo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // ── popular temporadas ────────────────────────────────────────────────
        popularTemporadas();
        indiceTemporada = temporadas.size() - 1;
        atualizarNavegacaoTemporada();
    }

    // ── navegação anterior/próxima entre temporadas ───────────────────────────
    private void navegarTemporada(int delta) {
        int novoIndice = indiceTemporada + delta;
        if (novoIndice < 0 || novoIndice >= temporadas.size()) return;
        indiceTemporada = novoIndice;
        atualizarNavegacaoTemporada();
    }

    private void trocarModo() {
        modoAtual = "Pilotos".equals(comboModo.getSelectedItem()) ? Modo.PILOTOS : Modo.CARROS;
        carregarTemporada(temporadaAtual);
    }

    private void atualizarNavegacaoTemporada() {
        boolean temTemporada = indiceTemporada >= 0 && indiceTemporada < temporadas.size();
        btnTemporadaAnterior.setEnabled(temTemporada && indiceTemporada > 0);
        btnTemporadaProxima.setEnabled(temTemporada && indiceTemporada < temporadas.size() - 1);
        if (temTemporada) {
            String temporada = temporadas.get(indiceTemporada);
            lblTemporada.setText(temporada);
            carregarTemporada(temporada);
        } else {
            lblTemporada.setText("—");
        }
    }

    // ── carregar lista de temporadas a partir dos recursos-fonte ─────────────
    private void popularTemporadas() {
        temporadas.clear();
        try {
            Properties p = new Properties();
            InputStream is = CarregadorRecursos.recursoComoStream("properties/temporadas.properties");
            if (is != null) {
                p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                List<String> anos = new ArrayList<>();
                for (String k : p.stringPropertyNames()) {
                    anos.add(k);
                }
                Collections.sort(anos);
                temporadas.addAll(anos);
            }
        } catch (Exception ex) {
            // fallback: varredura do diretório de recursos-fonte do projeto
            File dir = new File("src/main/resources/properties");
            if (dir.isDirectory()) {
                List<String> dirs = new ArrayList<>();
                for (File f : Objects.requireNonNull(dir.listFiles())) {
                    if (f.isDirectory() && f.getName().startsWith("t")) dirs.add(f.getName());
                }
                Collections.sort(dirs);
                temporadas.addAll(dirs);
            }
        }
    }

    // ── carregar temporada (roteia pro modo atual) ────────────────────────────
    private void carregarTemporada(String temporada) {
        if (temporada == null) return;
        temporadaAtual = temporada;
        painelItens.removeAll();

        try {
            if (modoAtual == Modo.CARROS) {
                carregarCarros(temporada);
            } else {
                carregarPilotos(temporada);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar temporada: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        painelItens.revalidate();
        painelItens.repaint();
    }

    private void carregarCarros(String temporada) throws IOException {
        entradasCarros.clear();
        List<Carro> carros = lerCarrosDoArquivo(temporada);
        carros.sort(Comparator.comparingInt((Carro c) ->
                (c.getPotencia() + c.getAerodinamica() + c.getFreios()) / 3).reversed());

        for (Carro carro : carros) {
            CarroEntry entry = new CarroEntry(carro, carro.getNomeOriginal(), temporada);
            entradasCarros.add(entry);
            painelItens.add(criarCartaoCarro(entry));
        }
    }

    private void carregarPilotos(String temporada) throws IOException {
        entradasPilotos.clear();
        List<Piloto> pilotos = lerPilotosDoArquivo(temporada);
        List<Carro> carros = lerCarrosDoArquivo(temporada);
        for (Piloto piloto : pilotos) {
            for (Carro carro : carros) {
                if (piloto.getChaveCarro() != null && piloto.getChaveCarro().equals(carro.getNomeOriginal())) {
                    piloto.setCarro(carro);
                    break;
                }
            }
        }

        for (Piloto piloto : pilotos) {
            PilotoEntry entry = new PilotoEntry(piloto, piloto.getNomeOriginal(), temporada);
            entradasPilotos.add(entry);
            painelItens.add(criarCartaoPiloto(entry));
        }
    }

    /**
     * Lê carros.properties diretamente de src/main/resources (não do
     * classpath/target/classes, que só é atualizado num rebuild) — assim o
     * editor sempre reflete o que foi salvo por ele mesmo na sessão anterior,
     * sem exigir `mvn compile` entre uma edição e a próxima abertura.
     */
    private List<Carro> lerCarrosDoArquivo(String temporada) throws IOException {
        List<Carro> retorno = new ArrayList<>();
        File arquivo = arquivoCarros(temporada);
        if (!arquivo.isFile()) return retorno;
        List<String> linhas = java.nio.file.Files.readAllLines(arquivo.toPath(), StandardCharsets.ISO_8859_1);
        int id = 1;
        for (String linha : linhas) {
            String trimmed = linha.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            int eq = trimmed.indexOf('=');
            if (eq < 0) continue;
            String chave = trimmed.substring(0, eq).trim();
            String[] values = trimmed.substring(eq + 1).split(",", -1);
            if (values.length < 8) continue;

            Carro carro = new Carro();
            carro.setId(id++);
            carro.setNomeOriginal(chave);
            String nomeHomenagem = values.length > 10 && !values[10].trim().isEmpty() ? values[10].trim() : null;
            carro.setNomeHomenagem(nomeHomenagem);
            carro.setNome(chave); // o editor sempre mostra o nome real como título (ver criarCartaoCarro)
            carro.setPotencia(Integer.parseInt(values[0].trim()));
            carro.setPotenciaReal(carro.getPotencia());
            carro.setCor1(new Color(Integer.parseInt(values[1].trim()), Integer.parseInt(values[2].trim()),
                    Integer.parseInt(values[3].trim())));
            carro.setCor2(new Color(Integer.parseInt(values[5].trim()), Integer.parseInt(values[6].trim()),
                    Integer.parseInt(values[7].trim())));
            carro.setAerodinamica(values.length > 8 ? Integer.parseInt(values[8].trim()) : carro.getPotencia());
            carro.setFreios(values.length > 9 ? Integer.parseInt(values[9].trim()) : carro.getPotencia());
            String[] tnsCarros = values[4].trim().split(";");
            carro.setImg("carros/" + temporada + "/" + tnsCarros[0].trim());
            retorno.add(carro);
        }
        return retorno;
    }

    /** Análogo a {@link #lerCarrosDoArquivo}, mas pra pilotos.properties. */
    private List<Piloto> lerPilotosDoArquivo(String temporada) throws IOException {
        List<Piloto> retorno = new ArrayList<>();
        File arquivo = arquivoPilotos(temporada);
        if (!arquivo.isFile()) return retorno;
        List<String> linhas = java.nio.file.Files.readAllLines(arquivo.toPath(), StandardCharsets.ISO_8859_1);
        int id = 1;
        for (String linha : linhas) {
            String trimmed = linha.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            int eq = trimmed.indexOf('=');
            if (eq < 0) continue;
            String chave = trimmed.substring(0, eq).trim();
            String[] values = trimmed.substring(eq + 1).split(",", -1);
            if (values.length < 2) continue;

            Piloto piloto = new Piloto();
            piloto.setId(id++);
            piloto.setNomeOriginal(chave);
            piloto.setChaveCarro(values[0].trim());
            piloto.setNome(chave);
            String nomeHomenagem = values.length > 2 && !values[2].trim().isEmpty() ? values[2].trim() : null;
            piloto.setNomeHomenagem(nomeHomenagem);
            int duasCasas = Integer.parseInt(values[1].trim());
            piloto.setHabilidadeReal(duasCasas * 10);
            piloto.setHabilidade(piloto.getHabilidadeReal());
            retorno.add(piloto);
        }
        return retorno;
    }

    // ── cartão visual de um carro ─────────────────────────────────────────────
    private JPanel criarCartaoCarro(CarroEntry entry) {
        JPanel card = new JPanel(null); // layout absoluto para posicionamento livre
        card.setPreferredSize(new Dimension(CARD_W, CARD_H_CARRO));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                new EmptyBorder(6, 6, 6, 6)));
        card.setBackground(new Color(30, 30, 30));

        // nome do carro (real)
        JLabel lblNome = new JLabel(entry.carro.getNomeOriginal());
        lblNome.setForeground(Color.WHITE);
        lblNome.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblNome.setBounds(6, 4, CARD_W - 12, 18);
        card.add(lblNome);

        // canvas de preview (lado + cima + cap, pintado com cor1/cor2)
        PreviewCanvas canvas = new PreviewCanvas(() -> entry.carro.getCor1(), () -> entry.carro.getCor2());
        canvas.setBounds(6, 26, PREVIEW_LADO_W + PREVIEW_CIMA_W + PREVIEW_CAP_W + 16, PREVIEW_LADO_H + 10);
        card.add(canvas);
        entry.canvas = canvas;

        // sprite original, ocupando a largura toda da linha, pra comparação/referência
        int ySprite = 80;
        int spriteRowH = 56;
        BufferedImage spriteLado = buscarSpriteLado(entry.carro, entry.temporada);
        BufferedImage spriteCima = buscarSpriteCima(entry.carro, entry.temporada);
        if (spriteLado != null || spriteCima != null) {
            int cimaW = spriteRowH;
            int ladoW = CARD_W - 12 - cimaW - 8;
            int xSprite = 6;
            if (spriteLado != null) {
                ImagemCanvas canvasSpriteLado = new ImagemCanvas(() -> spriteLado, ladoW, spriteRowH);
                canvasSpriteLado.setBounds(xSprite, ySprite, ladoW, spriteRowH);
                card.add(canvasSpriteLado);
                xSprite += ladoW + 8;
            }
            if (spriteCima != null) {
                ImagemCanvas canvasSpriteCima = new ImagemCanvas(() -> spriteCima, cimaW, spriteRowH);
                canvasSpriteCima.setBounds(xSprite, ySprite, cimaW, spriteRowH);
                card.add(canvasSpriteCima);
            }
        } else {
            JLabel lblSemSprite = new JLabel("(sem sprite sheet pra esta temporada)");
            lblSemSprite.setForeground(Color.GRAY);
            lblSemSprite.setFont(new Font("SansSerif", Font.ITALIC, 10));
            lblSemSprite.setBounds(6, ySprite + spriteRowH / 2 - 7, CARD_W - 12, 14);
            card.add(lblSemSprite);
        }

        int yBotoesCor = ySprite + spriteRowH + 8;

        // botões de cor
        JButton btnCor1 = criarBotaoCor("Cor 1", entry.carro.getCor1(), c -> {
            entry.carro.setCor1(c);
            entry.modificado = true;
            canvas.repaint();
        });
        // linha: Cor1 | ⇄ | Cor2
        int swapW = 50;
        int corBtnW = (CARD_W - 20 - swapW - 8) / 2; // 8 = 2 gaps de 4px
        btnCor1.setBounds(6, yBotoesCor, corBtnW, 26);
        card.add(btnCor1);
        entry.btnCor1 = btnCor1;

        JButton btnTrocar = new JButton("⇄");
        btnTrocar.setToolTipText("Trocar Cor 1 ↔ Cor 2");
        btnTrocar.setMargin(new Insets(0, 0, 0, 0));
        btnTrocar.setBounds(6 + corBtnW + 4, yBotoesCor, swapW, 26);
        btnTrocar.addActionListener(e -> {
            Color tmp = entry.carro.getCor1();
            entry.carro.setCor1(entry.carro.getCor2());
            entry.carro.setCor2(tmp);
            entry.modificado = true;
            btnCor1.setBackground(entry.carro.getCor1());
            btnCor1.setForeground(luminancia(entry.carro.getCor1()) > 128 ? Color.BLACK : Color.WHITE);
            entry.btnCor2.setBackground(entry.carro.getCor2());
            entry.btnCor2.setForeground(luminancia(entry.carro.getCor2()) > 128 ? Color.BLACK : Color.WHITE);
            CarregadorRecursos.invalidarCacheModeloV2();
            canvas.repaint();
        });
        card.add(btnTrocar);

        JButton btnCor2 = criarBotaoCor("Cor 2", entry.carro.getCor2(), c -> {
            entry.carro.setCor2(c);
            entry.modificado = true;
            canvas.repaint();
        });
        btnCor2.setBounds(6 + corBtnW + 4 + swapW + 4, yBotoesCor, corBtnW, 26);
        card.add(btnCor2);
        entry.btnCor2 = btnCor2;

        // nome-homenagem
        int yHomenagem = yBotoesCor + 32;
        JLabel lblHomenagem = new JLabel("Homenagem:");
        lblHomenagem.setForeground(Color.LIGHT_GRAY);
        lblHomenagem.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblHomenagem.setBounds(6, yHomenagem, 80, 20);
        card.add(lblHomenagem);

        JTextField txtHomenagem = new JTextField(entry.carro.getNomeHomenagem());
        txtHomenagem.setBounds(88, yHomenagem, CARD_W - 20 - 82, 20);
        txtHomenagem.getDocument().addDocumentListener(simpleDocListener(() -> {
            entry.novoNomeHomenagem = txtHomenagem.getText();
            entry.modificado = true;
        }));
        card.add(txtHomenagem);

        // checkbox de propagação + botão salvar (ícone), lado a lado
        int btnSalvarW = 36;
        int yBotoesSalvar = yHomenagem + 22;
        JCheckBox chkPropagar = new JCheckBox("Aplicar a todas as temporadas");
        chkPropagar.setForeground(Color.LIGHT_GRAY);
        chkPropagar.setFont(new Font("SansSerif", Font.PLAIN, 10));
        chkPropagar.setOpaque(false);
        chkPropagar.setBounds(6, yBotoesSalvar, CARD_W - 20 - btnSalvarW - 4, 26);
        chkPropagar.addActionListener(e -> entry.propagarHomenagem = chkPropagar.isSelected());
        card.add(chkPropagar);

        JButton btnSalvar = criarBotaoSalvar();
        btnSalvar.setBounds(CARD_W - 14 - btnSalvarW, yBotoesSalvar, btnSalvarW, 26);
        btnSalvar.addActionListener(e -> salvarEntryCarro(entry));
        card.add(btnSalvar);

        // potência, aero, freios — spinners (100-999) lado a lado, numa linha só
        int y = yBotoesSalvar + 32;
        int grupoW = (CARD_W - 18) / 3;
        adicionarSpinner(card, 6, y, grupoW, "Pot",
                entry.carro.getPotencia(),
                v -> { entry.carro.setPotencia(v); entry.carro.setPotenciaReal(v); entry.modificado = true; });
        adicionarSpinner(card, 6 + grupoW + 3, y, grupoW, "Aero",
                entry.carro.getAerodinamica(),
                v -> { entry.carro.setAerodinamica(v); entry.modificado = true; });
        adicionarSpinner(card, 6 + (grupoW + 3) * 2, y, grupoW, "Fre",
                entry.carro.getFreios(),
                v -> { entry.carro.setFreios(v); entry.modificado = true; });

        return card;
    }

    // ── cartão visual de um piloto ─────────────────────────────────────────────
    private JPanel criarCartaoPiloto(PilotoEntry entry) {
        JPanel card = new JPanel(null);
        card.setPreferredSize(new Dimension(CARD_W, CARD_H_PILOTO));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                new EmptyBorder(6, 6, 6, 6)));
        card.setBackground(new Color(30, 30, 30));

        // linha 1: capacete (sprite real, não o modelo colorido — o editor
        // mostra a arte de referência, independente de MODO_HOMENAGEM) +
        // nome do piloto + carro, tudo na mesma linha
        Carro carroDoPiloto = entry.piloto.getCarro();
        Color cor1 = carroDoPiloto != null ? carroDoPiloto.getCor1() : Color.GRAY;
        Color cor2 = carroDoPiloto != null ? carroDoPiloto.getCor2() : Color.DARK_GRAY;
        BufferedImage capacete = buscarCapaceteSprite(entry.piloto, entry.temporada, cor1, cor2);
        int capW = 44, capH = 44;
        ImagemCanvas canvasCapacete = new ImagemCanvas(() -> capacete, capW, capH);
        canvasCapacete.setBounds(6, 6, capW, capH);
        card.add(canvasCapacete);

        int xInfo = 6 + capW + 8;
        JLabel lblNome = new JLabel(entry.piloto.getNomeOriginal());
        lblNome.setForeground(Color.WHITE);
        lblNome.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblNome.setBounds(xInfo, 8, CARD_W - 12 - xInfo, 18);
        card.add(lblNome);

        JLabel lblCarro = new JLabel(carroDoPiloto != null ? carroDoPiloto.getNomeOriginal() : entry.piloto.getChaveCarro());
        lblCarro.setForeground(Color.LIGHT_GRAY);
        lblCarro.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblCarro.setBounds(xInfo, 27, CARD_W - 12 - xInfo, 16);
        card.add(lblCarro);

        // linha 2: nome-homenagem + habilidade, lado a lado
        int yCampos = 6 + capH + 8;
        int habW = 82;
        JLabel lblHomenagem = new JLabel("Homenagem:");
        lblHomenagem.setForeground(Color.LIGHT_GRAY);
        lblHomenagem.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblHomenagem.setBounds(6, yCampos, 72, 20);
        card.add(lblHomenagem);

        JTextField txtHomenagem = new JTextField(entry.piloto.getNomeHomenagem());
        txtHomenagem.setBounds(80, yCampos, CARD_W - 20 - 74 - habW, 20);
        txtHomenagem.getDocument().addDocumentListener(simpleDocListener(() -> {
            entry.novoNomeHomenagem = txtHomenagem.getText();
            entry.modificado = true;
        }));
        card.add(txtHomenagem);

        JLabel lblHabilidade = new JLabel("Hab:");
        lblHabilidade.setForeground(Color.LIGHT_GRAY);
        lblHabilidade.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblHabilidade.setBounds(CARD_W - 14 - habW, yCampos, 28, 20);
        card.add(lblHabilidade);

        int habilidadeDuasCasas = entry.piloto.getHabilidadeReal() / 10;
        JSpinner spinHabilidade = new JSpinner(new SpinnerNumberModel(habilidadeDuasCasas, 1, 99, 1));
        spinHabilidade.setBounds(CARD_W - 14 - habW + 28, yCampos, habW - 28, 22);
        spinHabilidade.addChangeListener(e -> {
            entry.novaHabilidade = (int) spinHabilidade.getValue();
            entry.modificado = true;
        });
        card.add(spinHabilidade);

        // linha 3: checkbox de propagação + botão salvar (ícone), lado a lado
        int btnSalvarW = 36;
        int yBotoes = yCampos + 26;
        JCheckBox chkPropagar = new JCheckBox("Aplicar a todas as temporadas");
        chkPropagar.setForeground(Color.LIGHT_GRAY);
        chkPropagar.setFont(new Font("SansSerif", Font.PLAIN, 10));
        chkPropagar.setOpaque(false);
        chkPropagar.setBounds(6, yBotoes, CARD_W - 20 - btnSalvarW - 4, 26);
        chkPropagar.addActionListener(e -> entry.propagarHomenagem = chkPropagar.isSelected());
        card.add(chkPropagar);

        JButton btnSalvar = criarBotaoSalvar();
        btnSalvar.setBounds(CARD_W - 14 - btnSalvarW, yBotoes, btnSalvarW, 26);
        btnSalvar.addActionListener(e -> salvarEntryPiloto(entry));
        card.add(btnSalvar);

        return card;
    }

    /**
     * Busca o capacete do sprite sheet real da temporada (não o modelo
     * colorido) — o editor quer mostrar a arte original enquanto edita,
     * independente de Global.MODO_HOMENAGEM (que só afeta o jogo em si).
     * Cai pro modelo colorido só se não houver sprite sheet pra essa
     * temporada ou o piloto não for encontrado nele.
     */
    private BufferedImage buscarCapaceteSprite(Piloto piloto, String temporada, Color corFallback1, Color corFallback2) {
        if (SpriteSheet.isDisponivel(temporada)) {
            String driverKey = piloto.getNomeOriginal().replaceAll("\\.", "");
            int idx = CarregadorRecursos.indicePiloto(temporada, driverKey);
            if (idx >= 0) {
                BufferedImage img = SpriteSheet.getCapacete(temporada, idx);
                if (img != null) {
                    return img;
                }
            }
        }
        return CarregadorRecursos.pintarModeloV2("png/capacete-v2.png", corFallback1, corFallback2, PREVIEW_CAP_W, PREVIEW_CAP_H);
    }

    /**
     * Busca a imagem "lado" do sprite sheet real da equipe, pra comparação
     * lado a lado com o modelo pintado — {@code null} se a temporada não
     * tiver sprite sheet ou a equipe não estiver nele.
     */
    private BufferedImage buscarSpriteLado(Carro carro, String temporada) {
        if (SpriteSheet.isDisponivel(temporada) && carro.getImg() != null) {
            int idx = CarregadorRecursos.indiceTime(temporada, CarregadorRecursos.extrairTime(carro.getImg()));
            if (idx >= 0) {
                return SpriteSheet.getCarroLado(temporada, idx);
            }
        }
        return null;
    }

    /** Análogo a {@link #buscarSpriteLado}, mas pra imagem "cima". */
    private BufferedImage buscarSpriteCima(Carro carro, String temporada) {
        if (SpriteSheet.isDisponivel(temporada) && carro.getImg() != null) {
            int idx = CarregadorRecursos.indiceTime(temporada, CarregadorRecursos.extrairTime(carro.getImg()));
            if (idx >= 0) {
                return SpriteSheet.getCarroCima(temporada, idx);
            }
        }
        return null;
    }

    /** Spinner compacto (rótulo curto + campo numérico 100-999), pra caber vários numa linha só. */
    private void adicionarSpinner(JPanel card, int x, int y, int w, String label, int valorInicial,
                                   java.util.function.IntConsumer onMudanca) {
        int lblW = 26;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.LIGHT_GRAY);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lbl.setBounds(x, y + 3, lblW, 16);
        card.add(lbl);

        int valorTravado = Math.max(100, Math.min(999, valorInicial));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(valorTravado, 100, 999, 1));
        spinner.setBounds(x + lblW, y, w - lblW, 22);
        spinner.addChangeListener(e -> onMudanca.accept((int) spinner.getValue()));
        card.add(spinner);
    }

    private JButton criarBotaoCor(String label, Color cor, java.util.function.Consumer<Color> onMudanca) {
        JButton btn = new JButton(label);
        btn.setBackground(cor);
        btn.setForeground(luminancia(cor) > 128 ? Color.BLACK : Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addActionListener(e -> {
            Color nova = JColorChooser.showDialog(this, "Escolha " + label, btn.getBackground());
            if (nova != null) {
                btn.setBackground(nova);
                btn.setForeground(luminancia(nova) > 128 ? Color.BLACK : Color.WHITE);
                onMudanca.accept(nova);
            }
        });
        return btn;
    }

    private int luminancia(Color c) {
        return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
    }

    /**
     * Botão de salvar com um ícone de disquete desenhado em código — em vez
     * de um emoji no texto do botão, que não renderiza de forma confiável em
     * toda fonte/plataforma Swing (apareceu em branco em teste do usuário).
     */
    private JButton criarBotaoSalvar() {
        JButton btn = new JButton(new IconeDisquete());
        btn.setToolTipText("Salvar");
        return btn;
    }

    /** Ícone de disquete simples, desenhado em Graphics2D — sem depender de fonte/emoji. */
    static class IconeDisquete implements javax.swing.Icon {
        private static final int TAMANHO = 16;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);

            g2.setColor(new Color(230, 230, 230));
            g2.fillRoundRect(0, 0, TAMANHO, TAMANHO, 2, 2);
            g2.setColor(new Color(60, 60, 60));
            g2.drawRoundRect(0, 0, TAMANHO - 1, TAMANHO - 1, 2, 2);

            // aba superior direita dobrada (canto característico do disquete)
            g2.fillRect(TAMANHO - 5, 0, 5, 5);

            // rótulo (retângulo branco na metade de cima)
            g2.setColor(Color.WHITE);
            g2.fillRect(3, 2, TAMANHO - 8, 5);

            // abertura metálica (retângulo escuro na metade de baixo)
            g2.setColor(new Color(60, 60, 60));
            g2.fillRect(3, 9, TAMANHO - 6, 5);
            g2.setColor(new Color(200, 200, 200));
            g2.fillRect(4, 10, TAMANHO - 8, 3);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return TAMANHO;
        }

        @Override
        public int getIconHeight() {
            return TAMANHO;
        }
    }

    // ── salvar individual: carro ───────────────────────────────────────────────
    private void salvarEntryCarro(CarroEntry entry) {
        try {
            salvarCarroNoArquivo(arquivoCarros(entry.temporada), List.of(entry));
            boolean propagou = false;
            if (entry.propagarHomenagem && entry.novoNomeHomenagem != null
                    && entry.nomeHomenagemCarregado != null && !entry.nomeHomenagemCarregado.isEmpty()) {
                propagarNomeHomenagemCarro(entry.temporada, entry.chaveOriginal,
                        entry.nomeHomenagemCarregado, entry.novoNomeHomenagem);
                entry.nomeHomenagemCarregado = entry.novoNomeHomenagem;
                propagou = true;
            }
            entry.modificado = false;
            entry.chaveOriginal = entry.carro.getNomeOriginal();
            String msg = entry.carro.getNomeOriginal() + " salvo."
                    + (propagou ? " Aplicado a todas as temporadas." : "");
            JOptionPane.showMessageDialog(this, msg, "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── salvar individual: piloto ──────────────────────────────────────────────
    private void salvarEntryPiloto(PilotoEntry entry) {
        try {
            salvarPilotoNoArquivo(arquivoPilotos(entry.temporada), List.of(entry));
            if (entry.propagarHomenagem && entry.novoNomeHomenagem != null) {
                propagarNomeHomenagemPiloto(entry.temporada, entry.chaveOriginal, entry.novoNomeHomenagem);
            }
            entry.modificado = false;
            JOptionPane.showMessageDialog(this, entry.piloto.getNomeOriginal() + " salvo.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── salvar todos (temporada + modo atual) ─────────────────────────────────
    private void salvarTodos() {
        try {
            if (modoAtual == Modo.CARROS) {
                for (CarroEntry entry : entradasCarros) {
                    if (!entry.modificado) continue;
                    salvarCarroNoArquivo(arquivoCarros(entry.temporada), List.of(entry));
                    if (entry.propagarHomenagem && entry.novoNomeHomenagem != null
                            && entry.nomeHomenagemCarregado != null && !entry.nomeHomenagemCarregado.isEmpty()) {
                        propagarNomeHomenagemCarro(entry.temporada, entry.chaveOriginal,
                                entry.nomeHomenagemCarregado, entry.novoNomeHomenagem);
                        entry.nomeHomenagemCarregado = entry.novoNomeHomenagem;
                    }
                    entry.modificado = false;
                }
            } else {
                for (PilotoEntry entry : entradasPilotos) {
                    if (!entry.modificado) continue;
                    salvarPilotoNoArquivo(arquivoPilotos(temporadaAtual), List.of(entry));
                    if (entry.propagarHomenagem && entry.novoNomeHomenagem != null) {
                        propagarNomeHomenagemPiloto(entry.temporada, entry.chaveOriginal, entry.novoNomeHomenagem);
                    }
                    entry.modificado = false;
                }
            }
            JOptionPane.showMessageDialog(this, "Temporada " + temporadaAtual + " salva.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private File arquivoCarros(String temporada) {
        return new File("src/main/resources/properties/" + temporada + "/carros.properties");
    }

    private File arquivoPilotos(String temporada) {
        return new File("src/main/resources/properties/" + temporada + "/pilotos.properties");
    }

    /**
     * Lê carros.properties, atualiza cor1/cor2/potencia/aero/nomeHomenagem
     * pros entries fornecidos e reescreve o arquivo mantendo comentários e
     * ordem original.
     */
    private void salvarCarroNoArquivo(File arquivo, List<CarroEntry> lista) throws Exception {
        if (!arquivo.isFile()) {
            throw new IOException("Arquivo não encontrado em " + arquivo.getPath()
                    + " — execute a ferramenta a partir da raiz do projeto.");
        }
        List<String> linhas = new ArrayList<>(java.nio.file.Files.readAllLines(arquivo.toPath(), StandardCharsets.ISO_8859_1));

        Map<String, CarroEntry> mapa = new LinkedHashMap<>();
        for (CarroEntry e : lista) mapa.put(e.chaveOriginal, e);

        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i).trim();
            if (linha.startsWith("#") || linha.isEmpty()) continue;
            int eq = linha.indexOf('=');
            if (eq < 0) continue;
            String chave = linha.substring(0, eq).trim();
            CarroEntry entry = mapa.get(chave);
            if (entry == null) continue;

            String valorAtual = linha.substring(eq + 1).trim();
            String[] cols = valorAtual.split(",", -1);
            if (cols.length >= 8) {
                Color c1 = entry.carro.getCor1();
                Color c2 = entry.carro.getCor2();
                cols[0] = String.valueOf(entry.carro.getPotencia());
                cols[1] = String.valueOf(c1.getRed());
                cols[2] = String.valueOf(c1.getGreen());
                cols[3] = String.valueOf(c1.getBlue());
                cols[5] = String.valueOf(c2.getRed());
                cols[6] = String.valueOf(c2.getGreen());
                cols[7] = String.valueOf(c2.getBlue());
                if (cols.length > 8) cols[8] = String.valueOf(entry.carro.getAerodinamica());
                if (cols.length > 9) cols[9] = String.valueOf(entry.carro.getFreios());
                String novoNome = entry.novoNomeHomenagem != null ? entry.novoNomeHomenagem : entry.carro.getNomeHomenagem();
                if (cols.length > 10) {
                    cols[10] = novoNome == null ? "" : novoNome;
                } else {
                    cols = appendCampo(cols, novoNome == null ? "" : novoNome);
                }
                linhas.set(i, chave + "=" + String.join(",", cols));
            }
        }

        java.nio.file.Files.write(arquivo.toPath(), linhas, StandardCharsets.ISO_8859_1);
    }

    /**
     * Lê pilotos.properties, atualiza habilidade/nomeHomenagem pros entries
     * fornecidos e reescreve o arquivo mantendo comentários e ordem original.
     */
    private void salvarPilotoNoArquivo(File arquivo, List<PilotoEntry> lista) throws Exception {
        if (!arquivo.isFile()) {
            throw new IOException("Arquivo não encontrado em " + arquivo.getPath()
                    + " — execute a ferramenta a partir da raiz do projeto.");
        }
        List<String> linhas = new ArrayList<>(java.nio.file.Files.readAllLines(arquivo.toPath(), StandardCharsets.ISO_8859_1));

        Map<String, PilotoEntry> mapa = new LinkedHashMap<>();
        for (PilotoEntry e : lista) mapa.put(e.chaveOriginal, e);

        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i).trim();
            if (linha.startsWith("#") || linha.isEmpty()) continue;
            int eq = linha.indexOf('=');
            if (eq < 0) continue;
            String chave = linha.substring(0, eq).trim();
            PilotoEntry entry = mapa.get(chave);
            if (entry == null) continue;

            String valorAtual = linha.substring(eq + 1).trim();
            String[] cols = valorAtual.split(",", -1);
            if (cols.length >= 2) {
                if (entry.novaHabilidade != null) {
                    cols[1] = String.valueOf(entry.novaHabilidade);
                }
                String novoNome = entry.novoNomeHomenagem != null ? entry.novoNomeHomenagem : entry.piloto.getNomeHomenagem();
                if (cols.length > 2) {
                    cols[2] = novoNome == null ? "" : novoNome;
                } else {
                    cols = appendCampo(cols, novoNome == null ? "" : novoNome);
                }
                linhas.set(i, chave + "=" + String.join(",", cols));
            }
        }

        java.nio.file.Files.write(arquivo.toPath(), linhas, StandardCharsets.ISO_8859_1);
    }

    private String[] appendCampo(String[] cols, String novoValor) {
        String[] novo = new String[cols.length + 1];
        System.arraycopy(cols, 0, novo, 0, cols.length);
        novo[cols.length] = novoValor;
        return novo;
    }

    // ── propagação pra todas as temporadas ────────────────────────────────────

    /**
     * Varre carros.properties de todas as temporadas e atualiza nomeHomenagem
     * de toda linha cujo nomeHomenagem (antes desta edição) era igual ao que
     * a linha editada tinha antes de ser salva — ver design.md decisão 7.
     */
    private void propagarNomeHomenagemCarro(String temporadaJaSalva, String chaveJaSalva,
                                             String nomeAntesDaEdicao, String novoNome) throws Exception {
        File dir = new File("src/main/resources/properties");
        File[] temporadasDirs = dir.listFiles(f -> f.isDirectory() && f.getName().startsWith("t"));
        if (temporadasDirs == null) return;
        for (File temporadaDir : temporadasDirs) {
            File arquivo = new File(temporadaDir, "carros.properties");
            if (!arquivo.isFile()) continue;
            boolean mesmaTemporadaJaSalva = temporadaDir.getName().equals(temporadaJaSalva);
            List<String> linhas = new ArrayList<>(java.nio.file.Files.readAllLines(arquivo.toPath(), StandardCharsets.ISO_8859_1));
            boolean alterou = false;
            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i).trim();
                if (linha.startsWith("#") || linha.isEmpty()) continue;
                int eq = linha.indexOf('=');
                if (eq < 0) continue;
                String chave = linha.substring(0, eq).trim();
                if (mesmaTemporadaJaSalva && chave.equals(chaveJaSalva)) continue; // já salva individualmente
                String[] cols = linha.substring(eq + 1).split(",", -1);
                if (cols.length <= 10) continue;
                if (!nomeAntesDaEdicao.equals(cols[10].trim())) continue;
                cols[10] = novoNome;
                linhas.set(i, chave + "=" + String.join(",", cols));
                alterou = true;
            }
            if (alterou) {
                java.nio.file.Files.write(arquivo.toPath(), linhas, StandardCharsets.ISO_8859_1);
            }
        }
    }

    /**
     * Varre pilotos.properties de todas as temporadas e atualiza
     * nomeHomenagem de toda linha cuja chave real seja igual à do piloto
     * editado.
     */
    private void propagarNomeHomenagemPiloto(String temporadaJaSalva, String chaveReal, String novoNome) throws Exception {
        File dir = new File("src/main/resources/properties");
        File[] temporadasDirs = dir.listFiles(f -> f.isDirectory() && f.getName().startsWith("t"));
        if (temporadasDirs == null) return;
        for (File temporadaDir : temporadasDirs) {
            File arquivo = new File(temporadaDir, "pilotos.properties");
            if (!arquivo.isFile()) continue;
            boolean mesmaTemporadaJaSalva = temporadaDir.getName().equals(temporadaJaSalva);
            List<String> linhas = new ArrayList<>(java.nio.file.Files.readAllLines(arquivo.toPath(), StandardCharsets.ISO_8859_1));
            boolean alterou = false;
            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i).trim();
                if (linha.startsWith("#") || linha.isEmpty()) continue;
                int eq = linha.indexOf('=');
                if (eq < 0) continue;
                String chave = linha.substring(0, eq).trim();
                if (!chave.equals(chaveReal)) continue;
                if (mesmaTemporadaJaSalva) continue; // já salva individualmente
                String[] cols = linha.substring(eq + 1).split(",", -1);
                if (cols.length > 2) {
                    cols[2] = novoNome;
                } else {
                    cols = appendCampo(cols, novoNome);
                }
                linhas.set(i, chave + "=" + String.join(",", cols));
                alterou = true;
            }
            if (alterou) {
                java.nio.file.Files.write(arquivo.toPath(), linhas, StandardCharsets.ISO_8859_1);
            }
        }
    }

    // ── listener simplificado de DocumentEvent ────────────────────────────────
    private javax.swing.event.DocumentListener simpleDocListener(Runnable onChange) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        };
    }

    // ── canvas de preview (le cores via supplier, pra funcionar tanto com
    //    entry.carro mutavel quanto com cores fixas somente-leitura) ──────────
    static class PreviewCanvas extends JPanel {
        private final java.util.function.Supplier<Color> cor1Supplier;
        private final java.util.function.Supplier<Color> cor2Supplier;

        PreviewCanvas(java.util.function.Supplier<Color> cor1Supplier, java.util.function.Supplier<Color> cor2Supplier) {
            this.cor1Supplier = cor1Supplier;
            this.cor2Supplier = cor2Supplier;
            setBackground(new Color(20, 20, 20));
            setPreferredSize(new Dimension(PREVIEW_LADO_W + PREVIEW_CIMA_W + PREVIEW_CAP_W + 20, 60));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color c1 = cor1Supplier.get();
            Color c2 = cor2Supplier.get();

            BufferedImage lado = CarregadorRecursos.pintarModeloV2(
                    "png/carro-lado-v2.png", c1, c2, PREVIEW_LADO_W, PREVIEW_LADO_H);
            int yLado = (getHeight() - PREVIEW_LADO_H) / 2;
            g2.drawImage(lado, 0, yLado, null);

            BufferedImage cima = CarregadorRecursos.pintarModeloV2(
                    "png/carro-cima-v2.png", c1, c2, PREVIEW_CIMA_W, PREVIEW_CIMA_H);
            int xCima = PREVIEW_LADO_W + 8;
            int yCima = (getHeight() - PREVIEW_CIMA_H) / 2;
            g2.drawImage(cima, xCima, yCima, null);

            BufferedImage cap = CarregadorRecursos.pintarModeloV2(
                    "png/capacete-v2.png", c1, c2, PREVIEW_CAP_W, PREVIEW_CAP_H);
            int xCap = xCima + PREVIEW_CIMA_W + 8;
            int yCap = (getHeight() - PREVIEW_CAP_H) / 2;
            g2.drawImage(cap, xCap, yCap, null);

            g2.dispose();
        }
    }

    // ── canvas de preview de uma única imagem (capacete do sprite, sprite
    //    original de referência, etc.) — centralizada, sem escalar ─────────────
    static class ImagemCanvas extends JPanel {
        private final java.util.function.Supplier<BufferedImage> imagemSupplier;

        ImagemCanvas(java.util.function.Supplier<BufferedImage> imagemSupplier, int w, int h) {
            this.imagemSupplier = imagemSupplier;
            setBackground(new Color(20, 20, 20));
            setPreferredSize(new Dimension(w, h));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage img = imagemSupplier.get();
            if (img == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // escala mantendo proporção, encaixando dentro do canvas
            double escala = Math.min((double) getWidth() / img.getWidth(), (double) getHeight() / img.getHeight());
            int w = (int) (img.getWidth() * escala);
            int h = (int) (img.getHeight() * escala);
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g2.drawImage(img, x, y, w, h, null);
            g2.dispose();
        }
    }

    // ── FlowLayout que quebra em múltiplas linhas ─────────────────────────────
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int width = target.getWidth();
                if (width == 0) width = Integer.MAX_VALUE;
                Insets ins = target.getInsets();
                int maxW = width - ins.left - ins.right;
                int rowW = 0, rowH = 0, totalH = ins.top + ins.bottom;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (rowW + d.width > maxW && rowW > 0) {
                        totalH += rowH + getVgap();
                        rowW = 0; rowH = 0;
                    }
                    rowW += d.width + getHgap();
                    rowH = Math.max(rowH, d.height);
                }
                totalH += rowH;
                return new Dimension(width, totalH);
            }
        }
    }

    // ── dados de um carro ─────────────────────────────────────────────────────
    static class CarroEntry {
        final Carro carro;
        String chaveOriginal;
        final String temporada;
        boolean modificado = false;
        boolean propagarHomenagem = false;
        String novoNomeHomenagem;
        /**
         * nomeHomenagem que este carro tinha quando a temporada foi carregada
         * nesta sessão do editor (ou o valor após a última propagação bem
         * sucedida) — usado como âncora da propagação em vez de reler do
         * disco a cada save. Reler do disco quebra depois do 1º save sem
         * propagação: o valor "antigo" já foi sobrescrito só nesta
         * temporada, então nenhum save seguinte encontra mais o valor que as
         * outras temporadas ainda têm. Só é atualizada quando a propagação
         * de fato ocorre (nesse momento todas as temporadas convergem pro
         * novo valor); um save individual sem propagar NÃO atualiza a
         * âncora, senão a propagação de uma edição seguinte perderia a
         * referência do valor que as outras temporadas ainda têm.
         */
        String nomeHomenagemCarregado;
        PreviewCanvas canvas;
        JButton btnCor1, btnCor2;

        CarroEntry(Carro carro, String chaveOriginal, String temporada) {
            this.carro = carro;
            this.chaveOriginal = chaveOriginal;
            this.temporada = temporada;
            this.nomeHomenagemCarregado = carro.getNomeHomenagem();
        }
    }

    // ── dados de um piloto ────────────────────────────────────────────────────
    static class PilotoEntry {
        final Piloto piloto;
        final String chaveOriginal;
        final String temporada;
        boolean modificado = false;
        boolean propagarHomenagem = false;
        String novoNomeHomenagem;
        Integer novaHabilidade;

        PilotoEntry(Piloto piloto, String chaveOriginal, String temporada) {
            this.piloto = piloto;
            this.chaveOriginal = chaveOriginal;
            this.temporada = temporada;
        }
    }

    // ── entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new EditorCarrosPilotos().setVisible(true);
        });
    }
}

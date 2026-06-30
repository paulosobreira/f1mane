package br.f1mane.editor;

import br.f1mane.entidades.Carro;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.SpriteSheet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * Utilitário visual para editar as cores (cor1 / cor2) de cada carro de uma temporada.
 * Lê e salva diretamente no carros.properties da temporada selecionada.
 *
 * Uso: execute a partir da raiz do projeto (working directory), pois grava
 * diretamente em src/main/resources/properties — mesmo padrão de SpriteSheet.main.
 * java -cp target/classes br.f1mane.editor.EditorCoresCarros
 */
public class EditorCoresCarros extends JFrame {

    private static final int PREVIEW_CIMA_W  = 90;
    private static final int PREVIEW_CIMA_H  = 90;
    private static final int PREVIEW_LADO_W  = 180;
    private static final int PREVIEW_LADO_H  = 40;
    private static final int PREVIEW_CAP_W   = 55;
    private static final int PREVIEW_CAP_H   = 55;
    private static final int CARD_W          = 340;
    private static final int CARD_H          = 258;

    // ── estado ───────────────────────────────────────────────────────────────
    private String temporadaAtual;
    private List<CarroEntry> entradas = new ArrayList<>();
    private final List<String> temporadas = new ArrayList<>();
    private int indiceTemporada = -1;

    // ── componentes principais ────────────────────────────────────────────────
    private final JLabel lblTemporada = new JLabel();
    private final JButton btnTemporadaAnterior = new JButton("◀ Anterior");
    private final JButton btnTemporadaProxima = new JButton("Próxima ▶");
    private final JPanel painelCarros = new JPanel();
    private final JScrollPane scroll;

    public EditorCoresCarros() {
        super("Editor de Cores de Carros — FlMane");
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

        JButton btnSalvarTodos = new JButton("Salvar Todos");
        btnSalvarTodos.addActionListener(e -> salvarTodos());
        topo.add(btnSalvarTodos);

        // ── painel de carros ──────────────────────────────────────────────────
        painelCarros.setLayout(new GridLayout(0, 4, 12, 12));
        painelCarros.setBorder(new EmptyBorder(8, 8, 8, 8));
        scroll = new JScrollPane(painelCarros,
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

    // ── carregar carros da temporada ──────────────────────────────────────────
    private void carregarTemporada(String temporada) {
        if (temporada == null) return;
        temporadaAtual = temporada;
        entradas.clear();
        painelCarros.removeAll();

        try {
            CarregadorRecursos cr = CarregadorRecursos.getCarregadorRecursos(false);
            List<Carro> carros = cr.carregarListaCarros(temporada);
            carros.sort(Comparator.comparingInt((Carro c) ->
                    (c.getPotencia() + c.getAerodinamica() + c.getFreios()) / 3).reversed());

            // ler o arquivo de propriedades para guardar todas as linhas originais
            Properties props = new Properties();
            InputStream is = CarregadorRecursos.recursoComoStream("properties/" + temporada + "/carros.properties");
            if (is != null) props.load(is);

            for (Carro carro : carros) {
                // encontrar a chave original (sem substVogais) comparando nome decodificado
                String chaveOriginal = encontrarChave(props, carro.getNome());
                CarroEntry entry = new CarroEntry(carro, chaveOriginal, temporada);
                entradas.add(entry);
                painelCarros.add(criarCartao(entry));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar temporada: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        painelCarros.revalidate();
        painelCarros.repaint();
    }

    // carros.properties usa Util.substVogais(nome) como chave — precisamos da chave real
    private String encontrarChave(Properties props, String nomeSubst) {
        for (String k : props.stringPropertyNames()) {
            if (br.nnpe.Util.substVogais(k).equals(nomeSubst)) return k;
        }
        return nomeSubst;
    }

    // ── cartão visual de um carro ─────────────────────────────────────────────
    private JPanel criarCartao(CarroEntry entry) {
        JPanel card = new JPanel(null); // layout absoluto para posicionamento livre
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                new EmptyBorder(6, 6, 6, 6)));
        card.setBackground(new Color(30, 30, 30));

        // nome do carro
        JLabel lblNome = new JLabel(entry.carro.getNome());
        lblNome.setForeground(Color.WHITE);
        lblNome.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblNome.setBounds(6, 4, CARD_W - 12, 18);
        card.add(lblNome);

        // canvas de preview (lado + cima + cap)
        PreviewCanvas canvas = new PreviewCanvas(entry);
        canvas.setBounds(6, 26, PREVIEW_LADO_W + PREVIEW_CIMA_W + PREVIEW_CAP_W + 16, PREVIEW_LADO_H + 10);
        card.add(canvas);
        entry.canvas = canvas;

        // botões de cor
        JButton btnCor1 = criarBotaoCor("Cor 1", entry.carro.getCor1(), c -> {
            entry.carro.setCor1(c);
            entry.modificado = true;
            canvas.repaint();
        });
        // linha 1: botões de cor (metade da largura cada)
        int btnW = (CARD_W - 20) / 2;
        btnCor1.setBounds(6, 86, btnW, 26);
        card.add(btnCor1);
        entry.btnCor1 = btnCor1;

        JButton btnCor2 = criarBotaoCor("Cor 2", entry.carro.getCor2(), c -> {
            entry.carro.setCor2(c);
            entry.modificado = true;
            canvas.repaint();
        });
        btnCor2.setBounds(btnW + 14, 86, btnW, 26);
        card.add(btnCor2);
        entry.btnCor2 = btnCor2;

        // linha 2: editar e salvar
        JButton btnEditar = new JButton("Editar");
        btnEditar.setBounds(6, 118, btnW, 26);
        btnEditar.addActionListener(e -> abrirEditor(entry));
        card.add(btnEditar);

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBounds(btnW + 14, 118, btnW, 26);
        btnSalvar.addActionListener(e -> salvarEntry(entry));
        card.add(btnSalvar);

        // sliders: potência, aero, freios
        int y = 152;
        adicionarSlider(card, entry, "Pot.", entry.carro.getPotencia(),
                v -> { entry.carro.setPotencia(v); entry.carro.setPotenciaReal(v); entry.modificado = true; }, y);
        adicionarSlider(card, entry, "Aero", entry.carro.getAerodinamica(),
                v -> { entry.carro.setAerodinamica(v); entry.modificado = true; }, y + 26);
        adicionarSlider(card, entry, "Fre.", entry.carro.getFreios(),
                v -> { entry.carro.setFreios(v); entry.modificado = true; }, y + 52);

        return card;
    }

    private void adicionarSlider(JPanel card, CarroEntry entry, String label, int valorInicial,
                                  java.util.function.IntConsumer onMudanca, int y) {
        int lblW = 32, valW = 38, gap = 4;
        int sliderW = CARD_W - 20 - lblW - valW - gap * 2;

        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.LIGHT_GRAY);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setBounds(6, y + 3, lblW, 18);
        card.add(lbl);

        JSlider slider = new JSlider(100, 999, Math.max(100, Math.min(999, valorInicial)));
        slider.setBounds(6 + lblW + gap, y, sliderW, 24);
        slider.setBackground(new Color(30, 30, 30));
        card.add(slider);

        JLabel valLabel = new JLabel(String.valueOf(valorInicial));
        valLabel.setForeground(Color.WHITE);
        valLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        valLabel.setBounds(6 + lblW + gap + sliderW + gap, y + 3, valW, 18);
        card.add(valLabel);

        slider.addChangeListener(e -> {
            int v = slider.getValue();
            valLabel.setText(String.valueOf(v));
            onMudanca.accept(v);
        });
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

    // ── diálogo de edição detalhada ───────────────────────────────────────────
    private void abrirEditor(CarroEntry entry) {
        JDialog dlg = new JDialog(this, "Editar: " + entry.carro.getNome(), true);
        dlg.setSize(520, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        // campos RGB
        JPanel campos = new JPanel(new GridLayout(2, 4, 8, 8));
        campos.setBorder(new TitledBorder("Valores RGB"));

        Color c1 = entry.carro.getCor1();
        Color c2 = entry.carro.getCor2();

        JSpinner[] r1g1b1 = criarSpinners(c1.getRed(), c1.getGreen(), c1.getBlue());
        JSpinner[] r2g2b2 = criarSpinners(c2.getRed(), c2.getGreen(), c2.getBlue());

        campos.add(new JLabel("Cor 1 R:"));
        campos.add(r1g1b1[0]);
        campos.add(new JLabel("G:"));
        campos.add(r1g1b1[1]);
        campos.add(new JLabel("Cor 1 B:"));
        campos.add(r1g1b1[2]);
        campos.add(new JLabel(""));
        campos.add(new JLabel(""));

        campos.add(new JLabel("Cor 2 R:"));
        campos.add(r2g2b2[0]);
        campos.add(new JLabel("G:"));
        campos.add(r2g2b2[1]);
        campos.add(new JLabel("Cor 2 B:"));
        campos.add(r2g2b2[2]);
        campos.add(new JLabel(""));
        campos.add(new JLabel(""));

        // preview ao vivo dentro do diálogo
        PreviewCanvas previewDlg = new PreviewCanvas(entry);
        previewDlg.setPreferredSize(new Dimension(PREVIEW_LADO_W + PREVIEW_CIMA_W + PREVIEW_CAP_W + 24, 120));

        ChangeListener<Object> atualizar = ev -> {
            try {
                Color nc1 = new Color(
                        (int) r1g1b1[0].getValue(), (int) r1g1b1[1].getValue(), (int) r1g1b1[2].getValue());
                Color nc2 = new Color(
                        (int) r2g2b2[0].getValue(), (int) r2g2b2[1].getValue(), (int) r2g2b2[2].getValue());
                entry.carro.setCor1(nc1);
                entry.carro.setCor2(nc2);
                // limpar cache para que a próxima renderização re-pinte
                CarregadorRecursos.invalidarCacheModeloV2();
                previewDlg.repaint();
                entry.modificado = true;
            } catch (IllegalArgumentException ignored) {}
        };

        for (JSpinner s : r1g1b1) s.addChangeListener(atualizar);
        for (JSpinner s : r2g2b2) s.addChangeListener(atualizar);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancelar = new JButton("Cancelar");
        botoes.add(cancelar);
        botoes.add(ok);

        ok.addActionListener(e -> {
            if (entry.canvas != null) entry.canvas.repaint();
            if (entry.btnCor1 != null) {
                entry.btnCor1.setBackground(entry.carro.getCor1());
                entry.btnCor1.setForeground(luminancia(entry.carro.getCor1()) > 128 ? Color.BLACK : Color.WHITE);
            }
            if (entry.btnCor2 != null) {
                entry.btnCor2.setBackground(entry.carro.getCor2());
                entry.btnCor2.setForeground(luminancia(entry.carro.getCor2()) > 128 ? Color.BLACK : Color.WHITE);
            }
            dlg.dispose();
        });
        cancelar.addActionListener(e -> dlg.dispose());

        dlg.add(campos, BorderLayout.NORTH);
        dlg.add(previewDlg, BorderLayout.CENTER);
        dlg.add(botoes, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JSpinner[] criarSpinners(int r, int g, int b) {
        SpinnerNumberModel mr = new SpinnerNumberModel(r, 0, 255, 1);
        SpinnerNumberModel mg = new SpinnerNumberModel(g, 0, 255, 1);
        SpinnerNumberModel mb = new SpinnerNumberModel(b, 0, 255, 1);
        return new JSpinner[]{new JSpinner(mr), new JSpinner(mg), new JSpinner(mb)};
    }

    // ── salvar individual ─────────────────────────────────────────────────────
    private void salvarEntry(CarroEntry entry) {
        try {
            salvarNoArquivo(List.of(entry));
            entry.modificado = false;
            JOptionPane.showMessageDialog(this, entry.carro.getNome() + " salvo.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── salvar todos ──────────────────────────────────────────────────────────
    private void salvarTodos() {
        try {
            salvarNoArquivo(entradas);
            entradas.forEach(e -> e.modificado = false);
            JOptionPane.showMessageDialog(this, "Temporada " + temporadaAtual + " salva.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Lê o arquivo de propriedades, atualiza as colunas cor1/cor2 para os entries
     * fornecidos e reescreve o arquivo mantendo comentários e ordem original.
     */
    private void salvarNoArquivo(List<CarroEntry> lista) throws Exception {
        // grava direto no recurso-fonte; assume working directory = raiz do projeto (ver Javadoc da classe)
        File arquivo = new File("src/main/resources/properties/" + temporadaAtual + "/carros.properties");
        if (!arquivo.isFile()) {
            throw new IOException("Arquivo não encontrado em " + arquivo.getPath()
                    + " — execute a ferramenta a partir da raiz do projeto.");
        }
        List<String> linhas = new ArrayList<>(java.nio.file.Files.readAllLines(arquivo.toPath(), StandardCharsets.ISO_8859_1));

        // montar mapa de chave → entry para lookup rápido
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
            // formato: potencia,cor1r,cor1g,cor1b,img,cor2r,cor2g,cor2b[,aero,freios]
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
                linhas.set(i, chave + "=" + String.join(",", cols));
            }
        }

        java.nio.file.Files.write(arquivo.toPath(), linhas, StandardCharsets.ISO_8859_1);
    }

    // ── canvas de preview ─────────────────────────────────────────────────────
    static class PreviewCanvas extends JPanel {
        private final CarroEntry entry;

        PreviewCanvas(CarroEntry entry) {
            this.entry = entry;
            setBackground(new Color(20, 20, 20));
            setPreferredSize(new Dimension(PREVIEW_LADO_W + PREVIEW_CIMA_W + PREVIEW_CAP_W + 20, 60));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color c1 = entry.carro.getCor1();
            Color c2 = entry.carro.getCor2();

            // lado
            BufferedImage lado = CarregadorRecursos.pintarModeloV2(
                    "png/carro-lado-v2.png", c1, c2, PREVIEW_LADO_W, PREVIEW_LADO_H);
            int yLado = (getHeight() - PREVIEW_LADO_H) / 2;
            g2.drawImage(lado, 0, yLado, null);

            // cima
            BufferedImage cima = CarregadorRecursos.pintarModeloV2(
                    "png/carro-cima-v2.png", c1, c2, PREVIEW_CIMA_W, PREVIEW_CIMA_H);
            int xCima = PREVIEW_LADO_W + 8;
            int yCima = (getHeight() - PREVIEW_CIMA_H) / 2;
            g2.drawImage(cima, xCima, yCima, null);

            // capacete
            BufferedImage cap = CarregadorRecursos.pintarModeloV2(
                    "png/capacete-v2.png", c1, c2, PREVIEW_CAP_W, PREVIEW_CAP_H);
            int xCap = xCima + PREVIEW_CIMA_W + 8;
            int yCap = (getHeight() - PREVIEW_CAP_H) / 2;
            g2.drawImage(cap, xCap, yCap, null);

            g2.dispose();
        }
    }

    // ── wrapper para ChangeListener genérico ──────────────────────────────────
    @FunctionalInterface
    interface ChangeListener<T> extends javax.swing.event.ChangeListener {
        void onChange(T event);
        @Override
        default void stateChanged(javax.swing.event.ChangeEvent e) {
            onChange(null);
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
        final String chaveOriginal;
        final String temporada;
        boolean modificado = false;
        PreviewCanvas canvas;
        JButton btnCor1, btnCor2;

        CarroEntry(Carro carro, String chaveOriginal, String temporada) {
            this.carro = carro;
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
            new EditorCoresCarros().setVisible(true);
        });
    }
}

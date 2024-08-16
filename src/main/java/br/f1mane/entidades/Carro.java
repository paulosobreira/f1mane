package br.f1mane.entidades;

import br.f1mane.controles.InterfaceJogo;
import br.f1mane.recursos.idiomas.Lang;
import br.nnpe.Html;
import br.nnpe.Util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

/**
 * @author Paulo Sobreira Criado em 06/05/2007 as 11:09:15
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Carro implements Serializable {
    private static final long serialVersionUID = -181518724082829223L;
    public final static String TIPO_PNEU_MOLE = "TIPO_PNEU_MOLE";
    public final static String TIPO_PNEU_DURO = "TIPO_PNEU_DURO";
    public final static String TIPO_PNEU_CHUVA = "TIPO_PNEU_CHUVA";
    public final static String PNEU_FURADO = "PNEU_FURADO";
    public final static String PERDEU_AEREOFOLIO = "PERDEU_AEREOFOLIO";
    public static final String BATEU_FORTE = "BATEU_FORTE";
    public static final String PANE_SECA = "PANE_SECA";
    public static final String ABANDONOU = "ABANDONOU";
    public static final String EXPLODIU_MOTOR = "EXPLODIU_MOTOR";
    public static final String GIRO_MIN = "GIRO_MIN";
    public static final String GIRO_NOR = "GIRO_NOR";
    public static final String GIRO_MAX = "GIRO_MAX";
    public static final int GIRO_MIN_VAL = 1;
    public static final int GIRO_NOR_VAL = 5;
    public static final int GIRO_MAX_VAL = 9;
    public static final String MAIS_ASA = "MAIS_ASA";
    public static final String ASA_NORMAL = "ASA_NORMAL";
    public static final String MENOS_ASA = "MENOS_ASA";
    public static final int LARGURA = 62;
    public static final int LARGURA_CIMA = 86;
    public static final int ALTURA = 24;
    public static final int ALTURA_CIMA = 86;
    public static final int MEIA_LARGURA = 31;
    public static final int MEIA_LARGURA_CIMA = 43;
    public static final int MEIA_ALTURA = 12;
    public static final int MEIA_ALTURA_CIMA = 43;
    public static final double FATOR_AREA_CARRO = .7;
    public static final int RAIO_DERRAPAGEM = 155;
    private int porcentagemCombustivel;
    private int porcentagemDesgastePneus;
    private int porcentagemDesgasteMotor;
    private String tipoPneu;
    private int potencia;
    private int potenciaReal;
    private int aerodinamica;
    private int freios;
    private int id;
    private String nome;

    @JsonIgnore
    private String img;
    @JsonIgnore
    private int durabilidadeAereofolio;
    @JsonIgnore
    private int cargaErs;
    @JsonIgnore
    private String asa = ASA_NORMAL;
    @JsonIgnore
    private String danificado;
    @JsonIgnore
    private int giro = GIRO_NOR_VAL;
    @JsonIgnore
    private boolean paneSeca;
    @JsonIgnore
    private boolean recolhido;
    @JsonIgnore
    private int pneus;
    @JsonIgnore
    private int combustivel;
    @JsonIgnore
    private int motor;
    @JsonIgnore
    private int potenciaAntesQualify;
    @JsonIgnore
    private int aeroAntesQualify;
    @JsonIgnore
    private int freiosAntesQualify;
    @JsonIgnore
    private int tanqueCheio;
    @JsonIgnore
    private int durabilidadeMaxPneus;
    @JsonIgnore
    private int temperaturaMotor;
    @JsonIgnore
    private int temperaturaPneus;
    @JsonIgnore
    private int durabilidadeMaxMotor;
    @JsonIgnore
    private int tempMax;
    @JsonIgnore
    private boolean pneuAquecido;
    @JsonIgnore
    private boolean msgPneu;
    @JsonIgnore
    private Color cor1;
    @JsonIgnore
    private Color cor2;
    @JsonIgnore
    private Piloto piloto;
    @JsonIgnore
    private boolean pontenciaErs;

    public String getCor1Hex() {
        return String.format("#%02x%02x%02x", Integer.valueOf(cor1.getRed()), Integer.valueOf(cor1.getGreen()), Integer.valueOf(cor1.getBlue()));
    }

    public String getCor2Hex() {
        return String.format("#%02x%02x%02x", Integer.valueOf(cor2.getRed()), Integer.valueOf(cor2.getGreen()), Integer.valueOf(cor2.getBlue()));
    }

    public int getPotenciaAntesQualify() {
        return potenciaAntesQualify;
    }

    public void setPotenciaAntesQualify(int potenciaAntesQualify) {
        this.potenciaAntesQualify = potenciaAntesQualify;
    }

    public int getCargaErs() {
        return cargaErs;
    }

    public void setCargaErs(int cargaErs) {
        this.cargaErs = cargaErs;
    }

    public int getGiro() {
        return giro;
    }

    @Override
    public String toString() {
        return nome;
    }

    public void setGiro(int giro) {
        this.giro = giro;
    }

    public boolean equals(Object arg0) {
        Carro carro = (Carro) arg0;
        if (carro == null) {
            return false;
        }
        return nome.equals(carro.getNome());
    }

    public int getMotor() {
        return motor;
    }

    public boolean isRecolhido() {
        return recolhido;
    }

    public void setRecolhido(boolean recolhido) {
        this.recolhido = recolhido;
    }

    public void setMotor(int motor) {
        this.motor = motor;
    }

    public int getDurabilidadeMaxMotor() {
        return durabilidadeMaxMotor;
    }

    public Color getCor2() {
        return cor2;
    }

    public void setCor2(Color cor2) {
        this.cor2 = cor2;
    }

    public void setDurabilidadeMaxMotor(int durabilidadeMaxMotor, int mediaPontecia) {
        if (this.durabilidadeMaxMotor != 0)
            return;
        if (mediaPontecia < 800) {
            mediaPontecia = 800;
        }
        this.durabilidadeMaxMotor = Util.inteiro(
                (durabilidadeMaxMotor * (mediaPontecia / 1000.0)) + (durabilidadeMaxMotor * (getPotencia() / 1000.0)));
        this.motor = this.durabilidadeMaxMotor;
    }

    public int hashCode() {
        return nome.hashCode();
    }

    public Piloto getPiloto() {
        return piloto;
    }

    public void setPiloto(Piloto piloto) {
        this.piloto = piloto;
    }

    public int getCombustivel() {
        return combustivel;
    }

    public int getDurabilidadeMaxPneus() {
        return durabilidadeMaxPneus;
    }

    public int getTanqueCheio() {
        return tanqueCheio;
    }

    public int getPotenciaReal() {
        return potenciaReal;
    }

    public void setPotenciaReal(int potenciaReal) {
        this.potenciaReal = potenciaReal;
    }

    public void setPotencia(int potencia) {
        this.potencia = potencia;
    }

    public void setTanqueCheio(int tanqueCheio) {
        this.tanqueCheio = tanqueCheio;
    }

    public void setCombustivel(int combustivel) {
        if (combustivel > tanqueCheio) {
            combustivel = tanqueCheio;
        }

        this.combustivel = combustivel;
    }

    public int getPneus() {
        return pneus;
    }

    public void setPneus(int pneus) {
        this.pneus = pneus;
    }

    public void setPorcentPneus(int porcent) {
        this.pneus = Util.inteiro(durabilidadeMaxPneus * (porcent / 100.0));
    }

    public void setDurabilidadeMaxPneus(int durabilidadeMaxPneus) {
        this.durabilidadeMaxPneus = durabilidadeMaxPneus;
    }

    public String getTipoPneu() {
        return tipoPneu;
    }

    public void trocarPneus(InterfaceJogo interfaceJogo, String tipoPneu, int distaciaCorrida) {
        msgPneu = false;
        if (interfaceJogo.isSemTrocaPneu() && this.getTipoPneu() != null) {
            if (!verificaPneusIncompativeisClima(interfaceJogo) && pneus > 0) {
                return;
            }
        }
        this.setTipoPneu(tipoPneu);

        if (Carro.TIPO_PNEU_DURO.equals(getTipoPneu())) {
            setPneuDuro(distaciaCorrida, interfaceJogo);
        } else {
            setPneuMoleOuChuva(distaciaCorrida, interfaceJogo);
        }
        if (Clima.CHUVA.equals(interfaceJogo.getClima())) {
            getPiloto().getCarro().setTemperaturaPneus(30);
        } else if (Clima.NUBLADO.equals(interfaceJogo.getClima())) {
            getPiloto().getCarro().setTemperaturaPneus(40);
        } else {
            getPiloto().getCarro().setTemperaturaPneus(50);
        }
    }

    public Color getCor1() {
        return cor1;
    }

    public void setCor1(Color cor) {
        this.cor1 = cor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getPotencia() {
        return potencia;
    }

    public boolean verificaCondicoesCautelaPneu(InterfaceJogo controleJogo) {
        int pneus = porcentagemDesgastePneus;
        double consumoMedioPenus = getPiloto().calculaConsumoMedioPneu();
        if (pneus < (consumoMedioPenus)) {
            return true;
        }
        if (Carro.TIPO_PNEU_MOLE.equals(getTipoPneu())) {
            return pneus < 10;
        } else {
            return pneus < 5;
        }
    }

    public boolean verificaCondicoesCautelaGiro(InterfaceJogo controleJogo) {
        int combust = porcentagemCombustivel;
        int motor = porcentagemDesgasteMotor;
        return (motor <= 5) || (combust <= (controleJogo.isSemReabastecimento() ? 5 : 3));
    }

    public boolean verificaPneusIncompativeisClima(InterfaceJogo controleJogo) {
        if (controleJogo.isChovendo() && !TIPO_PNEU_CHUVA.equals(getTipoPneu())) {
            return true;
        }
        return !controleJogo.isChovendo() && TIPO_PNEU_CHUVA.equals(getTipoPneu());
    }

    public boolean testePotencia() {
        return Math.random() < ((potencia + (pontenciaErs ? 200 : 0)) / 1000.0);
    }

    public boolean testeAerodinamica() {
        return Math.random() < (aerodinamica / 1000.0);
    }

    public boolean testeFreios(InterfaceJogo interfaceJogo) {
        if (verificaPneusIncompativeisClima(interfaceJogo)) {
            return false;
        }
        return Math.random() < (freios / 1000.0);
    }

    public void setPneuDuro(int distanciaCorrida, InterfaceJogo controleJogo) {
        if (controleJogo.isSemReabastecimento()) {
            pneus = Util.inteiro(distanciaCorrida);
        } else {
            pneus = Util.inteiro(distanciaCorrida * 0.7);
        }
        durabilidadeMaxPneus = pneus;
    }

    public void setPneuMoleOuChuva(int distaciaCorrida, InterfaceJogo controleJogo) {
        if (controleJogo.isSemReabastecimento()) {
            pneus = Util.inteiro(distaciaCorrida * 0.7);
        } else {
            pneus = Util.inteiro(distaciaCorrida * 0.5);
        }
        durabilidadeMaxPneus = pneus;
    }

    public double calcularModificadorCarro(double ganho, No no, InterfaceJogo controleJogo) {
        processaPorcentagemCombustivel();
        processaPorcentagemDesgasteMotor();
        processaPorcentagemDesgastePneus();
        processaTemperaturaMotor(controleJogo);
        processaTemperaturaPneus(controleJogo);
        ganho = calculaModificadorCombustivel(ganho, no, controleJogo);
        ganho = calculaModificadorAsa(ganho, no, controleJogo);
        ganho = calculaModificadorPneu(ganho, no, controleJogo);
        calculaDesgasteMotor(ganho, no, controleJogo);
        calculaConsumoCombustivel(controleJogo);
        calculaDesgastePneus(no, controleJogo);
        return ganho;
    }

    private double calculaModificadorAsa(double ganho, No no, InterfaceJogo controleJogo) {
        boolean testeAerodinamica = testeAerodinamica();
        boolean testePotencia = testePotencia();
        boolean testeFreios = testeFreios(controleJogo);
        if (no.verificaRetaOuLargada()) {
            if (MENOS_ASA.equals(getAsa())) {
                ganho *= testeAerodinamica && testePotencia ? 1.05 : 1.01;
            } else if (MAIS_ASA.equals(getAsa())) {
                ganho *= testeAerodinamica ? 0.97 : 0.95;
            }
        }
        if (no.verificaCurvaAlta() || no.verificaCurvaBaixa()) {
            if (MENOS_ASA.equals(getAsa())) {
                ganho *= testeAerodinamica ? 0.93 : 0.9;
            } else if (MAIS_ASA.equals(getAsa())) {
                ganho *= testeAerodinamica && testeFreios ? 1.09 : 1.07;
            }
        }
        return ganho;
    }

    public int getTempMax() {
        return tempMax;
    }

    public void setTempMax(int tempMax) {
        this.tempMax = tempMax;
    }

    private void processaTemperaturaMotor(InterfaceJogo controleJogo) {
        if (giro == GIRO_MAX_VAL && temperaturaMotor < tempMax) {
            temperaturaMotor++;
            if (getPiloto().isJogadorHumano() && (temperaturaMotor >= tempMax - 6 && temperaturaMotor <= tempMax - 5)) {
                controleJogo.infoPrioritaria(Html.laranja(Lang.msg("temperatura",
                        new String[]{getPiloto().nomeJogadorFormatado(), Html.txtRedBold(getPiloto().getNome())})));
            }
        }
        if (giro != GIRO_MAX_VAL) {
            if (getPiloto().getNoAtual().verificaRetaOuLargada()) {
                temperaturaMotor -= 3;
            }
            if (getPiloto().getNoAtual().verificaCurvaAlta()) {
                temperaturaMotor -= 2;
            }
            if (getPiloto().getNoAtual().verificaCurvaBaixa()) {
                temperaturaMotor -= 1;
            }
        }
        if (giro == GIRO_MIN_VAL) {
            temperaturaMotor -= 1;
        }
        if (temperaturaMotor > tempMax) {
            temperaturaMotor = tempMax;
        }
        if (temperaturaMotor < 0) {
            temperaturaMotor = 0;
        }
    }

    public boolean verificaMotorSuperAquecido() {
        return temperaturaMotor >= tempMax;
    }

    public int getTemperaturaMotor() {
        return temperaturaMotor;
    }

    public void setTemperaturaMotor(int temperaturaMotor) {
        this.temperaturaMotor = temperaturaMotor;
    }

    public int getTemperaturaPneus() {
        return temperaturaPneus;
    }

    public void setTemperaturaPneus(int temperaturaPneus) {
        this.temperaturaPneus = temperaturaPneus;
    }

    public static void main(String[] args) {
        for (int i = 100; i > 0; i -= 1) {
            System.out.println(i);
            double indicativo = 2 - (1 + i / 1000.0);
            System.out.println("ind " + indicativo);
        }
    }

    private double calculaModificadorCombustivel(double ganho, No no, InterfaceJogo controleJogo) {
        double indicativo = 2 - (1 + porcentagemCombustivel / 1000.0);
        if (indicativo < 0.7) {
            indicativo = 0.7;
        }
        if (indicativo > 1.2) {
            indicativo = 1.2;
        }
        if (!no.verificaRetaOuLargada()) {
            ganho *= indicativo;
        }
        return ganho;
    }

    private void calculaDesgasteMotor(double ganho, No no, InterfaceJogo controleJogo) {
        if (getPiloto().isRecebeuBanderada()) {
            return;
        }
        double valDesgaste;
        boolean testePotencia = testePotencia();
        if (giro == GIRO_MAX_VAL) {
            valDesgaste = ((testePotencia ? 40 : 80) + ganho);
        } else if (giro == GIRO_NOR_VAL) {
            valDesgaste = ((testePotencia ? 20 : 40) + ganho);
        } else {
            valDesgaste = ((testePotencia ? 10 : 20) + ganho);
        }
        if (Clima.SOL.equals(controleJogo.getClima()) && Math.random() < (giro / 10.0)) {
            valDesgaste += 10;
        }
        if (valDesgaste < 0) {
            valDesgaste = 0;
        }
        int dist = 50;
        if (controleJogo.calculaDiffParaProximoRetardatario(getPiloto(), true) < dist) {
            valDesgaste += testeAerodinamica() ? 10 : 20;
        }
        if (verificaMotorSuperAquecido()) {
            double desgasteTemp;
            desgasteTemp = testePotencia ? 2 : 3;
            valDesgaste *= desgasteTemp;
        }
        if (verificaDano()) {
            valDesgaste /= 2;
        }
        if (porcentagemDesgasteMotor < 5 && (GIRO_MIN_VAL == giro)) {
            valDesgaste *= 0.1;
        }
        motor -= valDesgaste;
        if (porcentagemDesgasteMotor < 0) {
            setDanificado(Carro.EXPLODIU_MOTOR, controleJogo);
            controleJogo.infoPrioritaria(
                    Html.vermelho(Lang.msg("042", new String[]{getPiloto().nomeJogadorFormatado(), getPiloto().getNome()})));
        }
    }

    private void calculaConsumoCombustivel(InterfaceJogo controleJogo) {
        if (getPiloto().isRecebeuBanderada()) {
            return;
        }
        boolean testePotencia = testePotencia();
        double valConsumo = 0;
        if (giro == GIRO_MIN_VAL) {
            valConsumo += (testePotencia && testeAerodinamica() ? 0 : 5);
        } else if (giro == GIRO_NOR_VAL) {
            valConsumo += (testePotencia && testeAerodinamica() ? 40 : 80);
        } else if (giro == GIRO_MAX_VAL) {
            valConsumo += (testePotencia && testeAerodinamica() ? 80 : 160);
        }
        if (!controleJogo.isModoQualify() && controleJogo.isSemReabastecimento()) {
            valConsumo *= (controleJogo.getFatorConsumoCombustivelSemReabastecimento() * 0.9);
        }
        combustivel -= valConsumo;
        if (combustivel < 0) {
            combustivel = 0;
            setDanificado(PANE_SECA, controleJogo);
            paneSeca = true;
            controleJogo.infoPrioritaria(Html.txtRedBold(getPiloto().getNome() + " " + Lang.msg("118")));
        }
    }

    public boolean isPaneSeca() {
        return paneSeca;
    }

    private double calculaModificadorPneu(double ganho, No no, InterfaceJogo controleJogo) {
        if (controleJogo.isChovendo() && !no.verificaRetaOuLargada()) {
            return ganho *= TIPO_PNEU_CHUVA.equals(getTipoPneu()) ? 0.95 : 0.85;
        }
        pneuAquecido = getTemperaturaPneus() >= 99;

        if (porcentagemDesgastePneus > 90 || porcentagemDesgastePneus < 10) {
            return ganho;
        }
        if (TIPO_PNEU_MOLE.equals(getTipoPneu())) {
            if (no.verificaCurvaBaixa() || no.verificaCurvaAlta()) {
                if (controleJogo.verificaPistaEmborrachada()
                        && pneuAquecido) {
                    ganho *= 1.3;
                } else {
                    ganho *= testeFreios(controleJogo) ? 1.2 : 1.07;
                }
            }
        } else if (TIPO_PNEU_DURO.equals(getTipoPneu())) {
            if (no.verificaCurvaAlta()) {
                if (pneuAquecido
                        && controleJogo.verificaPistaEmborrachada()) {
                    ganho *= 1.07;
                } else {
                    ganho *= getPiloto().testeHabilidadePilotoFreios(controleJogo) ? 1.05 : 1.03;
                }
            } else if (no.verificaCurvaBaixa()) {
                if (pneuAquecido
                        && controleJogo.verificaPistaEmborrachada()) {
                    ganho *= 1.08;
                } else {
                    ganho *= getPiloto().testeHabilidadePilotoFreios(controleJogo) ? 1.07 : 1.03;
                }
            }
        }
        return ganho;
    }

    private void processaTemperaturaPneus(InterfaceJogo controleJogo) {
        No noAtual = getPiloto().getNoAtual();
        String modoPilotagem = getPiloto().getModoPilotagem();
        double modAquecer = 85;
        if (!TIPO_PNEU_MOLE.equals(getTipoPneu())) {
            modAquecer = 55;
        }
        if (!controleJogo.isChovendo()) {
            if (Piloto.NORMAL.equals(modoPilotagem)) {
                modAquecer -= 20;
            } else if (Piloto.LENTO.equals(modoPilotagem)) {
                modAquecer -= 30;
            }
            if (Clima.NUBLADO.equals(controleJogo.getClima())) {
                modAquecer -= 20;
            }
        }
        if (Clima.CHUVA.equals(controleJogo.getClima())) {
            if (Piloto.LENTO.equals(modoPilotagem)) {
                modAquecer -= 20;
            }
            modAquecer -= 30;
        }
        modAquecer /= 1000;
        if (Math.random() < modAquecer) {
            if (noAtual.verificaRetaOuLargada()) {
                temperaturaPneus += 1;
            } else {
                temperaturaPneus += 3;
            }
        }
        if (controleJogo.isSafetyCarNaPista() && Math.random() > modAquecer) {
            temperaturaPneus--;
        }
        if (temperaturaPneus > 100) {
            temperaturaPneus = 100;
        }
        if (temperaturaPneus == 99) {
            msgPneus(controleJogo);
        }

        if (Clima.CHUVA.equals(controleJogo.getClima())) {
            if (temperaturaPneus < 30) {
                temperaturaPneus = 30;
            }
        } else {
            if (temperaturaPneus < 50) {
                temperaturaPneus = 50;
            }

        }
    }

    private void calculaDesgastePneus(No no, InterfaceJogo controleJogo) {
        if (getPiloto().isRecebeuBanderada() || controleJogo.isSafetyCarNaPista()) {
            return;
        }
        getPiloto().setTravouRodas(false);
        getPiloto().setMarcaPneu(false);
        int incStress = 10 - (getPiloto().getCarro().getPorcentagemDesgastePneus() / 100);
        int decStress = (getPiloto().getCarro().getPorcentagemDesgastePneus() / 100);
        double desgPneus = 10;
        if (no.verificaCurvaBaixa()) {
            getPiloto().incStress(getPiloto().testeHabilidadePilotoAerodinamicaFreios(controleJogo) ? incStress/2 : incStress);
            if (!controleJogo.isChovendo() && getPiloto().getPtosBox() == 0) {
                boolean teste = getPiloto().testeHabilidadePilotoAerodinamicaFreios(controleJogo);
                if (getPiloto().getStress() > 80) {
                    teste = false;
                    if (getPiloto().getStress() > 70 && !no.verificaRetaOuLargada()) {
                        controleJogo.travouRodas(getPiloto(), true);
                    }
                    getPiloto().decStress(getPiloto().testeHabilidadePiloto() ? decStress : decStress/2);
                }
                if (controleJogo.asfaltoAbrasivo() && !controleJogo.isChovendo() && !no.verificaRetaOuLargada()
                        && getPiloto().getStress() > 70) {
                    controleJogo.travouRodas(getPiloto(), true);
                }
                desgPneus += (teste ? 50 : 100);
            }
        } else if (no.verificaCurvaAlta()) {
            desgPneus += (getPiloto().testeHabilidadePilotoAerodinamicaFreios(controleJogo) ? 3 : 4);
            if (!controleJogo.isChovendo() && getPiloto().getPtosBox() == 0) {
                boolean teste = getPiloto().testeHabilidadePilotoAerodinamicaFreios(controleJogo);
                if (getPiloto().getStress() > 70) {
                    teste = false;
                    controleJogo.travouRodas(getPiloto(), true);
                    getPiloto().decStress(getPiloto().testeHabilidadePiloto() ? decStress : decStress/2);
                }
                if (controleJogo.asfaltoAbrasivo() && !controleJogo.isChovendo() && !no.verificaRetaOuLargada()
                        && getPiloto().getStress() > 50 && Math.random() > 0.5) {
                    controleJogo.travouRodas(getPiloto(), true);
                }
                desgPneus += (teste ? 30 : 70);
            }
        } else if (no.verificaRetaOuLargada()) {
            int indexFrete = no.getIndex() + 50;
            if (indexFrete < (controleJogo.getNosDaPista().size() - 1)) {
                No noFrente = controleJogo.getNosDaPista().get(indexFrete);
                boolean teste = getPiloto().testeHabilidadePilotoAerodinamicaFreios(controleJogo);
                if (getPiloto().getStress() > 60 && !controleJogo.isChovendo() && getPiloto().getPtosBox() == 0
                        && noFrente.verificaCurvaBaixa()) {
                    controleJogo.travouRodas(getPiloto(), true);
                    getPiloto().incStress(getPiloto().testeHabilidadePiloto() ? incStress/2 : incStress);
                    if (controleJogo.asfaltoAbrasivo() && getPiloto().getStress() > 80 && !controleJogo.isChovendo()
                            && noFrente.verificaCurvaAlta() && Math.random() > 0.7) {
                        controleJogo.travouRodas(getPiloto(), true);
                    }
                    teste = false;
                }
                desgPneus += (teste ? 5 : 10);
            }
        }
        if (Clima.SOL.equals(controleJogo.getClima()) && Math.random() > 0.5) {
            if (no.verificaCurvaBaixa()) {
                if (!getPiloto().testeHabilidadePilotoAerodinamicaFreios(controleJogo))
                    desgPneus += 15;
                else
                    desgPneus += 10;
            } else if (no.verificaCurvaAlta()) {
                if (!getPiloto().testeHabilidadePilotoAerodinamicaFreios(controleJogo))
                    desgPneus += 15;
                else
                    desgPneus += 10;
            }
        }
        if (!no.verificaRetaOuLargada() && Piloto.AGRESSIVO.equals(getPiloto().getModoPilotagem())) {
            desgPneus += 10;
        }

        if (controleJogo.asfaltoAbrasivo()) {
            desgPneus *= 1.7;
        }
        double fatorComb = 2 - (1 + porcentagemCombustivel / 1000.0);
        if (no.verificaRetaOuLargada()) {
            fatorComb = 1;
        }
        if (fatorComb < 0.9) {
            fatorComb = 0.9;
        }
        double valDesgaste = (desgPneus * fatorComb);

        if (TIPO_PNEU_CHUVA.equals(getTipoPneu()) && !no.verificaRetaOuLargada()) {
            if (Clima.NUBLADO.equals(controleJogo.getClima())) {
                valDesgaste *= 1.1;
            }
            if (Clima.SOL.equals(controleJogo.getClima())) {
                valDesgaste *= 1.2;
            }
        }
        if (!controleJogo.isModoQualify() && controleJogo.isSemTrocaPneu()) {
            valDesgaste *= controleJogo.getFatorConsumoPneuSemTroca();
        }
        if (verificaDano()) {
            valDesgaste /= 3;
        }
        if (porcentagemDesgastePneus < 5 && Piloto.LENTO.equals(getPiloto().getModoPilotagem())) {
            valDesgaste *= 0.1;
        }
        if (porcentagemDesgastePneus < -5 && !controleJogo.isSemTrocaPneu()) {
            setDanificado(Carro.ABANDONOU, controleJogo);
            setRecolhido(true);
            controleJogo.infoPrioritaria(Html.txtRedBold(Lang.msg("abandoNouDevidoDanosRodas",
                    new String[]{getPiloto().nomeJogadorFormatado(), getPiloto().getNome()})));
        }
        pneus -= valDesgaste;
        if ((pneus < 0) && !verificaDano()) {
            setDanificado(PNEU_FURADO, controleJogo);
            pneus = -1;
            controleJogo.infoPrioritaria(Html.vermelho(
                    Lang.msg("043", new String[]{getPiloto().nomeJogadorFormatado(), getPiloto().getNome()})));

        }
    }

    private void msgPneus(InterfaceJogo controleJogo) {
        if (getPiloto().isJogadorHumano() && !controleJogo.isSafetyCarNaPista() && !controleJogo.isChovendo()
                && pneuAquecido && !msgPneu) {
            msgPneu = true;
            controleJogo.info(Html.laranja(
                    Lang.msg("msgpneus", new String[]{getPiloto().nomeJogadorFormatado(), getPiloto().getNome()})));
        }
    }

    public void processaPorcentagemDesgastePneus() {
        if (durabilidadeMaxPneus == 0) {
            return;
        }
        porcentagemDesgastePneus = (100 * pneus) / durabilidadeMaxPneus;
    }

    public void processaPorcentagemDesgasteMotor() {
        if (durabilidadeMaxMotor == 0) {
            return;
        }
        porcentagemDesgasteMotor = (100 * motor) / durabilidadeMaxMotor;
    }

    public void processaPorcentagemCombustivel() {
        if (tanqueCheio == 0) {
            return;
        }
        porcentagemCombustivel = (100 * combustivel) / tanqueCheio;
    }

    public String getDanificado() {
        return danificado;
    }

    public void setDanificado(String danificado, InterfaceJogo interfaceJogo) {
        if (getPiloto().isBox()) {
            getPiloto().setBox(true);
        }
        if (Carro.PANE_SECA.equals(danificado)) {
            paneSeca = true;
        }
        if (Carro.PERDEU_AEREOFOLIO.equals(danificado)) {
            setDurabilidadeAereofolio(0);
        }
        this.danificado = danificado;
        if (ABANDONOU.equals(danificado) || BATEU_FORTE.equals(danificado) || PANE_SECA.equals(danificado)
                || EXPLODIU_MOTOR.equals(danificado)) {
            if (interfaceJogo != null) {
                interfaceJogo.desqualificaPiloto(getPiloto());
            }
            if (ABANDONOU.equals(danificado) || EXPLODIU_MOTOR.equals(danificado) || PANE_SECA.equals(danificado)) {
                setRecolhido(true);
            }
        }

    }

    public boolean verificaDano() {
        return (danificado != null);
    }

    public void setTipoPneu(String tipoPneu) {
        this.tipoPneu = tipoPneu;
    }

    public void mudarGiroMotor(String giroMotor) {
        if (GIRO_MAX.equals(giroMotor)) {
            giro = GIRO_MAX_VAL;
        } else if (GIRO_MIN.equals(giroMotor)) {
            giro = GIRO_MIN_VAL;
        } else if (GIRO_NOR.equals(giroMotor)) {
            giro = GIRO_NOR_VAL;
        }

    }

    @JsonIgnore
    public String getGiroFormatado() {
        switch (giro) {
            case 1:
                return Lang.msg("Min");
            case 5:
                return Lang.msg("Nor");
            case 9:
                return Lang.msg("Max");
            default:
                break;
        }
        return null;
    }

    public String getAsa() {
        return asa;
    }

    public void setAsa(String asa) {
        this.asa = asa;
    }

    public int getDurabilidadeAereofolio() {
        return durabilidadeAereofolio;
    }

    public void setDurabilidadeAereofolio(int durabilidadeAereofolio) {
        this.durabilidadeAereofolio = durabilidadeAereofolio;
    }

    public void usaErs() {
        if (cargaErs > 0) {
            cargaErs -= (testePotencia() ? 1 : 2);
            pontenciaErs = true;
        } else {
            pontenciaErs = false;
        }
    }

    public boolean isPneuFurado() {
        return PNEU_FURADO.equals(danificado);
    }

    public boolean verificaParado() {
        return (!isRecolhido() && BATEU_FORTE.equals(danificado)) || EXPLODIU_MOTOR.equals(danificado)
                || PANE_SECA.equals(danificado) || ABANDONOU.equals(danificado);
    }

    public int getAerodinamica() {
        return aerodinamica;
    }

    public void setAerodinamica(int aerodinamica) {
        this.aerodinamica = aerodinamica;
    }

    public int getFreios() {
        return freios;
    }

    public void setFreios(int freios) {
        this.freios = freios;
    }

    public void atualizaInfoDebug(StringBuilder buffer) {
        Field[] declaredFields = Carro.class.getDeclaredFields();
        buffer.append("-=Carro=- <br>");
        List<String> campos = new ArrayList<String>();
        for (Field field : declaredFields) {
            try {
                Object object = field.get(this);
                String valor = "null";
                if (object != null) {
                    if (Util.isWrapperType(object.getClass())) {
                        continue;
                    }
                    valor = object.toString();
                }
                campos.add(field.getName() + " = " + valor + "<br>");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        Collections.sort(campos, new StringComparator());
        for (Iterator<String> iterator = campos.iterator(); iterator.hasNext(); ) {
            buffer.append(iterator.next());
        }
    }

    public int getPorcentagemCombustivel() {
        return porcentagemCombustivel;
    }

    public void setPorcentagemCombustivel(int porcentagemCombustivel) {
        this.porcentagemCombustivel = porcentagemCombustivel;
    }

    public int getPorcentagemDesgastePneus() {
        return porcentagemDesgastePneus;
    }

    public void setPorcentagemDesgastePneus(int porcentagemDesgastePneus) {
        this.porcentagemDesgastePneus = porcentagemDesgastePneus;
    }

    public int getPorcentagemDesgasteMotor() {
        return porcentagemDesgasteMotor;
    }

    public void setPorcentagemDesgasteMotor(int porcentagemDesgasteMotor) {
        this.porcentagemDesgasteMotor = porcentagemDesgasteMotor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private static class StringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }

    public int getAeroAntesQualify() {
        return aeroAntesQualify;
    }

    public void setAeroAntesQualify(int aeroAntesQualify) {
        this.aeroAntesQualify = aeroAntesQualify;
    }

    public int getFreiosAntesQualify() {
        return freiosAntesQualify;
    }

    public void setFreiosAntesQualify(int freiosAntesQualify) {
        this.freiosAntesQualify = freiosAntesQualify;
    }
}

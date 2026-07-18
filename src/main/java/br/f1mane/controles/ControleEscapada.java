package br.f1mane.controles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.Piloto;
import br.f1mane.recursos.idiomas.Lang;
import br.nnpe.Global;
import br.nnpe.Html;

/**
 * Derrapagem (traçado 0 → 1/2) e escapada ancorada ao traçado (traçado 1/2 →
 * 4/5, via {@link ObjetoEscapada}) — mecânicas "irmãs" que compartilham a
 * busca da próxima zona de escapada no circuito. Extraído de {@code Piloto}.
 *
 * @author Paulo Sobreira
 */
public class ControleEscapada {
    private final InterfaceJogo controleJogo;

    /** Estado por piloto exclusivo desta classe — nada aqui é lido fora dela. */
    private static final class EstadoEscapada {
        /** Modo de pilotagem de antes de entrar no traçado de fuga (4/5) — restaurado ao voltar. */
        String modoPilotagemAntesDaFuga;
        /** Giro de antes de entrar no traçado de fuga (4/5) — restaurado ao voltar. */
        int giroAntesDaFuga;
        /** Se o piloto estava no traçado de fuga (4/5) no ciclo anterior — usado só para detectar a transição de saída e restaurar modo/giro. */
        boolean estavaNoTracadoDeFuga;
        /**
         * Resultado (marcado/não marcado) do par de testes sequenciais (stress,
         * depois pneus) de cada {@link ObjetoEscapada} já resolvida nesta volta,
         * feito assim que a zona entra na janela de detecção da entrada. Só uma
         * entrada por zona por volta: uma vez presente aqui (seja {@code true}
         * marcado ou {@code false} não marcado), a zona não é testada de novo
         * até a volta mudar.
         */
        final Map<ObjetoEscapada, Boolean> resultadoTesteEscapadaPorZonaNestaVolta = new HashMap<>();
        /** Volta em que {@code resultadoTesteEscapadaPorZonaNestaVolta} foi populado pela última vez — usada só pra saber quando limpar o cache ao virar a volta. */
        int numeroVoltaDoCacheDeTesteEscapada = -1;
    }

    private final Map<Piloto, EstadoEscapada> estadoPorPiloto = new HashMap<>();

    public ControleEscapada(InterfaceJogo controleJogo) {
        this.controleJogo = controleJogo;
    }

    private EstadoEscapada estadoDe(Piloto piloto) {
        return estadoPorPiloto.computeIfAbsent(piloto, p -> new EstadoEscapada());
    }

    public void processaEscapadaDaPista(Piloto piloto) {
        if (controleJogo.isSafetyCarNaPista()) {
            return;
        }
        if (controleJogo.isModoQualify()) {
            return;
        }
        EstadoEscapada estado = estadoDe(piloto);
        if (piloto.isBox() || piloto.getPtosBox() != 0) {
            /**
             * Piloto indo pro box (desde a decisão, isBox(), não só quando
             * já fisicamente na pit lane, getPtosBox() != 0) é excluído da
             * escapada. Se já estava marcado (impedidoDeMudarTracadoPorEscapada)
             * antes de decidir ir pro box, libera a trava aqui — sem isso,
             * ela nunca mais seria limpa (só processaEscapadaAncoradaAoTracado(),
             * que deixa de rodar, faz isso), travando o piloto pra qualquer
             * mudança de traçado (inclusive entrar no box) pelo resto da
             * corrida.
             */
            piloto.setImpedidoDeMudarTracadoPorEscapada(false);
            return;
        }
        /**
         * Redução de velocidade/modo/giro vale só enquanto o piloto está
         * literalmente no traçado de fuga (4 ou 5) — não durante toda a
         * janela de retorno (animação de troca de traçado), que é só o
         * suavizado visual da posição lateral, comum a qualquer mudança de
         * traçado. Modo/giro de antes são salvos ao entrar e restaurados
         * assim que o traçado deixa de ser 4/5, em vez de depender de outro
         * método (ex.: processaIAnovoIndex) resetar pra NORMAL/GIRO_NOR_VAL —
         * esse reset pode nunca rodar (ex.: com colisao != null), deixando o
         * piloto travado em LENTO/giro mínimo pra sempre depois de escapar.
         */
        boolean noTracadoDeFuga = piloto.getTracado() == 4 || piloto.getTracado() == 5;
        if (noTracadoDeFuga) {
            /**
             * Rede de segurança: a escapada já foi cumprida (o piloto está
             * de fato no traçado de fuga), então a trava de mudança de
             * traçado (ver Piloto.mudarTracado()) não tem mais motivo pra
             * existir — normalmente já foi limpa por
             * processaEscapadaAncoradaAoTracado() antes desta mudança de
             * traçado acontecer, mas redundante aqui cobre qualquer via
             * alternativa de entrada em 4/5.
             */
            piloto.setImpedidoDeMudarTracadoPorEscapada(false);
            if (!estado.estavaNoTracadoDeFuga) {
                estado.modoPilotagemAntesDaFuga = piloto.getModoPilotagem();
                estado.giroAntesDaFuga = piloto.getCarro().getGiro();
            }
            piloto.setGanho(piloto.getGanho() * 0.60);
            piloto.getCarro().setGiro(Carro.GIRO_MIN_VAL);
            piloto.setModoPilotagem(Piloto.LENTO);
        } else if (estado.estavaNoTracadoDeFuga) {
            piloto.setModoPilotagem(estado.modoPilotagemAntesDaFuga);
            piloto.getCarro().setGiro(estado.giroAntesDaFuga);
        }
        estado.estavaNoTracadoDeFuga = noTracadoDeFuga;

        processaEscapadaAncoradaAoTracado(piloto, estado);
        processaSaidaDaEscapada(piloto);
    }

    /**
     * Derrapagem do traçado 0 para o 1 ou 2: independente de stress/modo de
     * pilotagem (ao contrário da escapada — ver
     * {@link #processaEscapadaAncoradaAoTracado}), dispara só por pneus
     * gastos + falha no teste de habilidade de freios numa curva. Escolhe o
     * lado da próxima {@link ObjetoEscapada} do circuito (ver
     * {@link #proximaEscapadaNoCircuito}), qualquer que seja o traçado em
     * que ela está ancorada — colocar o piloto já no traçado certo pra uma
     * possível escapada à frente, em vez de simplesmente alternar de lado.
     * Sem nenhuma escapada no circuito (ou nenhuma mais à frente nesta
     * volta), sorteia entre 1 e 2. Usa {@code mudarTracado} NÃO forçado —
     * a guarda genérica de {@code mudarTracado} já adia a troca sozinha
     * enquanto uma animação de troca anterior ainda está em andamento.
     * <p>
     * Chamado de {@code Piloto.processaNovoIndex()} como irmã de
     * {@link #processaEscapadaDaPista} (não aninhada nela) — as duas
     * compartilham as mesmas guardas iniciais (safety car, qualify, rota de
     * box), repetidas aqui já que cada uma roda de forma independente.
     */
    public void processaDerrapagem(Piloto piloto) {
        if (controleJogo.isSafetyCarNaPista()) {
            return;
        }
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (piloto.getPtosBox() != 0) {
            return;
        }
        if (!(piloto.getNoAtual().verificaCurvaBaixa() || piloto.getNoAtual().verificaCurvaAlta())
                || piloto.getTracado() != 0
                || piloto.getCarro().getPorcentagemDesgastePneus() >= 30
                || piloto.testeHabilidadePilotoFreios()) {
            return;
        }
        controleJogo.travouRodas(piloto);
        ObjetoEscapada proximaEscapada = proximaEscapadaNoCircuito(piloto);
        if (proximaEscapada != null) {
            piloto.mudarTracado(proximaEscapada.getTracadoOrigem());
        } else {
            piloto.mudarTracado(controleJogo.getRandom().intervalo(1, 2));
        }
    }

    /**
     * {@link ObjetoEscapada} mais próxima à frente do piloto no circuito,
     * em qualquer traçado (1 ou 2) em que esteja ancorada — usada pela
     * derrapagem ({@link #processaDerrapagem}) pra escolher o lado que já
     * deixa o piloto no traçado certo pra essa zona. Mesma lógica de
     * {@link #proximaEscapadaNoTracadoAtual}, só sem o filtro de traçado (o
     * piloto está no 0 quando isto é chamado, então não há "traçado atual"
     * 1/2 pra filtrar).
     */
    private ObjetoEscapada proximaEscapadaNoCircuito(Piloto piloto) {
        List<ObjetoPista> objetos = controleJogo.getCircuito().getObjetos();
        if (objetos == null) {
            return null;
        }
        int indiceAtual = piloto.getNoAtual().getIndex();
        ObjetoEscapada maisProxima = null;
        for (ObjetoPista objetoPista : objetos) {
            if (!(objetoPista instanceof ObjetoEscapada)) {
                continue;
            }
            ObjetoEscapada escapada = (ObjetoEscapada) objetoPista;
            if (escapada.getIndiceEntrada() < 0 || escapada.getIndiceSaida() < indiceAtual) {
                continue;
            }
            if (maisProxima == null || escapada.getIndiceEntrada() < maisProxima.getIndiceEntrada()) {
                maisProxima = escapada;
            }
        }
        return maisProxima;
    }

    /**
     * Tolerância (em índices de nó) para o índice atual já ter passado
     * ligeiramente da entrada de uma escapada no mesmo ciclo em que a
     * ultrapassa (o índice avança por {@code avancoLimitado}, que pode ser
     * maior que 1 por ciclo — então um salto grande pode pular exatamente
     * por cima do índice de entrada). Além dessa tolerância, a entrada é
     * considerada perdida/já passada: o piloto NÃO pode mais escapar por
     * essa zona, só pela próxima. Ver histórico completo em
     * {@code Piloto.java} antes desta extração — bug relatado em Interlagos
     * corrigido aumentando de 20 pra 150.
     */
    private static final int TOLERANCIA_INDICES_ENTRADA_JA_PASSADA = 150;

    /**
     * Janela de detecção/teste da escapada ancorada: distância (em índices
     * de nó) até {@code indiceEntrada} a partir da qual a zona já é
     * considerada alcançável para fins de teste. Numericamente igual a
     * {@link #TOLERANCIA_INDICES_ENTRADA_JA_PASSADA} (coincidência
     * conveniente), mas conceitualmente distinta: esta é a janela de
     * detecção À FRENTE da entrada; a outra é a tolerância de salto de
     * ciclo JÁ PASSADA da entrada.
     */
    private static final int JANELA_DETECCAO_ENTRADA_ESCAPADA = 150;

    /**
     * Reconecta a corrida ao modelo de {@link ObjetoEscapada} (ancorado ao
     * traçado via indiceEntrada/indiceSaida). Só relevante quando o piloto
     * está no traçado 1 ou 2, já que zonas de escapada nunca são ancoradas
     * ao traçado 0. Não persiste estado entre ciclos (fora do cache de
     * teste por volta): a cada chamada, recalcula do zero qual é a próxima
     * escapada à frente no traçado atual.
     * <p>
     * Dois testes sequenciais e independentes por causa de risco — teste 1
     * (stress, ver {@link #testeEscapadaStress}) e, só se o 1 não marcar,
     * teste 2 (pneus, ver {@link #testeEscapadaPneus}) — rodam no MÁXIMO
     * UMA VEZ por zona por piloto por volta: assim que a zona entra na
     * janela de detecção ({@code distancia} entre
     * -{@link #TOLERANCIA_INDICES_ENTRADA_JA_PASSADA} e
     * {@link #JANELA_DETECCAO_ENTRADA_ESCAPADA}) e ainda não há resultado
     * em cache para ela nesta volta, os dois testes rodam e o desfecho
     * (marcado ou não marcado) é gravado — em QUALQUER ciclo seguinte desta
     * mesma volta, mesmo mais perto da entrada e mesmo que o piloto continue
     * satisfazendo as mesmas pré-condições de risco, a zona NÃO é testada de
     * novo. Um piloto marcado cumpre a escapada ao alcançar
     * {@code distancia <= 0} independente de {@code modoPilotagem} nesse
     * momento — mudar de modo depois de marcado não salva mais; a única
     * forma de evitar a marca é passar no teste de habilidade quando a
     * zona é avaliada.
     * <p>
     * NÃO tenta desviar pro traçado 0 pra evitar a zona (ver D15 do
     * design.md): decidir se/quando sair do traçado 1/2 de volta pro 0 é
     * responsabilidade exclusiva da lógica geral de condução — a lógica de
     * escapada só decide se o piloto escapa ou não a partir de onde ele já
     * está, nunca dirige o carro pra longe da zona por conta própria.
     */
    private void processaEscapadaAncoradaAoTracado(Piloto piloto, EstadoEscapada estado) {
        int tracadoAtual = piloto.getTracado();
        if (tracadoAtual != 1 && tracadoAtual != 2) {
            return;
        }
        ObjetoEscapada zona = proximaEscapadaNoTracadoAtual(piloto, tracadoAtual);
        if (zona == null) {
            return;
        }
        int distancia = zona.getIndiceEntrada() - piloto.getNoAtual().getIndex();
        if (distancia > JANELA_DETECCAO_ENTRADA_ESCAPADA) {
            return;
        }
        if (distancia < -TOLERANCIA_INDICES_ENTRADA_JA_PASSADA) {
            /** Entrada já passada há tempo (não foi um mero salto de 1 ciclo): essa zona não vale mais. */
            return;
        }

        garanteCacheDeTesteEscapadaDaVoltaAtual(piloto, estado);

        Boolean resultado = estado.resultadoTesteEscapadaPorZonaNestaVolta.get(zona);
        if (resultado == null) {
            boolean marcado = testeEscapadaStress(piloto);
            if (!marcado) {
                marcado = testeEscapadaPneus(piloto);
            }
            estado.resultadoTesteEscapadaPorZonaNestaVolta.put(zona, marcado);
            resultado = marcado;
            if (marcado) {
                piloto.setImpedidoDeMudarTracadoPorEscapada(true);
            } else {
                notificaTesteEscapada(piloto);
            }
        }

        if (Boolean.FALSE.equals(resultado)) {
            /** Não marcado (passou nos dois testes, ou nenhuma pré-condição de risco se aplicou) — livre dessa zona pelo resto da volta. */
            return;
        }

        if (distancia <= 0 && piloto.getTracado() == tracadoAtual) {
            /**
             * Limpa a trava ANTES de forçar a mudança: é a própria execução
             * da escapada que precisa passar por mudarTracado, então a
             * trava não pode mais estar ativa nesse exato ciclo (ver
             * Piloto.mudarTracado()).
             */
            piloto.setImpedidoDeMudarTracadoPorEscapada(false);
            piloto.mudarTracado(laneDeFugaDoTracadoOrigem(tracadoAtual), true);
            notificaEscapadaExecutada(piloto);
        }
    }

    /** Limpa o cache de teste por zona por volta quando a volta muda, sem precisar de nenhum outro gatilho externo de reset. */
    private void garanteCacheDeTesteEscapadaDaVoltaAtual(Piloto piloto, EstadoEscapada estado) {
        int voltaAtual = piloto.getNumeroVolta();
        if (voltaAtual != estado.numeroVoltaDoCacheDeTesteEscapada) {
            estado.resultadoTesteEscapadaPorZonaNestaVolta.clear();
            estado.numeroVoltaDoCacheDeTesteEscapada = voltaAtual;
        }
    }

    /**
     * Regra de elegibilidade compartilhada pelas notificações de escapada
     * ({@link #notificaTesteEscapada} e {@link #notificaEscapadaExecutada}):
     * jogador humano OU piloto entre os 3 primeiros.
     */
    private boolean elegivelParaNotificacaoDeEscapada(Piloto piloto) {
        return piloto.isJogadorHumano() || piloto.getPosicao() <= 3;
    }

    /**
     * Notifica (mensagem não prioritária) o piloto que passou no teste de
     * escapada numa curva — ou seja, NÃO foi marcado pra escapar —,
     * restrito ao jogador humano ou a pilotos entre os 3 primeiros. Só
     * dispara quando alguma pré-condição de risco realmente se aplicou
     * ({@link #precondicaoTesteEscapadaStress} ou
     * {@link #precondicaoTesteEscapadaPneus}); sem risco nenhum, não há
     * "quase" a notificar.
     */
    private void notificaTesteEscapada(Piloto piloto) {
        if (!elegivelParaNotificacaoDeEscapada(piloto)) {
            return;
        }
        String chave;
        if (precondicaoTesteEscapadaStress(piloto)) {
            chave = "quaseErraCurvaRapido";
        } else if (precondicaoTesteEscapadaPneus(piloto)) {
            chave = "quaseErraCurvaPneusGastos";
        } else {
            return;
        }
        controleJogo.info(
                Html.preto(Lang.msg(chave, new String[] { piloto.nomeJogadorFormatado(), Html.negrito(piloto.getNome()) })));
    }

    /**
     * Notifica (mensagem prioritária, em vermelho pra chamar mais atenção)
     * o piloto marcado no momento em que a escapada é de fato executada
     * (entrada no traçado de fuga), restrito ao jogador humano ou a
     * pilotos entre os 3 primeiros — mesma regra de
     * {@link #notificaTesteEscapada}.
     */
    private void notificaEscapadaExecutada(Piloto piloto) {
        if (!elegivelParaNotificacaoDeEscapada(piloto)) {
            return;
        }
        controleJogo.infoPrioritaria(Html.vermelho(Lang.msg("sofreEscapadaDePista",
                new String[] { piloto.nomeJogadorFormatado(), Html.negrito(piloto.getNome()) })));
    }

    /**
     * Teste de stress da escapada ancorada: marca o piloto quando
     * {@code stress} está acima do limite E ele falha no teste de
     * habilidade — não exige {@code AGRESSIVO} nem exclui {@code LENTO}, e
     * não abre exceção pro jogador humano em modo manual (passa pelo mesmo
     * teste que a IA). Curto-circuito do {@code &&}: se {@code stress} não
     * estiver acima do limite, o teste de habilidade nem chega a ser
     * chamado (sem consumir RNG). Sucesso no teste de habilidade só evita a
     * marca — NÃO muda mais {@code modoPilotagem} pra {@code LENTO} (a
     * recompensa por "quase escapar" foi removida no tuning).
     */
    private boolean testeEscapadaStress(Piloto piloto) {
        if (!precondicaoTesteEscapadaStress(piloto)) {
            return false;
        }
        return !piloto.testeHabilidadePilotoCarro();
    }

    /** Pré-condição (sem RNG) do {@link #testeEscapadaStress}: usada também pra decidir a mensagem de quase-escapada quando o piloto passa no teste de habilidade. */
    private boolean precondicaoTesteEscapadaStress(Piloto piloto) {
        return piloto.getStress() > Global.LIMITE_ESTRESSE_PARA_ESCAPADA_ANCORADA;
    }

    /**
     * Teste de pneus da escapada ancorada: marca o piloto quando pneus
     * abaixo de 30% E stress acima de
     * {@link Global#LIMITE_ESTRESSE_PARA_ESCAPADA_PNEUS} (70, mais baixo
     * que o limite do teste de stress — as duas condições são exigidas
     * juntas, não é "ou") E ele falha no teste de habilidade de freios (não
     * o teste de habilidade genérico — este teste é sobre pneus/freios
     * especificamente). Não exclui {@code modoPilotagem == LENTO} nem abre
     * exceção pro jogador humano em modo manual. Mesmo curto-circuito de
     * {@link #testeEscapadaStress}: sem as duas pré-condições, nenhum
     * teste de habilidade é consultado. Sucesso só evita a marca, sem
     * recompensa de {@code LENTO}.
     */
    private boolean testeEscapadaPneus(Piloto piloto) {
        if (!precondicaoTesteEscapadaPneus(piloto)) {
            return false;
        }
        return !piloto.testeHabilidadePilotoFreios();
    }

    /** Pré-condição (sem RNG) do {@link #testeEscapadaPneus}: usada também pra decidir a mensagem de quase-escapada quando o piloto passa no teste de habilidade. */
    private boolean precondicaoTesteEscapadaPneus(Piloto piloto) {
        return piloto.getCarro().getPorcentagemDesgastePneus() < 30
                && piloto.getStress() >= Global.LIMITE_ESTRESSE_PARA_ESCAPADA_PNEUS;
    }

    /**
     * Traçado de fuga (4 ou 5) correspondente ao traçado de origem (1 ou 2)
     * de uma escapada — confirmado por {@code Piloto.mudarTracado} (só
     * permite RETORNAR de 4 para 2 e de 5 para 1): origem 1 → foge pelo
     * traçado 5; origem 2 → foge pelo traçado 4. NÃO é 1→4/2→5 (erro
     * corrigido após bug relatado em produção onde carros saíam pelo
     * traçado 1 e voltavam no traçado 2).
     */
    private static int laneDeFugaDoTracadoOrigem(int tracadoOrigem) {
        return tracadoOrigem == 1 ? 5 : 4;
    }

    /**
     * Traçado de origem (1 ou 2) de onde veio quem está fugindo pelo
     * traçado informado (4 ou 5) — inverso de
     * {@link #laneDeFugaDoTracadoOrigem(int)}.
     */
    private static int tracadoOrigemDoLaneDeFuga(int laneDeFuga) {
        return laneDeFuga == 5 ? 1 : 2;
    }

    /**
     * Tolerância (em índices de nó) para o índice atual já ter passado
     * ligeiramente de {@code indiceSaida} no mesmo ciclo em que a
     * ultrapassa — mesmo problema, no lado da SAÍDA, que
     * {@link #TOLERANCIA_INDICES_ENTRADA_JA_PASSADA} resolve no lado da
     * entrada. Sem essa tolerância, um piloto cujo teste de habilidade
     * falhasse em todos os ciclos dentro da janela de retorno podia ter o
     * índice ultrapassar {@code indiceSaida} de uma vez, e a partir daí
     * {@link #escapadaAtivaNoTracadoDeFuga} parava de encontrar a zona —
     * o piloto ficava preso no traçado de fuga (4/5) pra sempre.
     */
    private static final int TOLERANCIA_INDICES_SAIDA_JA_PASSADA = 100;

    /**
     * Reconecta o RETORNO da escapada: enquanto o piloto está no traçado de
     * fuga (4 ou 5), monitora a zona de {@link ObjetoEscapada} ativa nesse
     * traçado e, a 100 índices de nó ou menos do fim dela
     * ({@code indiceSaida}), faz um teste de habilidade do piloto a cada
     * ciclo para decidir se ele já consegue voltar ao traçado de origem (o
     * MESMO de onde saiu, nunca o outro) — ao suceder, força
     * {@code mudarTracado} de volta pra esse traçado.
     */
    private void processaSaidaDaEscapada(Piloto piloto) {
        int tracadoFuga = piloto.getTracado();
        if (tracadoFuga != 4 && tracadoFuga != 5) {
            return;
        }
        ObjetoEscapada zona = escapadaAtivaNoTracadoDeFuga(piloto, tracadoFuga);
        if (zona == null) {
            return;
        }
        int distanciaSaida = zona.getIndiceSaida() - piloto.getNoAtual().getIndex();
        if (distanciaSaida > 100 || distanciaSaida < -TOLERANCIA_INDICES_SAIDA_JA_PASSADA) {
            return;
        }
        if (piloto.testeHabilidadePiloto()) {
            piloto.mudarTracado(tracadoOrigemDoLaneDeFuga(tracadoFuga), true);
        }
    }

    /**
     * {@link ObjetoEscapada} cujo traçado de fuga correspondente
     * ({@link #laneDeFugaDoTracadoOrigem(int)} do seu {@code tracadoOrigem})
     * é {@code tracadoFuga}, e cujo intervalo
     * [{@code indiceEntrada}, {@code indiceSaida} +
     * {@link #TOLERANCIA_INDICES_SAIDA_JA_PASSADA}] cobre o índice atual do
     * piloto — ou {@code null} se nenhuma.
     */
    private ObjetoEscapada escapadaAtivaNoTracadoDeFuga(Piloto piloto, int tracadoFuga) {
        List<ObjetoPista> objetos = controleJogo.getCircuito().getObjetos();
        if (objetos == null) {
            return null;
        }
        int tracadoOrigemAlvo = tracadoOrigemDoLaneDeFuga(tracadoFuga);
        int indiceAtual = piloto.getNoAtual().getIndex();
        for (ObjetoPista objetoPista : objetos) {
            if (!(objetoPista instanceof ObjetoEscapada)) {
                continue;
            }
            ObjetoEscapada escapada = (ObjetoEscapada) objetoPista;
            if (escapada.getTracadoOrigem() != tracadoOrigemAlvo) {
                continue;
            }
            if (escapada.getIndiceEntrada() < 0 || escapada.getIndiceSaida() <= escapada.getIndiceEntrada()) {
                continue;
            }
            if (indiceAtual < escapada.getIndiceEntrada()
                    || indiceAtual > escapada.getIndiceSaida() + TOLERANCIA_INDICES_SAIDA_JA_PASSADA) {
                continue;
            }
            return escapada;
        }
        return null;
    }

    /**
     * {@link ObjetoEscapada} mais próxima à frente do piloto, ancorada no
     * mesmo traçado (1 ou 2) em que o piloto está agora, que ainda não foi
     * totalmente ultrapassada ({@code indiceSaida >= índice atual}) — ou
     * {@code null} se não houver nenhuma.
     */
    private ObjetoEscapada proximaEscapadaNoTracadoAtual(Piloto piloto, int tracadoAtual) {
        List<ObjetoPista> objetos = controleJogo.getCircuito().getObjetos();
        if (objetos == null) {
            return null;
        }
        int indiceAtual = piloto.getNoAtual().getIndex();
        ObjetoEscapada maisProxima = null;
        for (ObjetoPista objetoPista : objetos) {
            if (!(objetoPista instanceof ObjetoEscapada)) {
                continue;
            }
            ObjetoEscapada escapada = (ObjetoEscapada) objetoPista;
            if (escapada.getTracadoOrigem() != tracadoAtual) {
                continue;
            }
            if (escapada.getIndiceEntrada() < 0 || escapada.getIndiceSaida() < indiceAtual) {
                continue;
            }
            if (maisProxima == null || escapada.getIndiceEntrada() < maisProxima.getIndiceEntrada()) {
                maisProxima = escapada;
            }
        }
        return maisProxima;
    }
}

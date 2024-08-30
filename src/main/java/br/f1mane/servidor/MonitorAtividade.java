package br.f1mane.servidor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.f1mane.servidor.controles.ControlePaddockServidor;
import br.nnpe.Global;
import br.nnpe.Logger;
import br.f1mane.controles.InterfaceJogo;
import br.f1mane.servidor.entidades.TOs.SessaoCliente;

/**
 * @author Paulo Sobreira Criado em 25/08/2007 as 11:22:46
 */
public class MonitorAtividade implements Runnable {

    private final ControlePaddockServidor controlePaddock;
    private boolean alive = true;

    public MonitorAtividade(ControlePaddockServidor controlePaddock) {
        this.controlePaddock = controlePaddock;
    }

    /**
     * @see Runnable#run()
     */
    public void run() {
        while (alive) {
            try {
                Thread.sleep(5000);
                long timeNow = System.currentTimeMillis();
                removeClientesIniativos(timeNow);
                removeSessoesIniativas(timeNow);
                Map<SessaoCliente, JogoServidor> jogos = controlePaddock
                        .getControleJogosServer().getMapaJogosCriados();
                iniciaJogos(timeNow, jogos);
                removeJogadoresSemSessao(jogos);
                removeJogosSemJogadores(jogos, timeNow);
            } catch (InterruptedException e) {
                Logger.logarExept(e);
            } catch (Exception e) {
                Logger.logarExept(e);
            }
        }

    }

    private void removeJogosSemJogadores(Map<SessaoCliente, JogoServidor> jogos,
                                         long timeNow) {
        for (Iterator<SessaoCliente> iter = jogos.keySet().iterator(); iter
                .hasNext(); ) {
            SessaoCliente key = iter.next();
            JogoServidor jogoServidor = jogos.get(key);
            if ((timeNow - jogoServidor.getTempoCriacao()) < 300000) {
                continue;
            }
            if (jogoServidor.getMapJogadoresOnline().isEmpty()) {
                if (!jogoServidor.isCorridaTerminada()) {
                    Logger.logar("removeJogosSemJogadores "
                            + jogoServidor.getCircuito().getNome() + " - "
                            + jogoServidor.getTemporada());
                }
                jogoServidor.encerraCorrida();
            }
        }
    }

    public void removeJogadoresSemSessao(
            Map<SessaoCliente, JogoServidor> jogos) {
        for (Iterator<SessaoCliente> iter = jogos.keySet().iterator(); iter
                .hasNext(); ) {
            SessaoCliente key = iter.next();
            JogoServidor jogoServidor = jogos.get(key);
            for (Iterator<String> iterator = jogoServidor
                    .getMapJogadoresOnline().keySet().iterator(); iterator
                         .hasNext(); ) {
                String tokenJogador = iterator.next();
                SessaoCliente sessaoCliente = controlePaddock
                        .verificaUsuarioSessao(tokenJogador);
                if (sessaoCliente == null) {
                    iterator.remove();
                }
            }
        }
    }

    public void iniciaJogos(long timeNow,
                            Map<SessaoCliente, JogoServidor> jogos) throws Exception {
        for (Iterator<SessaoCliente> iter = jogos.keySet().iterator(); iter
                .hasNext(); ) {
            SessaoCliente key = iter.next();
            JogoServidor jogoServidor = (JogoServidor) jogos.get(key);
            if ((timeNow - jogoServidor
                    .getTempoCriacao()) > (Global.SEGUNDOS_PARA_INICIAR_CORRRIDA.intValue()
                    * 1000)) {
                jogoServidor.iniciarJogo();
            }
        }
    }

    public void removeClientesIniativos(long timeNow) {
        List<SessaoCliente> clientes = controlePaddock.getDadosPaddock()
                .getClientes();
        SessaoCliente sessaoClienteRemover = null;
        for (Iterator<SessaoCliente> iter = clientes.iterator(); iter
                .hasNext(); ) {
            SessaoCliente sessaoCliente = iter.next();
            InterfaceJogo jogo = controlePaddock
                    .obterJogoPeloNome(sessaoCliente.getJogoAtual());
            if (jogo != null
                    && (jogo.isSafetyCarNaPista() || jogo.isCorridaTerminada()
                    || !jogo.isCorridaIniciada())) {
                continue;
            }
            int intervaloAtividade = 800000;
            if (sessaoCliente.isGuest()) {
                intervaloAtividade = 400000;
            }
            if ((timeNow
                    - sessaoCliente.getUlimaAtividade()) > intervaloAtividade) {
                sessaoClienteRemover = sessaoCliente;
                break;
            }
        }
        if (sessaoClienteRemover != null) {
            controlePaddock.removerCliente(sessaoClienteRemover);
            Logger.logar(
                    "removeClientesIniativos " + sessaoClienteRemover.getNomeJogador());
        }
    }

    public void removeSessoesIniativas(long timeNow) {
        List<SessaoCliente> clientes = controlePaddock.getDadosPaddock()
                .getClientes();
        SessaoCliente sessaoClienteRemover = null;
        for (Iterator<SessaoCliente> iter = clientes.iterator(); iter
                .hasNext(); ) {
            SessaoCliente sessaoCliente = iter.next();
            int intervaloAtividade = 400000;
            if (sessaoCliente.isGuest()) {
                intervaloAtividade = 200000;
            }
            if ((timeNow
                    - sessaoCliente.getUlimaAtividade()) > intervaloAtividade) {
                sessaoClienteRemover = sessaoCliente;
                break;
            }
        }
        if (sessaoClienteRemover != null) {
            Logger.logar("removeSessoesIniativas " + sessaoClienteRemover.getNomeJogador());
            controlePaddock.removerSessao(sessaoClienteRemover);
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

}

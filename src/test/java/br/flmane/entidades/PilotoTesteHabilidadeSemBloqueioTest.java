package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * testeHabilidadePiloto() era o bloqueio mais abrangente por desconcentração
 * (usado em quase todo cálculo de ganho, ultrapassagem, troca de traçado,
 * DRS/ERS) — hoje resolve unicamente a partir de `danificado()` e da
 * habilidade do piloto, sem nenhuma condição equivalente escondida.
 */
class PilotoTesteHabilidadeSemBloqueioTest {

    private Piloto criarPiloto(double valorNextDouble) {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(valorNextDouble);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        return piloto;
    }

    @Test
    void habilidadeAlta_sorteioFavoravel_passa() {
        Piloto piloto = criarPiloto(0.0);
        piloto.setHabilidade(500);

        assertTrue(piloto.testeHabilidadePiloto());
    }

    @Test
    void habilidadeBaixa_sorteioDesfavoravel_naoPassa() {
        Piloto piloto = criarPiloto(0.999);
        piloto.setHabilidade(500);

        assertFalse(piloto.testeHabilidadePiloto());
    }

    @Test
    void carroDanificado_sempreFalha_independenteDaHabilidade() {
        Piloto piloto = criarPiloto(0.0); // sorteio favoravel, mas carro danificado deveria bloquear
        piloto.setHabilidade(999);
        piloto.getCarro().setDanificado(Carro.EXPLODIU_MOTOR);

        assertFalse(piloto.testeHabilidadePiloto(), "carro danificado deveria continuar bloqueando (isso não muda)");
    }
}

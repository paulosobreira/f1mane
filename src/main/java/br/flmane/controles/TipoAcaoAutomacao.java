package br.flmane.controles;

/**
 * Tipos de ação que {@link ControleAutomacao} pode decidir e executar em
 * nome de um piloto de IA. Cada tipo tem seu próprio cooldown independente
 * em jogo online (ver {@link ControleAutomacao}).
 */
public enum TipoAcaoAutomacao {
    MODO_PILOTAGEM, GIRO_MOTOR, ERS, DRS, BOX, TRACADO
}

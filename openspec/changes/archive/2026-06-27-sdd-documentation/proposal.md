## Why

O projeto F1 Mane não possui documentação de design de software formal. Com o código morto removido, o codebase está estável o suficiente para ser documentado com precisão — o SDD servirá de referência para onboarding, manutenção e evolução do sistema.

## What Changes

- Criar o SDD (`docs/sdd.md`) cobrindo toda a arquitetura do sistema: modos de execução, camada de jogo, servidor multiplayer, modelo de dados, persistência, rendering e recursos
- O documento descreve o que **está implementado**, não o que deveria estar
- Nenhuma mudança de código — somente adição de documentação

## Capabilities

### New Capabilities
- `sdd-execution-modes`: Três modos de execução (`MainLauncher`, `MainFrame`, `AppletPaddock`) e como o mesmo JAR serve todos
- `sdd-game-engine`: Engine de corrida local — `ControleJogoLocal`, `ControleCiclo` (tick loop), `ControleCorrida`, `ControleBox`, `ControleSafetyCar`, `ControleClima`, `GerenciadorVisual`
- `sdd-multiplayer`: Camada servidor — `PaddockServer`, `LetsRace` (REST JAX-RS), `SessaoCliente`, `JogoServidor`, `JogoCliente`
- `sdd-data-model`: Entidades centrais — `Piloto`, `Carro`, `Circuito`, `No` e seus campos, constantes e relações
- `sdd-persistence`: JPA com perfis Maven H2/MySQL, entidades que estendem `F1ManeDados`, `ControlePersistencia`
- `sdd-rendering`: `PainelCircuito` (Swing paintComponent), `SpriteSheet` (layout de pixels por temporada), `GerenciadorVisual`
- `sdd-resources`: `CarregadorRecursos` (singleton com cache), circuitos em XML, i18n via `Lang.msg()`, `SpriteSheet`

### Modified Capabilities

## Impact

- Cria `docs/sdd.md` (arquivo novo, sem impacto em código existente)
- Referencia código-fonte existente com caminhos de arquivo e nomes de classe reais

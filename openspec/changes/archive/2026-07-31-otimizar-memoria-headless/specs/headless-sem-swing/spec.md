## ADDED Requirements

### Requirement: Corrida no servidor headless não instancia thread de rendering
Quando uma corrida é criada sob o servidor headless, `ControleJogoLocal` SHALL NOT instanciar `GerenciadorVisual` nem qualquer thread de rendering — o cliente web recebe o estado da corrida por REST e desenha no navegador, de modo que a thread de rendering e os buffers que ela mantém são custo puro no servidor.

#### Scenario: Corrida criada em modo headless
- **WHEN** uma corrida é iniciada num processo rodando em modo headless (servidor)
- **THEN** nenhum `GerenciadorVisual` é criado para essa corrida e nenhuma thread de rendering é iniciada

#### Scenario: Corrida criada em modo GUI mantém rendering
- **WHEN** uma corrida é iniciada por `MainFrame` (modo solo Swing)
- **THEN** o `GerenciadorVisual` é criado e a thread de rendering roda como hoje

#### Scenario: Ciclo da corrida headless continua funcional
- **WHEN** uma corrida roda no servidor headless sem `GerenciadorVisual`
- **THEN** o tick da simulação, as voltas, ultrapassagens, pit stops e o resultado final continuam produzidos normalmente e servidos pelo endpoint REST

### Requirement: Componentes Swing criados sob demanda, nunca no caminho servidor
Os componentes Swing hoje mantidos como campo por corrida — o painel de debug e o `JEditorPane` de informação textual de `ControleEstatisticas`, o painel de narração e o `PainelTabelaResultadoFinal` de `ControleJogoLocal` — SHALL ser criados apenas quando efetivamente solicitados por um modo com interface gráfica, permanecendo nulos durante toda a vida de uma corrida servida em modo headless.

#### Scenario: Corrida headless não aloca painel de debug nem editor de texto
- **WHEN** uma corrida completa roda do início ao fim no servidor headless
- **THEN** nenhum `JPanel`, `JEditorPane`, `JScrollPane` ou `PainelTabelaResultadoFinal` é instanciado por essa corrida

#### Scenario: Modo GUI continua exibindo os mesmos painéis
- **WHEN** o modo solo Swing solicita o painel de debug ou o resultado final
- **THEN** o componente é criado sob demanda e exibe o mesmo conteúdo de hoje

#### Scenario: Atualização de estatísticas sem componente presente
- **WHEN** as estatísticas da corrida são atualizadas em modo headless e nenhum componente de exibição foi criado
- **THEN** a atualização não cria componente nem agenda tarefa na Event Dispatch Thread, e não lança exceção

### Requirement: Contrato de jogo isolado das classes de visão
O contrato de corrida usado pelo servidor headless (`InterfaceJogo`) SHALL NOT mencionar tipos Swing, `java.awt` de UI ou `br.flmane.visao` em nenhuma assinatura — os pontos que devolvem componentes gráficos moram num contrato separado (`InterfaceJogoVisual`), implementado só pelos modos com interface. `java.awt.image.BufferedImage` é exceção explícita: é Java2D puro, gerado e servido em modo headless sem componente de UI.

#### Scenario: Contrato de corrida sem tipos gráficos
- **WHEN** as assinaturas declaradas por `InterfaceJogo` são inspecionadas
- **THEN** nenhum tipo de `javax.swing`, `br.flmane.visao`, `br.flmane.MainFrame` ou `java.awt` (exceto `BufferedImage`) aparece como retorno ou parâmetro

#### Scenario: Pontos gráficos preservados no contrato visual
- **WHEN** as assinaturas de `InterfaceJogoVisual` são inspecionadas
- **THEN** `getMainFrame`, `setMainFrame`, `obterResultadoFinal`, `painelNarracao` e `painelDebug` continuam declarados — a separação move funcionalidade, não a apaga

#### Scenario: Modo GUI mantém acesso a todos os pontos do contrato
- **WHEN** o modo solo Swing usa o controle de jogo
- **THEN** todos os pontos que devolvem componentes gráficos continuam acessíveis, sem mudança de comportamento para o jogador

### Requirement: Estruturas estáticas de vida longa com crescimento limitado
Estruturas estáticas que acompanham a vida do processo servidor — em particular o mapa de contagem de exceções de `br.nnpe.Logger` — SHALL ter limite de tamanho explícito e política definida de descarte, de forma que o consumo não cresça indefinidamente com o tempo de uptime do container.

#### Scenario: Muitas exceções distintas registradas
- **WHEN** o processo servidor registra um número de assinaturas de exceção distintas acima do limite configurado
- **THEN** o mapa de contagem permanece dentro do limite, descartando entradas conforme a política definida, sem lançar erro nem perder o registro em log das exceções

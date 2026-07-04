## 1. Constantes e detecção de zona de frenagem

- [x] 1.1 Adicionar em `Global.java` as constantes `TAMANHO_ZONA_FRENAGEM`, `MAX_CLUSTERS_CURVA_ALTA_ZONA_FRENAGEM`, `MIN_NOS_CURVA_BAIXA_ZONA_FRENAGEM` e `FATOR_REDUCAO_MARCAS_CURVA`, com os valores iniciais do design
- [x] 1.2 Em `ControleRecursos.carregaRecursos(...)`, ao lado do cálculo de `mapaNoProxCurva`/`mapaNoCurvaAnterior`, implementar a varredura que identifica clusters contíguos de `CURVA_BAIXA` com pelo menos `MIN_NOS_CURVA_BAIXA_ZONA_FRENAGEM` nós, verifica que não há mais que `MAX_CLUSTERS_CURVA_ALTA_ZONA_FRENAGEM` nós de curva alta no trecho de reta anterior, e marca os nós de reta (até `TAMANHO_ZONA_FRENAGEM`) e os nós do próprio cluster como pertencentes a um `Set<No> zonaFrenagemNos`
- [x] 1.3 Expor a consulta (`isNoZonaFrenagem(No no)`) em `InterfaceJogo`/`ControleJogoLocal` (e stub/no-op equivalente em `JogoCliente`, igual a `obterProxCurva`), análoga a `obterProxCurva`
- [x] 1.4 Adicionar teste unitário cobrindo: reta+curva baixa extensa forma zona de frenagem; curva baixa curta isolada não forma zona; trecho com muitas curvas altas intermediárias (esses/chicane) não forma zona

## 2. processaFreioNaReta usa a zona de frenagem

- [x] 2.1 Alterar `Piloto.processaFreioNaReta()` para consultar `controleJogo.isNoZonaFrenagem(getNoAtual())` (em vez de só a distância até a curva mais próxima de qualquer tipo) antes de setar `freiandoReta=true`/acionar `travouRodas`
- [x] 2.2 Manter a suavização gradual de `multi`/`minMulti` baseada na distância até a curva, mesmo com a nova condição de zona
- [x] 2.3 Atualizar/adicionar testes cobrindo: freiando fora de zona de frenagem não aciona travada de roda por este caminho; freiando dentro da zona mantém a suavização de ganho

## 3. Redistribuição de intensidade de marcas e faíscas

- [x] 3.1 Em `ControleJogoLocal.travouRodas(Piloto, boolean)`, aplicar `Global.FATOR_REDUCAO_MARCAS_CURVA` à chance de gerar `TravadaRoda` quando o nó atual for `CURVA_BAIXA`/`CURVA_ALTA` e não estiver em zona de frenagem
- [x] 3.2 ~~Em `Piloto.processaFaiscas()`, aplicar a mesma redução...~~ Revisado durante a implementação: `processaFaiscas()` já restringe faíscas a nós de reta/largada (nunca dispara em nó de curva, antes ou depois desta mudança) — não há redução de curva a aplicar ali. A concentração na zona de frenagem já é obtida de graça pelo requisito 2.1 (`isFreiandoReta()` agora exige zona de frenagem), que gate a o bônus de probabilidade (`mod -= .50`) de `processaFaiscas`. Specs ajustadas (`zona-frenagem-marcas-intensidade`) pra refletir isso.
- [x] 3.3 Confirmar que, dentro da zona de frenagem, as taxas permanecem nas faixas atuais (sem a redução)
- [x] 3.4 Adicionar testes cobrindo: redução de marca/faísca em curva fora de zona de frenagem; ausência de redução dentro da zona de frenagem

## 4. ~~Flag de sessão no editor (não persistido)~~ — REMOVIDO

~~Todo este grupo foi implementado e depois removido a pedido: desenhar faísca/marca de pneu não é relevante pro editor de circuitos.~~ Ver seção "Retração" em design.md e grupo 10 abaixo.

## 5. ~~Simulação no teste de pista: marca de pneu persistente + faísca efêmera~~ — REMOVIDO

~~Todo este grupo foi implementado e depois removido a pedido.~~ Ver seção "Retração" em design.md e grupo 10 abaixo.

## 6. Marcação visual da zona de frenagem no editor

- [x] 6.1 Implementar `MainPanelEditor.desenhaZonaFrenagemOverlay(Graphics2D)`, que percorre os nós marcados como zona de frenagem e desenha uma marcação com cor diferenciada (ex.: acinzentada/translúcida) sobre o traçado nesses pontos
- [x] 6.2 Chamar esse método logo após `DesenhoProceduralCircuito.desenhaPistaZebraEBox(...)` no `paintComponent` do editor, sempre que o traçado estiver sendo mostrado
- [x] 6.3 Confirmar que `DesenhoProceduralCircuito.desenhaPistaZebraEBox` em si permanece inalterada, garantindo que a marcação não vaza para a imagem gerada em memória usada em corrida real
- [x] 6.4 Adicionar teste cobrindo: circuito com zona de frenagem detectada expõe os nós corretos pro overlay desenhar; circuito sem zona de frenagem não desenha nada; a rotina de geração de imagem em memória usada em corrida não invoca o overlay

## 7. Verificação

- [x] 7.1 Rodar `mvn test` e garantir que os testes existentes de `Piloto`/`ControleJogoLocal`/editor continuam passando
- [x] 7.2 Testar manualmente no editor (`EditorCircuitos`): confirmar que a marcação visual da zona de frenagem aparece automaticamente ao abrir um circuito com zona detectada (sem precisar de nenhum checkbox), e que o teste de pista roda normalmente sem nenhum controle de faísca/marca de pneu

## 8. Correções pós-implementação (achadas testando com Albert Park de verdade)

- [x] 8.1 Corrigir `ControleRecursos.calculaZonaFrenagem`: `MAX_NOS_CURVA_ALTA_ZONA_FRENAGEM` contava nós individuais de curva alta, rejeitando curvas altas longas e sinuosas (como a curva 1 de Albert Park, 123 nós) como se fossem chicane; renomeado pra `MAX_CLUSTERS_CURVA_ALTA_ZONA_FRENAGEM` e a contagem passou a ser por trecho contíguo (incrementa só ao entrar num trecho novo de curva alta)
- [x] 8.2 Corrigir `ControleRecursos.calculaZonaFrenagem`: varredura pra trás não era circular, então a reta antes de uma curva logo após a largada (que cruza o índice 0 da lista de nós) não era contada corretamente; trocado por indexação circular limitada a uma volta completa
- [x] 8.3 Corrigir `MainPanelEditor.obterZonaFrenagemNos()`: cache invalidava por referência de `circuito` (que nunca muda), não pela lista `pistaFull` (que `vetorizarPista(...)` substitui por instâncias de `No` novas) — o cache ficava preso aos nós antigos após revetorizar, fazendo a simulação/overlay do teste de pista pararem de reconhecer qualquer zona de frenagem
- [x] 8.4 Adicionar testes de regressão para os três casos acima, incluindo um teste que carrega `albert_park_mro.xml` de verdade e confirma que a primeira curva (logo após a reta da largada) forma zona de frenagem
- [x] 8.5 Atualizar specs (`zona-frenagem-deteccao`) e design.md pra refletir a semântica corrigida (trechos, não nós; varredura circular)

## 9. ~~Reforço da correlação com processaFreioNaReta/travouRodas (segunda rodada)~~ — REMOVIDO

~~Todo este grupo foi implementado (correção do sorteio por nó/tick, chances alinhadas com `travouRodas`/`processaFaiscas`, correção de vazamento de `Graphics`) e depois removido junto com o resto da simulação.~~ Ver grupo 10.

## 10. Remoção da simulação de faísca/marca de pneu no editor

- [x] 10.1 Remover de `MainPanelEditor.java`: campo `simulaFaiscaEMarcasPneu`, checkbox "Faísca e Marcas de Pneu" (e ajuste do `GridLayout` de 7 pra 6 colunas em `gerarBotoesTestePista`), `ultimoNoSimuladoFreioNaReta`, `travadaRodaImg0/1/2Editor`, `CHANCE_MARCA_PNEU_SIMULADA`/`CHANCE_FAISCA_SIMULADA`, `simulaFaiscaEMarcasPneuNoTesteDePista`, `pintaMarcaPneuSimulada`, `desenhaFaiscaSimulada`, `carregaSpritesTravadaRodaEditorSeNecessario`, e a chamada desses métodos em `desenhaCarroTeste`
- [x] 10.2 Remover de `TestePista.java`: campo `noAtual` e `getNoAtual()` (só existiam pra essa simulação; nenhum outro código os usava)
- [x] 10.3 Remover import não usado (`br.nnpe.ImageUtil`) deixado órfão em `MainPanelEditor.java`
- [x] 10.4 Remover os arquivos de teste da simulação (`MainPanelEditorSimulaFaiscaEMarcasPneuCheckboxTest.java`, `MainPanelEditorSimulaFaiscaEMarcasPneuTesteDePistaTest.java`); manter `MainPanelEditorZonaFrenagemOverlayTest.java` (a marcação visual da zona continua)
- [x] 10.5 Remover a capability `zona-frenagem-editor-preview` (proposal.md, specs/zona-frenagem-editor-preview/) e ajustar `zona-frenagem-visualizacao-editor` (remover o requisito de independência do checkbox, que não existe mais)
- [x] 10.6 Rodar `mvn test` completo e confirmar que nada mais referencia a simulação removida

## 11. Marca de pneu restrita à zona de frenagem, com gradiente de intensidade

- [x] 11.1 Trocar `ControleRecursos.zonaFrenagemNos` (`Set<No>`) por `zonaFrenagemPosicoes` (`Map<No, Double>`); `calculaZonaFrenagem` (estático e instância) passa a calcular, pra cada nó da zona, sua posição relativa (0.0 no início/nó mais distante da curva, 1.0 no final/último nó do cluster), na ordem real de percurso (reta invertida + cluster)
- [x] 11.2 Adicionar `ControleRecursos.obterPosicaoNaZonaFrenagem(No)` (retorna `Double`, `null` se fora de qualquer zona); `isNoZonaFrenagem` passa a delegar pra `containsKey`
- [x] 11.3 Reescrever `ControleJogoLocal.travouRodas`: vetar completamente (early return) quando o nó atual não estiver em nenhuma zona de frenagem, independente do tipo de nó; dentro da zona, multiplicar `lim` por uma interpolação linear entre `Global.INTENSIDADE_MARCA_INICIO_ZONA_FRENAGEM` (posição 0.0) e `Global.INTENSIDADE_MARCA_FIM_ZONA_FRENAGEM` (posição 1.0)
- [x] 11.4 Remover `Global.FATOR_REDUCAO_MARCAS_CURVA` (obsoleto, substituído pelo veto); adicionar `Global.INTENSIDADE_MARCA_INICIO_ZONA_FRENAGEM` (1.0) e `Global.INTENSIDADE_MARCA_FIM_ZONA_FRENAGEM` (0.3)
- [x] 11.5 Adaptar `MainPanelEditor.obterZonaFrenagemNos()` pra usar `.keySet()` sobre o novo retorno `Map<No, Double>`, mantendo sua própria assinatura (`Set<No>`) e cache inalterados (o overlay visual não precisa de posição, só de pertencimento)
- [x] 11.6 Atualizar testes: nó de qualquer tipo fora da zona nunca gera marca (não mais "reduzida", vetada por completo); início da zona usa intensidade máxima; final da zona usa intensidade reduzida; mesmo sorteio gera marca no início e não gera no final (demonstra o gradiente); posição relativa cresce de 0.0 a 1.0 ao longo do percurso da zona
- [x] 11.7 Atualizar specs (`zona-frenagem-deteccao`, `zona-frenagem-marcas-intensidade`) e design.md pra refletir o veto completo fora da zona e o gradiente de posição
- [x] 11.8 Rodar `mvn test` completo

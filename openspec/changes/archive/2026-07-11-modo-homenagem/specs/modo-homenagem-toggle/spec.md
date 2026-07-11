## ADDED Requirements

### Requirement: `Global.MODO_HOMENAGEM` substitui `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA`

O sistema SHALL expor um flag booleano `Global.MODO_HOMENAGEM` (default `true`), e `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` SHALL ser removido. Todo ponto de código que hoje checa `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` (`DesenhoProceduralCircuito`, `CarregadorRecursos`, `LetsRace`) SHALL passar a checar `MODO_HOMENAGEM` no lugar, preservando o mesmo comportamento de geração procedural de imagem de circuito descrito em `objetos-cenario-circuito`.

#### Scenario: GERAR_IMAGEM_CIRCUITO_EM_MEMORIA não existe mais
- **WHEN** o código é compilado após esta mudança
- **THEN** não há mais nenhuma referência a `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` em `src/main/java`

#### Scenario: MODO_HOMENAGEM controla a geração procedural do circuito
- **WHEN** `Global.MODO_HOMENAGEM` é `true` e uma corrida (solo ou o endpoint REST do multiplayer) carrega um circuito
- **THEN** a imagem de fundo do circuito é gerada em memória, exatamente como `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA=true` fazia antes desta mudança

### Requirement: Nome de piloto e carro é resolvido uma vez no carregamento, conforme `MODO_HOMENAGEM`

Ao carregar `pilotos.properties`/`carros.properties` de uma temporada, `Piloto.nome` e `Carro.nome` SHALL ser definidos como `nomeHomenagem` quando `Global.MODO_HOMENAGEM` for `true`, ou como o nome real (a chave original da properties) quando for `false` — substituindo as chamadas a `Util.substVogais(name)` usadas hoje pra esses dois campos. `Carro` SHALL ganhar um campo `nomeOriginal` (análogo ao já existente em `Piloto`) pra guardar o nome real independente do modo. Quando `nomeHomenagem` estiver ausente/vazio pra um carro ou piloto (temporada não migrada, ou removido manualmente), o nome real SHALL ser usado como fallback, mesmo com `MODO_HOMENAGEM` ativo.

#### Scenario: Modo homenagem ativo usa o nome-homenagem
- **WHEN** `Global.MODO_HOMENAGEM` é `true` e um piloto/carro com `nomeHomenagem` preenchido é carregado
- **THEN** `getNome()` desse piloto/carro retorna o valor de `nomeHomenagem`

#### Scenario: Modo homenagem inativo usa o nome real
- **WHEN** `Global.MODO_HOMENAGEM` é `false` e um piloto/carro é carregado
- **THEN** `getNome()` desse piloto/carro retorna o nome real (a chave original da properties), sem qualquer substituição de vogais

#### Scenario: Fallback pro nome real quando nomeHomenagem está ausente
- **WHEN** `Global.MODO_HOMENAGEM` é `true` e um piloto/carro sem `nomeHomenagem` preenchido é carregado
- **THEN** `getNome()` desse piloto/carro retorna o nome real, sem lançar exceção

#### Scenario: Nomes de circuito continuam usando substVogais, fora do escopo deste flag
- **WHEN** um nome de circuito é exibido (em qualquer estado de `MODO_HOMENAGEM`)
- **THEN** o comportamento de `Util.substVogais` pra nomes de circuito permanece inalterado por esta mudança

### Requirement: Imagem de carro/piloto prioriza um modelo colorido genérico em modo homenagem

Quando `Global.MODO_HOMENAGEM` for `true`, a resolução de imagem de carro (lado/cima) e capacete SHALL seguir esta ordem de prioridade: (1) o modelo/silhueta genérico pintado com `cor1`/`cor2` do carro (`CarregadorRecursos.pintarModeloV2`); (2) o sprite sheet da temporada, se `SpriteSheet.isDisponivel` for verdadeiro; (3) o arquivo de imagem individual (coluna `imagem`/capacete específico); voltando a (1) caso nenhuma das opções (2)/(3) esteja disponível, garantindo que sempre haja algo pra desenhar. Quando `Global.MODO_HOMENAGEM` for `false`, a ordem de prioridade SHALL ser a mesma usada antes desta mudança (sprite sheet se disponível, senão arquivo individual — sem o modelo colorido genérico).

#### Scenario: Modo homenagem ativo usa o modelo colorido por padrão
- **WHEN** `Global.MODO_HOMENAGEM` é `true` e um carro é desenhado
- **THEN** a imagem usada é o modelo genérico preenchido com `cor1`/`cor2` desse carro, não o sprite sheet nem o arquivo individual

#### Scenario: Modo homenagem inativo preserva a prioridade atual
- **WHEN** `Global.MODO_HOMENAGEM` é `false` e a temporada tem sprite sheet disponível
- **THEN** a imagem usada é a do sprite sheet, exatamente como antes desta mudança

#### Scenario: Fallback garantido quando nada mais está disponível
- **WHEN** `Global.MODO_HOMENAGEM` é `true`, a temporada não tem sprite sheet, e o arquivo de imagem individual não existe/falha ao carregar
- **THEN** o modelo colorido genérico é usado, sem lançar exceção nem deixar o carro sem imagem nenhuma

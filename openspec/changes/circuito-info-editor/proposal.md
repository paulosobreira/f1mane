## Why

`circuitos.properties` guarda hoje, por circuito, `<arquivo>=<NomeExibicao>,<ciclo>,<ativo>` — mas `ciclo` (o intervalo em milissegundos entre ticks da simulação, `InterfaceJogo.tempoCicloCircuito()`, usado por `ControleCiclo`/`ControleBox`/`ControleQualificacao` etc.) não é metadado de listagem como `nome`/`ativo`: é um parâmetro de ajuste fino do próprio circuito, hoje só editável indiretamente (abrindo `circuitos.properties` à mão) e sem nenhum retorno visual de "esse valor de ciclo dá que tempo de volta". Quem ajusta `ciclo` também não tem, no editor, a distância real do circuito — outro dado que ajuda a calibrar se o tempo de volta estimado faz sentido.

## What Changes

- `Circuito` ganha uma propriedade `ciclo` (inteiro, milissegundos por tick), editável no editor de circuitos (spinner), e `circuitos.properties` deixa de gravar esse valor — a linha passa de `<arquivo>=<NomeExibicao>,<ciclo>,<ativo>` para `<arquivo>=<NomeExibicao>,<ativo>`.
- `ControleRecursos`/`ControleJogoLocal`/`JogoCliente` (servidor multiplayer) passam a ler `ciclo` do `Circuito` já carregado (`circuito.getCiclo()`) em vez do mapa estático `circuitosCiclo` alimentado por `circuitos.properties`.
- O editor mostra, calculado e atualizado ao vivo (sempre que `ciclo` ou o traçado mudam), um **tempo de volta estimado**: soma, para cada nó de `pistaFull` classificado por tipo (reta/largada, curva alta, curva baixa), `1 / ganho médio daquele tipo` ticks, multiplicada por `ciclo` (ms/tick) — usando ganhos médios aproximados lidos da fórmula já existente em `Piloto.calculaModificadorPrincipal()` (não precisa ser exato, só refletir a mesma ordem de grandeza usada em corrida).
- O editor mostra também a **distância do circuito em quilômetros**, calculada a partir do comprimento de `pistaFull` (uma amostra por pixel do traçado, via `Circuito.vetorizarPista()`) e uma escala fixa pixels-por-metro (derivada do comprimento real aproximado de um carro de F1 frente ao tamanho em pixels de `Carro.ALTURA`).
- Migração dos circuitos existentes: extrai o `ciclo` hoje gravado em `circuitos.properties` para o campo `ciclo` de cada circuito (arquivo de metadados, `_mro_meta.xml`), e remove esse campo das linhas de `circuitos.properties`.

## Capabilities

### New Capabilities
- `circuito-info-editor`: `Circuito.ciclo` como propriedade própria do circuito (não mais em `circuitos.properties`), e cálculo/exibição no editor de tempo de volta estimado (a partir de `ciclo` + ganhos médios por tipo de nó) e distância do circuito em quilômetros (a partir do comprimento de `pistaFull` e uma escala pixels-por-metro).

### Modified Capabilities
<!-- Nenhuma spec existente documenta o formato de circuitos.properties ou o consumo de tempoCicloCircuito() em detalhe suficiente para exigir um delta — é território novo. -->

## Impact

- `br.f1mane.entidades.Circuito` — novo campo `ciclo` (int, ms/tick, default ~160) com getter/setter.
- `br.f1mane.controles.ControleRecursos` — `carregarCircuitos()` deixa de popular `circuitosCiclo` a partir de `circuitos.properties` (linha passa a ter só `NomeExibicao,ativo`).
- `br.f1mane.controles.ControleJogoLocal` / `br.f1mane.servidor.applet.JogoCliente` — `tempoCicloCircuito()` passa a retornar `circuito.getCiclo()` em vez de `circuitosCiclo.get(circuito.getNome())`.
- `br.f1mane.editor.MainPanelEditor` — novo spinner de `ciclo` (substituindo a dependência de `circuitos.properties` para esse valor) e novos labels somente-leitura de tempo de volta estimado e distância em km, recalculados quando o circuito é (re)vetorizado.
- `br.f1mane.entidades.Piloto` — nenhuma mudança de comportamento; só leitura dos valores-base já existentes em `calculaModificadorPrincipal()` para estimar ganhos médios por tipo de nó (constantes replicadas no cálculo do editor, não uma refatoração da fórmula de corrida).
- `src/main/resources/properties/circuitos.properties` — todas as linhas perdem o campo `ciclo` (passam de 3 para 2 campos), migrado para o `ciclo` de cada circuito.
- Circuitos existentes continuam carregando e jogando normalmente; `ciclo` ausente no circuito (circuito muito antigo, migração não aplicada) assume um valor padrão seguro (~160ms) no construtor de `Circuito`.

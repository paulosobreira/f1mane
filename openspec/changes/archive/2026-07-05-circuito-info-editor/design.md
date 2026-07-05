## Context

`ciclo` (tempo entre ticks de simulação, em ms) vive hoje em `properties/circuitos.properties` (`<arquivo>=<NomeExibicao>,<ciclo>,<ativo>`), carregado para o mapa estático `ControleRecursos.circuitosCiclo` e consultado por `InterfaceJogo.tempoCicloCircuito()` (implementado em `ControleJogoLocal`/`JogoCliente` como `circuitosCiclo.get(circuito.getNome())`). `ControleCiclo.run()` faz `Thread.sleep(controleJogo.tempoCicloCircuito())` a cada tick — ou seja, `ciclo` é literalmente quantos milissegundos reais separam um tick do próximo, não uma contagem de voltas. `Piloto.processarCiclo()`/`calculaModificadorPrincipal()` calculam, a cada tick, quantos "nós" de `pistaFull` o piloto avança (`ganho`), com faixas de valor diferentes por tipo de nó (reta/largada, curva alta, curva baixa) antes de qualquer modificador de carro/piloto/clima. `Circuito.vetorizarPista()` já expõe `pistaFull` (uma entrada por pixel do traçado, via `GeoUtil.drawBresenhamLine`) com o `tipo` de cada nó preservado.

## Goals / Non-Goals

**Goals:**
- `ciclo` sai de `circuitos.properties` e vira uma propriedade comum de `Circuito`, editável no editor como qualquer outro campo (`probalidadeChuva`, `multiplicadorLarguraPista`, etc.).
- Tempo de volta estimado aparece no editor, recalculado sempre que `ciclo` ou o traçado mudam — ferramenta de apoio para calibrar `ciclo`, não um substituto da simulação real.
- `distanciaKm` é um dado real do circuito (a distância oficial, conhecida de fontes externas), informado pelo usuário num campo do editor e persistido junto do circuito — não algo que o sistema tente adivinhar a partir da geometria do traçado.
- Migração dos circuitos existentes preserva os valores de `ciclo` hoje gravados em `circuitos.properties`.

**Non-Goals:**
- Não reimplementar ou alterar `Piloto.calculaModificadorPrincipal()` — o cálculo do editor só lê valores representativos (aproximados) da mesma faixa usada em corrida, sem tocar a fórmula real nem replicar toda a árvore de modificadores (carro, clima, dano, ERS, etc.).
- Não calcular `distanciaKm` a partir de `pistaFull`/geometria do traçado nem de nenhuma escala pixels-por-metro — é um dado informado, não derivado.
- Não persistir o tempo de volta estimado no XML do circuito — é calculado em memória a partir de `pistaFull`/`ciclo` toda vez que o editor precisa exibi-lo, não um dado gravado (diferente de `distanciaKm`, que é gravado por ser informado, não calculado).
- Não mudar a semântica de `tempoCicloCircuito()` nem o loop de `ControleCiclo` — só de onde o valor vem.

## Decisions

### `ciclo` como campo simples de `Circuito`, populado a partir de `circuitos.properties` só durante a migração
`Circuito` ganha `private int ciclo = 160;` (valor por padrão dentro da faixa hoje usada, 140-240ms) com getter/setter, seguindo o mesmo padrão de `probalidadeChuva`. Persistido no arquivo de metadados (`_mro_meta.xml`, ver capability `circuito-metadados-arquivo`), junto dos demais campos leves. `circuitos.properties` perde o campo (linha passa de 3 para 2 valores: `NomeExibicao,ativo`), e `ControleRecursos.carregarCircuitos()`/`ControleJogoLocal.tempoCicloCircuito()`/`JogoCliente.tempoCicloCircuito()` passam a usar `circuito.getCiclo()` — eliminando a necessidade do mapa estático `circuitosCiclo` para esse fim (mantido só se algum outro consumidor depender dele; a leitura de código não achou nenhum).

### Ganhos médios por tipo de nó: constantes aproximadas, derivadas por inspeção de `calculaModificadorPrincipal()`
`Piloto.calculaModificadorPrincipal()` retorna, por tipo de nó, uma faixa de valores-base (antes de modificadores de carro/piloto/clima/dano): reta/largada 30-50, curva alta 20-30, curva baixa 10-20. O cálculo do editor usa a média de cada faixa como "ganho médio" representativo: `GANHO_MEDIO_RETA = 40`, `GANHO_MEDIO_CURVA_ALTA = 25`, `GANHO_MEDIO_CURVA_BAIXA = 15` — constantes do próprio cálculo do editor (não uma refatoração de `Piloto`), documentadas com a faixa de onde vieram para facilitar reajuste se a fórmula de corrida mudar.
- Alternativa descartada — chamar `Piloto.calculaModificadorPrincipal()` de verdade a partir do editor: exigiria instanciar um `Piloto`/`Carro`/`ControleJogo` completos só para estimar um número aproximado, um custo de acoplamento desproporcional ao "não precisa ser exato" pedido.

### Tempo de volta estimado: ticks por tipo de nó, multiplicados por `ciclo`
Para cada nó de `circuito.getPistaFull()`, soma-se `1 / ganhoMedio(tipoDoNo)` (nós de reta e largada tratados igual, via `No.verificaRetaOuLargada()`) para obter o total de ticks de uma volta; multiplicado por `ciclo` (ms/tick) dá o tempo estimado em milissegundos, formatado como `mm:ss.SSS` (reaproveitando o mesmo formato usado em `ControleEstatisticas.formatarTempo`, se exposto, ou uma formatação equivalente local ao editor).

### Distância em quilômetros: campo informado pelo usuário, não calculado
`Circuito` ganha `private double distanciaKm = 0;` com getter/setter, editável no editor via um campo numérico simples (`JTextField`/`JSpinner`, mesmo padrão de `probalidadeChuva`), gravado no arquivo de metadados junto dos demais campos leves. Não há cálculo a partir de `pistaFull`/geometria — o valor vem de quem edita o circuito, tipicamente copiado da ficha técnica oficial do autódromo real.
- Alternativa descartada — calcular a partir de `pistaFull.size()` e uma escala pixels-por-metro assumida: rejeitada porque a distância real de cada autódromo já é um dado público conhecido, mais confiável que qualquer estimativa geométrica a partir do traçado desenhado no editor (que nunca é uma cópia exata da geometria real).

### Exibição no editor: tempo de volta estimado é somente-leitura; distância em km é um campo editável
Tempo de volta estimado aparece como `JLabel` (não editável) na barra superior do editor, atualizado no mesmo ponto em que `vetorizarCircuito()`/`refletirCircuitoNosCampos()` já rodam (após carregar um circuito, e após qualquer edição de nó que dispare revetorização) e também quando `ciclo` é alterado — para não exigir um novo gatilho de atualização. `distanciaKm`, por ser informado (não calculado), aparece como campo editável comum, gravado em `formularioObjetoPista`-style (o mesmo fluxo de `probalidadeChuvaText`/`nomePistaText`) sempre que o usuário o altera.

## Risks / Trade-offs

- [Migração perder algum valor de `ciclo` se um circuito em `circuitos.properties` não tiver um arquivo de circuito correspondente, ou vice-versa] → O utilitário de migração itera as linhas de `circuitos.properties`, lê o `ciclo` de cada uma antes de removê-lo, e só grava no circuito correspondente se o arquivo existir — circuitos sem correspondência ficam registrados no log da migração para conferência manual.
- [Constantes de ganho médio ficarem desatualizadas se `calculaModificadorPrincipal()` mudar no futuro] → Aceito conscientemente; o comentário no código aponta para o método de origem, facilitando reajuste manual quando a fórmula de corrida mudar.
- [Estimativa de tempo de volta dar números "estranhos" para o usuário achar que é bug] → Rótulo deixa claro que é uma estimativa ("~"), não um valor exato de corrida.
- [`distanciaKm` ficar em 0/desatualizada se ninguém informar o valor] → Aceitável — é metadado opcional informativo, sem efeito em nenhuma lógica de jogo; um valor em 0 só significa "ainda não informado".

## Open Questions

- Valor exato das constantes de ganho médio pode ser ajustado durante a implementação/validação (comparando com tempos de volta reais conhecidos de alguns circuitos) — não afeta a forma pública das propriedades nem os cenários de spec.

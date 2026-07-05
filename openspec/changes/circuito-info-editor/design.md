## Context

`ciclo` (tempo entre ticks de simulação, em ms) vive hoje em `properties/circuitos.properties` (`<arquivo>=<NomeExibicao>,<ciclo>,<ativo>`), carregado para o mapa estático `ControleRecursos.circuitosCiclo` e consultado por `InterfaceJogo.tempoCicloCircuito()` (implementado em `ControleJogoLocal`/`JogoCliente` como `circuitosCiclo.get(circuito.getNome())`). `ControleCiclo.run()` faz `Thread.sleep(controleJogo.tempoCicloCircuito())` a cada tick — ou seja, `ciclo` é literalmente quantos milissegundos reais separam um tick do próximo, não uma contagem de voltas. `Piloto.processarCiclo()`/`calculaModificadorPrincipal()` calculam, a cada tick, quantos "nós" de `pistaFull` o piloto avança (`ganho`), com faixas de valor diferentes por tipo de nó (reta/largada, curva alta, curva baixa) antes de qualquer modificador de carro/piloto/clima. `Circuito.vetorizarPista()` já expõe `pistaFull` (uma entrada por pixel do traçado, via `GeoUtil.drawBresenhamLine`) com o `tipo` de cada nó preservado.

## Goals / Non-Goals

**Goals:**
- `ciclo` sai de `circuitos.properties` e vira uma propriedade comum de `Circuito`, editável no editor como qualquer outro campo (`probalidadeChuva`, `multiplicadorLarguraPista`, etc.).
- Tempo de volta estimado e distância em km aparecem no editor, recalculados sempre que `ciclo` ou o traçado mudam — ferramentas de apoio para calibrar `ciclo`, não um substituto da simulação real.
- Migração dos circuitos existentes preserva os valores de `ciclo` hoje gravados em `circuitos.properties`.

**Non-Goals:**
- Não reimplementar ou alterar `Piloto.calculaModificadorPrincipal()` — o cálculo do editor só lê valores representativos (aproximados) da mesma faixa usada em corrida, sem tocar a fórmula real nem replicar toda a árvore de modificadores (carro, clima, dano, ERS, etc.).
- Não persistir o tempo de volta estimado nem a distância em km no XML do circuito — são calculados em memória a partir de `pistaFull`/`ciclo` toda vez que o editor precisa exibi-los, não um dado gravado.
- Não mudar a semântica de `tempoCicloCircuito()` nem o loop de `ControleCiclo` — só de onde o valor vem.

## Decisions

### `ciclo` como campo simples de `Circuito`, populado a partir de `circuitos.properties` só durante a migração
`Circuito` ganha `private int ciclo = 160;` (valor por padrão dentro da faixa hoje usada, 140-240ms) com getter/setter, seguindo o mesmo padrão de `probalidadeChuva`. Persistido no arquivo de metadados (`_mro_meta.xml`, ver capability `circuito-metadados-arquivo`), junto dos demais campos leves. `circuitos.properties` perde o campo (linha passa de 3 para 2 valores: `NomeExibicao,ativo`), e `ControleRecursos.carregarCircuitos()`/`ControleJogoLocal.tempoCicloCircuito()`/`JogoCliente.tempoCicloCircuito()` passam a usar `circuito.getCiclo()` — eliminando a necessidade do mapa estático `circuitosCiclo` para esse fim (mantido só se algum outro consumidor depender dele; a leitura de código não achou nenhum).

### Ganhos médios por tipo de nó: constantes aproximadas, derivadas por inspeção de `calculaModificadorPrincipal()`
`Piloto.calculaModificadorPrincipal()` retorna, por tipo de nó, uma faixa de valores-base (antes de modificadores de carro/piloto/clima/dano): reta/largada 30-50, curva alta 20-30, curva baixa 10-20. O cálculo do editor usa a média de cada faixa como "ganho médio" representativo: `GANHO_MEDIO_RETA = 40`, `GANHO_MEDIO_CURVA_ALTA = 25`, `GANHO_MEDIO_CURVA_BAIXA = 15` — constantes do próprio cálculo do editor (não uma refatoração de `Piloto`), documentadas com a faixa de onde vieram para facilitar reajuste se a fórmula de corrida mudar.
- Alternativa descartada — chamar `Piloto.calculaModificadorPrincipal()` de verdade a partir do editor: exigiria instanciar um `Piloto`/`Carro`/`ControleJogo` completos só para estimar um número aproximado, um custo de acoplamento desproporcional ao "não precisa ser exato" pedido.

### Tempo de volta estimado: ticks por tipo de nó, multiplicados por `ciclo`
Para cada nó de `circuito.getPistaFull()`, soma-se `1 / ganhoMedio(tipoDoNo)` (nós de reta e largada tratados igual, via `No.verificaRetaOuLargada()`) para obter o total de ticks de uma volta; multiplicado por `ciclo` (ms/tick) dá o tempo estimado em milissegundos, formatado como `mm:ss.SSS` (reaproveitando o mesmo formato usado em `ControleEstatisticas.formatarTempo`, se exposto, ou uma formatação equivalente local ao editor).

### Distância em quilômetros: tamanho de `pistaFull` × escala fixa pixels-por-metro
`pistaFull.size()` é aproximadamente o comprimento da pista em pixels (um nó por pixel do traçado, via Bresenham). Converte-se para metros com uma constante `METROS_POR_PIXEL`, derivada do comprimento aproximado de um carro de F1 real (~5,5m) dividido pelo tamanho em pixels do carro no jogo (`Carro.ALTURA = 24`), ou seja `METROS_POR_PIXEL ≈ 5.5 / 24 ≈ 0.229`. Distância em km = `pistaFull.size() * METROS_POR_PIXEL / 1000`.
- Alternativa descartada — pedir ao usuário para informar a escala real do circuito: mais preciso, mas o pedido explicitamente aceita uma aproximação; uma constante fixa é mais simples e já dá uma ordem de grandeza plausível (circuitos com pistaFull maior mostram distância maior, de forma consistente).

### Exibição no editor: labels somente-leitura, recalculados junto com `vetorizarCircuito()`
Tempo de volta estimado e distância em km aparecem como `JLabel` (não editáveis) na barra superior do editor, atualizados no mesmo ponto em que `vetorizarCircuito()`/`refletirCircuitoNosCampos()` já rodam (após carregar um circuito, e após qualquer edição de nó que dispare revetorização) — para não exigir um novo gatilho de atualização.

## Risks / Trade-offs

- [Migração perder algum valor de `ciclo` se um circuito em `circuitos.properties` não tiver um arquivo de circuito correspondente, ou vice-versa] → O utilitário de migração itera as linhas de `circuitos.properties`, lê o `ciclo` de cada uma antes de removê-lo, e só grava no circuito correspondente se o arquivo existir — circuitos sem correspondência ficam registrados no log da migração para conferência manual.
- [Constantes de ganho médio ficarem desatualizadas se `calculaModificadorPrincipal()` mudar no futuro] → Aceito conscientemente; o comentário no código aponta para o método de origem, facilitando reajuste manual quando a fórmula de corrida mudar.
- [Estimativa de tempo de volta/distância dar números "estranhos" para o usuário achar que é bug] → Rótulos deixam claro que são estimativas ("~"), não valores exatos de corrida.

## Open Questions

- Valor exato de `METROS_POR_PIXEL` e das constantes de ganho médio pode ser ajustado durante a implementação/validação visual (comparando com tempos de volta reais conhecidos de alguns circuitos) — não afeta a forma pública das propriedades nem os cenários de spec.

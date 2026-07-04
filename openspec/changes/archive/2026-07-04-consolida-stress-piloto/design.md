## Context

O estresse do piloto (`Piloto.stress`, 0-100) é mutado exclusivamente via `incStress(int)`/`decStress(int)` (únicos pontos que tocam o campo — já centralizam cap 0-100, sorteio de RNG por posição, e o efeito colateral de sair do modo AGRESSIVO ao passar de 99). O que está espalhado não é o *mecanismo* de mudar o estresse, e sim a *decisão* de quando e quanto chamar esses métodos — hoje em 8 pontos, 4 classes:

| # | Local | Condição de disparo | Valor |
|---|-------|---------------------|-------|
| 1 | `Piloto.processaPneusIncomaptiveis()` | pneu incompatível com o clima + curva baixa | `incStress(testeHabilidadePiloto() ? 0 : 4)` |
| 2 | idem, curva alta | pneu incompatível com o clima + curva alta | `incStress(testeHabilidadePiloto() ? 0 : 2)` |
| 3 | `Piloto.processaPenalidadeColisao()` | colisão em andamento (`getColisao() != null`) | `incStress(1)` |
| 4 | idem | colisão + `evitaBaterCarroFrente` | `incStress(1)` adicional |
| 5 | `Piloto.processaFreioNaReta()` | curva baixa + `retardaFreiandoReta` + top-3 + sorteio > 0.9 + `!testeHabilidadePilotoFreios()` | `incStress(10 - desgastePneus/100)` |
| 6 | `Piloto.decrementaPilotoDesconcentrado()` | modo AGRESSIVO + `stress < 70` + desconcentrado | `incStress(1)` |
| 7 | `Carro.calculaDesgastePneus()`, curva baixa | sempre que no é curva baixa | `incStress(testeHabilidadePilotoAerodinamicaFreios() ? incStress/2 : incStress)`, onde `incStress = 10 - desgastePneus/100` |
| 8 | idem, curva baixa | `!chovendo && ptosBox==0 && stress>80` | `decStress(testeHabilidadePiloto() ? decStress : decStress/2)`, onde `decStress = desgastePneus/100` |
| 9 | idem, curva alta | `!chovendo && ptosBox==0 && stress>70` | `decStress(...)` mesma fórmula |
| 10 | idem, reta/largada | `stress>60 && !chovendo && ptosBox==0` | `incStress(testeHabilidadePiloto() ? incStress/2 : incStress)` |
| 11 | `ControleCorrida.danificaAreofolio()` | acidente com perda de aerofólio (via `verificaAcidente`) + `testeHabilidadePiloto()` + sorteio < fatorAcidente | `incStress(15)` |
| 12 | `ControleBox` (fila do box) | piloto avançando na fila de box (`ptosBox < boxList.size()`) | `decStress(2)` a cada tick |

`Piloto.processaStress()` já roda a cada tick (chamado em `moveCarro`/tick principal, logo após `processaGanho()`) e hoje só faz o decaimento passivo por modo:

```java
private void processaStress() {
    int fatorStresse = controleJogo.getRandom().intervalo(1, 5);
    if (curva alta ou baixa) fatorStresse /= 2;
    if (NORMAL) decStress(fatorStresse);
    else if (LENTO) decStress(fatorStresse * (testeHabilidadePiloto() ? 2 : 1));
}
```

Este é um refactor de organização — nenhum valor ou condição muda, só o local onde a decisão é tomada. Ver [[consolida-stress-piloto]] (proposal.md) para motivação completa.

## Goals / Non-Goals

**Goals:**
- Toda decisão "quando e quanto o estresse muda" vive dentro de `Piloto.processaStress()`.
- Os 4 pontos externos a `Piloto` (`Carro`, `ControleCorrida`, `ControleBox`) não chamam mais `incStress`/`decStress` diretamente.
- Nenhuma mudança de valor/frequência agregada de estresse ao longo de uma corrida — só de organização de código.

**Non-Goals:**
- Não mexe no mecanismo interno de `incStress`/`decStress` (cap, RNG por posição, saída do modo AGRESSIVO aos 99).
- Não muda a lógica de desgaste de pneu, colisão, acidente ou box em si — só remove o efeito colateral de estresse de dentro delas.
- Não preserva a ordem exata de sorteios de RNG no tick (ver Risco 1).

## Decisions

### 1. Duplicar condições de gatilho dentro de `processaStress()`, não parametrizar chamadas externas

Decisão (confirmada com o usuário): ao invés de `Carro`/`ControleCorrida`/`ControleBox` chamarem `piloto.processaStress(delta)` com o valor já calculado, `processaStress()` reavalia por conta própria as mesmas condições hoje espalhadas (tipo de nó atual, desgaste de pneu do carro, estado de colisão, avanço na fila do box) e decide sozinho quando chamar `incStress`/`decStress`.

**Motivo**: mesmo padrão já usado em `Piloto.processaTravouRodas()` (decoupling do trava-rodas do desgaste de pneu) — desacoplamento completo, sem os locais originais precisarem saber que estão contribuindo pro estresse.

**Alternativa descartada**: manter cada local decidindo e apenas centralizar a escrita via `processaStress(delta)`. Mais simples e sem duplicação, mas não atinge o objetivo do usuário — a lógica de "quando" continuaria espalhada, só a chamada final mudaria de nome.

### 2. Locais originais viram leitura pura, nunca escrita

Onde o valor de estresse é usado para decidir OUTRO efeito no mesmo tick (ex.: `Carro.calculaDesgastePneus` usa `stress > 80` pra decidir se `teste = false`, o que afeta `desgPneus`), o local continua **lendo** `getStress()` normalmente. Só a chamada de escrita (`incStress`/`decStress`) sai de lá.

### 3. Acidente com perda de aerofólio: `processaStress()` só reage ao resultado, não reimplementa a decisão de acidente

`ControleCorrida.danificaAreofolio()` tem lógica de acidente substancial (sorteio de `fatorAcidente`, threads de mensagem atrasada, etc.) que não faz sentido duplicar dentro de `Piloto`. Solução: `danificaAreofolio()` continua decidindo *se* houve dano de aerofólio (responsabilidade dela), mas ao invés de chamar `incStress(15)` diretamente, seta um flag/contador em `Piloto` (ex.: `sofreuDanoAereofolioNesteTick`) que `processaStress()` consome e limpa no mesmo tick, chamando `incStress(15)` a partir daí. Mantém a decisão de acidente em `ControleCorrida` (correta arquiteturalmente) e a escrita de estresse centralizada em `Piloto.processaStress()`.

### 4. Fila do box: método dedicado `processaStressFilaBox()`, fora de `processaStress()`

Descoberta durante a implementação: `Piloto.processarCiclo()` (o método que chama `processaStress()`) só roda quando `getPtosBox() == 0` (`ControleCiclo`, linha ~75: `if (piloto.getPtosBox() == 0) { piloto.processarCiclo(); ... }`). `ControleBox.processarPilotoBox()` roda exatamente na janela oposta (`getPtosBox() != 0`, avançando a fila do box). Ou seja, `processaStress()` **nunca executa** no momento em que o decremento de estresse da fila do box deveria disparar — não tem como colocar essa condição dentro do corpo de `processaStress()`, porque esse método estruturalmente não roda ali.

**Decisão**: criar `Piloto.processaStressFilaBox()`, um método pequeno e dedicado (não faz parte de `processaStress()`), chamado diretamente por `ControleBox.processarPilotoBox()` no lugar do `decStress(2)` direto. Atende o pedido literal (nenhuma classe fora de `Piloto` chama `incStress`/`decStress` diretamente), mas essa única regra vive num método vizinho, não dentro do método guarda-chuva — como alternativa descartada, chamar o `processaStress()` inteiro a partir do box faria o decaimento passivo por modo de pilotagem e os demais gatilhos (colisão, pneu incompatível etc.) passarem a rodar também durante o box, o que nunca acontece hoje — mudança de comportamento real, não só de organização.

### 5. Ordem no tick: `processaStress()` move pra depois dos produtores de estado que ele consulta

Descoberta durante a implementação: `processaStress()` roda hoje na linha ~1125 do tick principal, **antes** de `processaColisao()` (1139), `processaPenalidadeColisao()` (1140), `processaEvitaBaterCarroFrente()` (1137), `processaFreioNaReta()` (1136) e `decrementaPilotoDesconcentrado()` (1148). Duplicar as condições de colisão/freio-na-reta/desconcentração dentro de `processaStress()` sem mudar sua posição faria a leitura de `getColisao()`/`evitaBaterCarroFrente`/`ciclosDesconcentrado` refletir o estado do **tick anterior**, não deste tick — um bug de dado desatualizado, diferente (e mais sério) do que a simples mudança de ordem de sorteios de RNG já aceita.

**Decisão**: mover a chamada de `processaStress()` para depois de `processaColisao`, `processaEvitaBaterCarroFrente`, `processaFreioNaReta` e `decrementaPilotoDesconcentrado` no tick (próximo de onde `controleJogo.verificaAcidente(this)` já roda, linha ~1150). Com isso, `getColisao()` e `evitaBaterCarroFrente` já refletem o estado deste tick quando `processaStress()` os lê.

**Exceção — freio na reta**: `retardaFreiandoReta` é lido e resetado para `false` dentro do próprio bloco condicional de `processaFreioNaReta()` que hoje decide o incremento — mesmo com `processaStress()` reposicionado, o sinal já teria sido apagado antes de `processaStress()` rodar. Para esse único gatilho, usa-se o mesmo padrão de flag consumível da Decisão 3 (ex.: `freioNaRetaMalSucedidoNesteTick` guardando a magnitude `10 - desgastePneus/100`): `processaFreioNaReta()` continua avaliando a condição e resetando `retardaFreiandoReta` exatamente como hoje, mas ao invés de chamar `incStress` diretamente, seta esse flag; `processaStress()` consome e limpa o flag.

`processaPneusIncomaptiveis()` (não depende de estado calculado por outro método no mesmo tick, só de `getNoAtual()` e `testeHabilidadePiloto()`) e os 4 gatilhos de `Carro.calculaDesgastePneus` (chamados por `processaGanho()`, que já roda antes de `processaStress()` mesmo na nova posição) não têm esse problema — permanecem como duplicação direta de condição, sem necessidade de flag.

## Risks / Trade-offs

- **[Risco] Determinismo de RNG muda** → resultados de simulação com seed fixa (testes, replays) deixam de bater com o comportamento anterior à mudança. **Mitigação**: nenhuma necessária no código; testes que fixam seed e comparam trajetória exata precisam ser atualizados (não há testes desse tipo hoje identificados para estresse especificamente).
- **[Risco] `ControleBox` precisa expor o tamanho da fila de box pra `Piloto`** → novo acoplamento de leitura (`Piloto` -> `ControleBox`/`InterfaceJogo`). **Mitigação**: expor via método existente `getPtosBox()` comparado a um novo getter simples em `InterfaceJogo` (ex.: `tamanhoFilaBox()`), evitando expor a lista inteira.
- **[Risco] Acidente de aerofólio via flag intermediário pode gerar estado obsoleto** se `processaStress()` não rodar no mesmo tick em que o flag foi setado (ex.: ordem de chamada mudar no futuro) → estresse do acidente seria aplicado um tick atrasado ou nunca. **Mitigação**: `processaStress()` já roda todo tick sem early-return condicional (diferente de `processaTravouRodas`, que tem vetos); documentar essa dependência de ordem explicitamente no código.
- **[Risco] Duplicação de condições = duas fontes de verdade temporárias durante a migração** → se só parte dos 12 pontos for migrada, há dupla contagem de estresse (local antigo ainda chama incStress E processaStress também dispara). **Mitigação**: tasks.md deve migrar e remover em um único commit por local, nunca deixar os dois ativos simultaneamente; testes devem cobrir "local original não chama mais incStress/decStress" para cada um dos 8 pontos.

## Migration Plan

1. Adicionar os 12 gatilhos dentro de `processaStress()`, um de cada vez, cada um seguido da remoção da chamada original e do teste correspondente atualizado (evita a dupla-contagem do Risco acima).
2. Ordem sugerida: primeiro os 5 já dentro de `Piloto.java` (mesma classe, sem novo acoplamento), depois `Carro.calculaDesgastePneus` (4 pontos), depois `ControleBox` (exige novo getter), por último `ControleCorrida.danificaAreofolio` (exige o flag intermediário).
3. Rodar `mvn -o clean test` após cada ponto migrado.

## Open Questions

Nenhuma — as duas decisões de design em aberto (estratégia de consolidação e aceitação de mudança de ordem de RNG) foram confirmadas com o usuário antes deste documento.

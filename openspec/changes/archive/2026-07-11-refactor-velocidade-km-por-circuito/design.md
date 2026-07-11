## Context

`Piloto.calculoVelocidade(double ganho)` (`Piloto.java:1306`) hoje é:

```java
private int calculoVelocidade(double ganho) {
    int val = 290;
    double porcent = getCarro().getPorcentagemCombustivel() / 100.0;
    val += (21 - (porcent / 5.0));
    boolean naReta = ...;
    return Util.inteiro(((val * ganho * (naReta ? 1 : 0.7) / ganhoMax) + ganho * (naReta ? 1 : 0.7)));
}
```

`ganho` é o quanto o índice do piloto avança na lista de nós da pista (`getNosDaPista()`, que é `circuito.getPistaFull()` — a versão interpolada/densa do traçado) a cada ciclo de simulação (`tempoCicloCircuito()` = `circuito.getCiclo()` ms). Não há nenhuma relação com o tamanho real do circuito — `330`/`320`/`290` são números de balanceamento escolhidos por tentativa, sem lastro físico.

`Circuito.distanciaKm` (adicionado em `circuito-info-editor`) guarda a extensão real da pista. Os dois circuitos que já têm o valor preenchido guardam **metros** num campo `int` (Albert Park = `5303`, Interlagos = `4309`, batendo com os comprimentos oficiais de 5,303 km e 4,309 km) — apesar do nome do campo e do label do editor ("Distância (km)") sugerirem quilômetros. Isso é consequência de `distanciaKm` ter sido convertido de `double` pra `int` (commit `922d33e0`/seguinte) sem ajustar a escala: um `int` não representa `5.303` km sem perda, então o valor informado passou a ser em metros. O restante desta mudança assume esse fato (ver Open Questions).

35 dos 37 circuitos ainda têm `distanciaKm == 0` (nunca foi preenchido). Para esses, não existe dado real de extensão pra calcular km/h — a fórmula nova precisa de um fallback.

## Goals / Non-Goals

**Goals:**
- Velocidade instantânea (`Piloto.velocidade`) calculada a partir do avanço real de índice por ciclo (`ganho`), relativizado ao comprimento real do circuito (`distanciaKm`) e ao tempo de ciclo (`tempoCicloCircuito()`), resultando num km/h fisicamente coerente para circuitos com `distanciaKm` informado.
- Teto de ~370–375 km/h: a velocidade calculada nunca ultrapassa 375; ao atingir a faixa de teto, o valor passa a oscilar entre 370 e 375 (efeito de "limitador"), em vez de travar num número fixo.
- Circuitos com `distanciaKm == 0` continuam com o comportamento atual (fórmula antiga), sem regressão visual pros 35 circuitos ainda não preenchidos.
- Consolidar as constantes de teto de velocidade hoje duplicadas (`290`, `320`, `330`) numa única referência usada tanto no cálculo quanto na escala de cor do velocímetro (`PainelCircuito.desenhaVelocidade`).
- Efeito artificial de "reta sustentada": depois de 3s contínuos numa reta (fora do traçado de fuga), a velocidade passa a subir sozinha até o teto, ignorando o `ganho` real, simulando o carro esticando a reta até o limite — pedido explicitamente como uma dramatização, não como física mais precisa.

**Non-Goals:**
- Preencher `distanciaKm` dos outros 35 circuitos (trabalho de conteúdo/dados, não desta mudança).
- Renomear o campo `distanciaKm` ou mudar sua unidade/serialização (fica pra uma mudança futura, se o time decidir formalizar "metros").
- Alterar a física de ultrapassagem/colisão/turbulência que também consome `ganho` — só o cálculo de velocidade exibida muda.
- Alterar `calculaVelocidadeExibir()` (suavização da agulha) — ela já limita o incremento por ciclo e vai naturalmente amortecer a oscilação do teto num flutuar sutil, sem precisar de mudança própria.

## Decisions

### 1. Fórmula: km/h real a partir de índice/ciclo e `distanciaKm`

```
kmPorNo   = (distanciaKm / 1000.0) / nosDaPista.size()   // km reais por unidade de índice
kmPorCiclo = ganho * kmPorNo
ciclosPorHora = 3_600_000.0 / tempoCicloCircuito()        // ms -> ciclos/hora
velocidadeKmh = kmPorCiclo * ciclosPorHora
```

Simplificando: `velocidadeKmh = (ganho * distanciaKm * 3600.0) / (nosDaPista.size() * tempoCicloCircuito())`, com `distanciaKm` tratado como metros (÷1000 embutido no `3600.0`, já que `3_600_000 / 1000 = 3600`).

Isso é exatamente o pedido: "referência de movimento, índice/pixel, com relação ao tamanho do circuito, extensão em quilômetros, calculando quilometragem por hora" — usa o mesmo `ganho` que já move o piloto (sem introduzir um segundo sistema de física), só reprojeta esse avanço em km/h reais usando dados que já existem (`nosDaPista.size()`, `distanciaKm`, `tempoCicloCircuito()`).

**Alternativa considerada:** manter a fórmula antiga e só reescalar seu resultado pra "parecer" km/h (ex.: multiplicar por um fator fixo). Rejeitada — não usaria `distanciaKm` de verdade, então dois circuitos com extensões bem diferentes continuariam mostrando a mesma velocidade pro mesmo `ganho`, o que é exatamente o problema que a proposta quer resolver.

### 2. Teto com oscilação, não clamp fixo

Quando `velocidadeKmh` calculada (real ou fallback) ultrapassa `TETO_VELOCIDADE_MIN` (370), o valor exibido passa a alternar entre `TETO_VELOCIDADE_MIN` (370) e `TETO_VELOCIDADE_MAX` (375) a cada ciclo em que o piloto seguir "no limite" — mesmo padrão sobe/desce já usado em `OcilaCor.ocila()`, mas como estado por `Piloto` (não por chave estática global, já que cada piloto precisa oscilar de forma independente): dois campos novos (`velocidadeTetoOscilacao`, `velocidadeTetoSubindo`) e um pequeno método que empurra o valor 1 km/h por ciclo em direção ao extremo oposto ao chegar em cada ponta.

Isso nunca deixa `velocidade` passar de 375. Como `calculaVelocidadeExibir()` só aproxima `velocidadeExibir` de `velocidade` (nunca ultrapassa o alvo), o valor exibido na tela também fica implicitamente limitado a 375 — sem precisar de um teto redundante na camada de exibição.

**Alternativa considerada:** clamp simples (`Math.min(velocidade, 375)`), sem oscilação. Rejeitada por não atender ao pedido explícito do usuário ("nunca passasse disso... ficasse oscilando entre esses valores") — um clamp fixo trava a agulha, não dá a sensação de limitador.

### 3. Fallback para `distanciaKm == 0`

Quando `circuito.getDistanciaKm() == 0`, `calculoVelocidade` mantém a fórmula antiga (constantes 290/ganhoMax) intacta — sem dividir por zero e sem regressão visual nos 35 circuitos que ainda não têm a distância cadastrada. O teto com oscilação (370–375) passa a valer também pro fallback, já que é uma melhoria independente de `distanciaKm` (hoje o teto informal já gira em torno de 320–330).

**Alternativa considerada:** exigir `distanciaKm` preenchido em todos os circuitos antes de fazer essa mudança (migração de dados primeiro). Rejeitada — bloquearia a entrega por um trabalho de conteúdo sem relação com a lógica, e o fallback já cobre o caso com segurança.

### 4. Constante única de teto

Extrair `TETO_VELOCIDADE_MIN = 370` e `TETO_VELOCIDADE_MAX = 375` (ou nome equivalente) como constantes em `Piloto` (ou `Global`, se outras classes precisarem — hoje só `Piloto` e `PainelCircuito` usam número de teto). `PainelCircuito.desenhaVelocidade()` troca o `330` fixo da escala de cor (`porcentVermelho100Verde0((100 * velocidade / 330))`) pela nova constante de teto, então a barra de cor sempre bate 100% exatamente no teto real.

### 5. Reta sustentada: rampa artificial só no valor exibido, nunca na velocidade real

Primeira versão desta decisão aplicava o incremento artificial dentro de `calculoVelocidade` (a velocidade "real"), mas o usuário pediu explicitamente pra restringir o efeito ao "valor visual da quilometragem" — a velocidade real também alimenta `ControleJogosServer.dadosParciais.velocidade` (sincronizada com clientes multiplayer) e a intensidade dos efeitos visuais de chuva em `PainelCircuito` (`piloto.getVelocidade() / 320.0`), então injetar um valor artificial ali corromperia esses consumidores, não só o velocímetro. A implementação final move o efeito inteiro pra `calculaVelocidadeExibir()` (que já é, por definição, só a camada de exibição/suavização da agulha), deixando `calculoVelocidade`/`Piloto.velocidade` inteiramente livres da rampa.

Um contador por piloto (`tempoContinuoNaRetaMs`) acumula `tempoCicloCircuito()` a cada ciclo em que `noAtual.verificaRetaOuLargada()` for verdadeiro e o piloto não estiver no traçado de fuga (`getTracado() == 4 || == 5`); zera assim que uma dessas condições deixar de valer. Ao atingir `LIMIAR_RETA_SUSTENTADA_MS` (3000ms), `calculaVelocidadeExibir()` para de suavizar `velocidadeExibir` em direção a `velocidade` e passa a incrementar `velocidadeExibir` do ciclo anterior em `INCREMENTO_VELOCIDADE_RETA_SUSTENTADA` (2 km/h), sujeito a um teto com oscilação 370–375 próprio (`velocidadeExibirTetoOscilacao`/`velocidadeExibirTetoSubindo` — campos e lógica de ping-pong deliberadamente duplicados dos equivalentes `velocidadeTeto*` usados por `aplicaTetoVelocidade`, e não compartilhados: os dois métodos rodam no mesmo ciclo de jogo — `processarCiclo` chama `calculoVelocidade` antes de `calculaVelocidadeExibir` chamar essa rampa — então usar o mesmo estado faria os dois pipelines pisarem um no outro).

Por que ler `velocidadeExibir` (o campo, valor do ciclo anterior) em vez de recalcular do zero: mantém uma subida contínua (sem salto) a partir de onde a agulha já estava, em vez de reiniciar de um valor arbitrário — o mesmo raciocínio da primeira versão, só que agora aplicado ao valor exibido em vez do real.

Por que também checar o traçado de fuga (e não só o tipo do nó): o mesmo problema já resolvido em `PilotoGanhoTracadoDeFugaTest` — o nó da pista principal (`noAtual`) não muda de tipo quando o piloto está fisicamente na escapada (traçado 4/5), já que o índice usado é sempre o da pista principal. Sem essa checagem, um piloto na escapada sobre um trecho de reta da pista principal ativaria a subida artificial mesmo estando fisicamente numa via de fuga, o que o usuário pediu explicitamente pra excluir.

**Alternativa considerada (rejeitada nesta correção):** manter o efeito em `calculoVelocidade`/`velocidade`. Rejeitada porque `velocidade` não é só um valor de exibição — é lido por `ControleJogosServer` (rede) e pelos efeitos de chuva (`PainelCircuito`), então a rampa artificial vazaria pra esses consumidores. Movida integralmente pra `calculaVelocidadeExibir()`/`velocidadeExibir`, que é exclusivamente a camada visual.

**Alternativa considerada, depois adotada numa correção seguinte:** gatilho também checando a zona de frenagem (não ativar a rampa se o piloto estiver numa zona de frenagem, mesmo ainda em nó de reta). Inicialmente rejeitada — ver decisão 6, que reverte essa rejeição a pedido do usuário.

### 6. Zona de frenagem desativa o incremento artificial (sem resetar a contagem)

A decisão 5 rejeitou gatear a rampa por frenagem, mas o usuário pediu explicitamente: "o incremento artificial de velocidade [deveria ser] desativado na zona de frenagem". `calculaVelocidadeExibir()` agora também checa `controleJogo.isNoZonaFrenagem(noAtual)` — o mesmo detector de zona de frenagem já usado por `processaFreioNaReta()` e pelas specs `zona-frenagem-*` (pré-calculado por circuito: nós de reta na aproximação de um cluster de curva baixa, mais o próprio cluster) — antes de ativar `aplicaRetaSustentadaNaVelocidadeExibir()`: `tempoContinuoNaRetaMs >= LIMIAR_RETA_SUSTENTADA_MS && !controleJogo.isNoZonaFrenagem(noAtual)`.

Por que `isNoZonaFrenagem` e não `isFreiandoReta()`: `isNoZonaFrenagem` é a detecção geométrica direta ("este nó pertence a uma zona de frenagem"), pré-computada por circuito e independente de estado de execução — bate literalmente com o termo que o usuário usou ("zona de frenagem"). `isFreiandoReta()` é um flag de comportamento em tempo de execução, setado por `processaFreioNaReta()` com lógica adicional (só dispara se a próxima curva for especificamente curva baixa, com multiplicadores probabilísticos) — um proxy indireto e mais restrito pra a mesma ideia.

Por que só suspender o incremento, sem resetar `tempoContinuoNaRetaMs`: a zona de frenagem é o trecho final de uma reta (nós de reta na aproximação, mais o cluster de curva baixa); resetar o contador ao entrá-la faria a rampa nunca reengatar caso a reta continuasse depois (situação rara, mas incoerente) e, mais relevante, faria o piloto precisar de outros 3s inteiros de reta depois de qualquer frenagem antes de a rampa voltar — mesmo já tendo passado bastante tempo numa reta contínua antes da zona. Suspender (sem zerar) faz o incremento retomar no mesmo ciclo em que o piloto sai da zona, se a contagem já estava acima do limiar, o que corresponde ao comportamento esperado: a "artificialidade" desliga só durante a frenagem, sem penalizar o resto da reta sustentada.

### 7. Incremento tênue acima de 300 km/h (teto só em retas bem longas)

O usuário pediu: "acima dos 300 km/h... o incremento gradual [deveria] se tornar mais tênue para que o teto só fosse atingido em retas muito longas". `calculaIncrementoRetaSustentada()` substitui o incremento fixo de 2 km/h/ciclo por três faixas, todas lidas a partir de `getVelocidadeExibir()` (o valor sendo incrementado, não `velocidade`):

- Abaixo de `LIMIAR_VELOCIDADE_INCREMENTO_TENUE` (300): incremento cheio, `INCREMENTO_VELOCIDADE_RETA_SUSTENTADA` (2) por ciclo — comportamento inalterado nessa faixa.
- De 300 até `LIMIAR_VELOCIDADE_INCREMENTO_MUITO_TENUE` (340): metade, 1 por ciclo.
- A partir de 340: só 1 km/h a cada `CICLOS_POR_INCREMENTO_MUITO_TENUE` (3) ciclos, usando `tempoContinuoNaRetaMs / tempoCicloCircuito()` como relógio (`% 3 == 0`) — sem precisar de um campo de estado novo, já que `tempoContinuoNaRetaMs` já existe e cresce 1 `tempoCicloCircuito()` por ciclo enquanto a reta sustentada estiver ativa.

Por que faixas discretas (2 → 1 → 1-a-cada-3-ciclos) em vez de uma curva contínua de desaceleração: com `INCREMENTO_VELOCIDADE_RETA_SUSTENTADA` valendo só 2 km/h, uma fórmula proporcional contínua (`incremento = INCREMENTO * (teto - atual) / (teto - 300)`) colapsa quase toda a faixa 300–370 pra "0 ou 1" por causa de arredondamento/truncamento inteiro — ou seja, na prática vira a mesma coisa que uma faixa discreta, só que com uma fórmula bem menos legível. As faixas discretas replicam o padrão que `calculaVelocidadeExibir()` já usa pra `incAcell`/`incFreiada` (degraus fixos em 100/200 km/h), mantendo o mesmo idioma do arquivo.

Por que reusar `tempoContinuoNaRetaMs` em vez de outro contador: evita adicionar mais um campo (`@JsonIgnore`) só pra saber "faz quantos ciclos que estou nessa faixa" — como o piloto só entra na faixa "muito tênue" depois de já estar continuamente na reta sustentada, o número de ciclos decorridos desde o início da reta (dividido pelo tamanho do ciclo) já serve como um relógio determinístico o bastante pra intercalar os incrementos.

O teto continua tecnicamente alcançável (nunca estagna em 0 pra sempre) — só que numa reta de ~340 até 370 km/h a 1km/h a cada 3 ciclos, são ~90 ciclos só nesse trecho (~13-20s dependendo do `ciclo` do circuito), ou seja, uma reta bem longa — nem toda pista de F1 tem uma reta capaz de sustentar isso sem cruzar uma curva ou zona de frenagem antes.

## Risks / Trade-offs

- [`distanciaKm` armazenado em metros mas nomeado/rotulado como "km"] → Mitigação: fórmula trata explicitamente o valor como metros (`distanciaKm / 1000.0`); documentar essa decisão no código (comentário) pra não reintroduzir o bug de escala numa mudança futura.
- [Fórmula real pode gerar velocidades muito baixas ou instáveis pra combinações incomuns de `distanciaKm`/`nosDaPista.size()`/`ciclo`, já que esses valores foram calibrados independentemente ao longo do tempo] → Mitigação: teto de 370–375 já limita o topo; não há garantia formal de piso, mas o comportamento herdado (ganho→velocidade proporcional) evita valores negativos ou zerados fora de paradas reais (safety car, box, grid).
- [Apenas 2 circuitos exercitam a fórmula nova hoje — cobertura de teste limitada em cenários reais de jogo] → Mitigação: testes unitários com `distanciaKm` sintético cobrindo faixas baixas/médias/teto, sem depender só dos XMLs existentes.

## Migration Plan

Sem migração de dados — mudança é só de código, e o fallback garante que os 35 circuitos sem `distanciaKm` não mudam de comportamento. Nenhum arquivo de circuito precisa ser tocado. Rollback é reverter o commit; nenhum estado persistido é criado por esta mudança (o teto/oscilação vive só em memória, por piloto, durante a corrida).

## Open Questions

- `distanciaKm` guardar metros com nome/label de "km" é uma inconsistência pré-existente (fora do escopo desta mudança corrigir) — vale abrir uma mudança futura pra renomear o campo (`distanciaMetros`) ou ajustar o label/parse do editor pra aceitar km com casas decimais?
- O teto 370–375 km/h é um valor fixo pra todos os circuitos, mesmo os que não tem distanciaKm (fallback). Faz sentido variar esse teto por circuito (ex.: retas mais longas permitem topo mais alto) numa iteração futura, ou 370–375 fixo é suficiente por ora?

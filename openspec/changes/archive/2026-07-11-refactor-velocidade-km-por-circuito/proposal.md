## Why

O velocímetro exibido durante a corrida (`Piloto.calculoVelocidade()` / `PainelCircuito.desenhaVelocidade()`) hoje é um número de balanceamento de jogo sem relação com a física real: usa constantes soltas e divergentes (290, 320 e 330) e é derivado apenas do `ganho` (avanço de índice por ciclo), sem nenhuma referência ao tamanho real do circuito. Agora que `Circuito.distanciaKm` guarda a extensão real do autódromo (ver `circuito-info-editor`), dá para calcular uma velocidade km/h de verdade a partir do quanto o piloto avança na pista por ciclo, relativizado ao comprimento real da volta — com um teto realista (~370–375 km/h) em vez de um número arbitrário.

## What Changes

- Nova fórmula de velocidade instantânea: converte o `ganho` (avanço de índice por ciclo) em km/h real, usando `distanciaKm` do circuito e a quantidade de nós da pista (`getNosDaPista().size()`) como referência de "quanto índice = quantos km", e `tempoCicloCircuito()` (ms/ciclo) como referência de tempo.
- Teto de velocidade (~370–375 km/h): ao atingir o teto, a velocidade não passa dele — em vez de travar num valor fixo, oscila suavemente entre os dois extremos da faixa (efeito parecido com o "sobe/desce" já usado em `OcilaCor`), simulando o carro no limite.
- Circuitos sem `distanciaKm` informado (valor 0 — hoje é o caso de 35 dos 37 circuitos) mantêm o comportamento atual (fórmula antiga baseada só em `ganho`), já que não há dado real de extensão para calcular km/h de verdade.
- Consolidação das constantes de velocidade máxima hoje espalhadas (290 em `calculoVelocidade`, 320 em outro cálculo de giro do motor, 330 na escala de cor do velocímetro) numa única referência de teto, usada tanto para o cálculo quanto para a escala de cor do painel.

## Capabilities

### New Capabilities
- `velocidade-real-km-h`: como a velocidade instantânea do piloto é calculada a partir do avanço real na pista e da extensão do circuito em km, incluindo o comportamento de teto com oscilação e o fallback para circuitos sem `distanciaKm`.

### Modified Capabilities
(nenhuma — `circuito-info-editor` continua descrevendo `distanciaKm` como hoje; esta mudança só passa a consumir esse valor já existente, sem alterar seu requisito.)

## Impact

- `src/main/java/br/f1mane/entidades/Piloto.java` — `calculoVelocidade(double ganho)` (linha ~1306) e possivelmente `calculaVelocidadeExibir()` (linha ~3695, que usa o valor bruto pra suavizar a agulha).
- `src/main/java/br/f1mane/visao/PainelCircuito.java` — `desenhaVelocidade()` (linha ~5054), especificamente a escala de cor `porcentVermelho100Verde0((100 * velocidade / 330))`.
- `src/main/java/br/f1mane/entidades/Circuito.java` — nenhuma mudança de schema; só leitura de `getDistanciaKm()` e `getNosDaPista()` em novo local.
- Nenhum arquivo de circuito (`*_mro_meta.xml`) precisa mudar — comportamento novo só se ativa para circuitos que já têm `distanciaKm` != 0.
- Testes unitários de `Piloto`/`PainelCircuito` relacionados a velocidade precisam cobrir: cálculo com `distanciaKm` informado, fallback com `distanciaKm` zero, e o comportamento de oscilação no teto.

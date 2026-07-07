## ADDED Requirements

### Requirement: Backend expõe marcas de pneu produzidas por nó no payload de posição
`ControleJogosServer.gerarPosicaoPilotos` SHALL incluir, no payload de posição enviado ao cliente web, o snapshot completo das marcas de pneu (`TravadaRoda`, identificada por `idNo`+`tracado`) acumuladas na pista durante a corrida atual, reaproveitando o mesmo modelo `TravadaRoda`/`encode()` já usado no caminho do applet. Como cada cliente conectado faz seu próprio polling independente do mesmo jogo, o payload SHALL sempre conter o snapshot completo acumulado (não um delta desde o último poll daquele cliente específico), pra que nenhum cliente perca uma marca por ela ter sido "consumida" por outro.

#### Scenario: Travada de roda gerada aparece no snapshot enviado a qualquer cliente
- **WHEN** `ControleJogoLocal.geraTravadaRoda` gera uma `TravadaRoda` para algum piloto
- **THEN** o payload de posição enviado a qualquer cliente que pollar a partir desse momento inclui essa `TravadaRoda` (idNo + tracado) na lista de marcas, independente de qual cliente tenha feito o poll imediatamente após o evento

#### Scenario: Mesma posição gerando lock-up repetidas vezes não duplica entradas
- **WHEN** o mesmo par idNo+tracado gera uma nova `TravadaRoda` mais de uma vez ao longo da corrida (ex.: a cada volta no mesmo ponto de frenagem)
- **THEN** o snapshot enviado ao cliente contém apenas uma entrada para aquele idNo+tracado, não uma por ocorrência

### Requirement: Cliente web consome marcas por nó em vez de inferir por status de piloto
O cliente HTML5 (`cpu.js`/`vdp.js`) SHALL acumular e desenhar marcas de pneu com base na lista autoritativa de `TravadaRoda` recebida do backend (por nó), e não mais a partir da simples ocorrência do status `"T"`/`"M"` de um piloto no tick corrente.

#### Scenario: Marca recebida do backend é desenhada uma vez e mantida
- **WHEN** o cliente recebe uma `TravadaRoda` (idNo + tracado) que ainda não estava no seu conjunto acumulado de marcas
- **THEN** o cliente adiciona essa marca ao conjunto acumulado e a desenha na posição do nó correspondente, mantendo-a desenhada nos ticks seguintes sem depender de receber o mesmo evento de novo

#### Scenario: Marca já conhecida não é duplicada
- **WHEN** o cliente recebe novamente uma `TravadaRoda` com o mesmo idNo+tracado que já está no seu conjunto acumulado
- **THEN** o cliente não desenha uma segunda marca sobreposta para o mesmo nó

#### Scenario: Status por piloto continua controlando apenas a fumaça
- **WHEN** o cliente recebe o status `"T"` para um piloto no tick corrente
- **THEN** o cliente aciona o efeito transitório de fumaça para aquele piloto (via `pilotosTravadaFumacaMap`), sem que isso seja necessário para a marca de pneu correspondente já ter sido desenhada a partir da lista de `TravadaRoda`

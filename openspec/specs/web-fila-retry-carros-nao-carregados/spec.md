# web-fila-retry-carros-nao-carregados

## Purpose

Garantir que sprites de carro (carro de cima, carro de cima sem aerofólio, carro de lado, capacete) que falham ao carregar no cliente HTML5 continuem sendo tentados indefinidamente — em vez de desistir de vez após o retry rápido inicial esgotar suas tentativas — e que o cache de sprite rotacionado seja corrigido quando o carro finalmente carrega, evitando que o carro fique com o desenho quebrado na tela pelo resto da corrida.

## Requirements

### Requirement: Carro com sprite falhado entra na fila de retry
Quando as 4 imagens de um piloto (carro de cima, carro de cima sem aerofólio, carro de lado, capacete) são carregadas ou recarregadas via `mid_carregaImagensCarro` e qualquer uma delas falha em carregar após `mid_carregaImagemComRetry` esgotar suas tentativas, o cliente HTML5 SHALL enfileirar o piloto inteiro em `mid_filaCarrosNaoCarregados`, sem duplicar entradas para o mesmo piloto já presente na fila.

#### Scenario: Uma das 4 imagens falha definitivamente
- **WHEN** `mid_carregaImagensCarro` é chamada para um piloto e a imagem de capacete falha após esgotar as tentativas de `mid_carregaImagemComRetry`, mesmo que as outras 3 imagens carreguem com sucesso
- **THEN** o `piloto.id` é adicionado ao fim de `mid_filaCarrosNaoCarregados`

#### Scenario: Piloto já está na fila
- **WHEN** uma nova falha ocorre para um piloto cujo `id` já está em `mid_filaCarrosNaoCarregados`
- **THEN** nenhuma entrada duplicada é adicionada à fila

### Requirement: Fila é consumida a cada 5 segundos, um carro por vez
O cliente HTML5 SHALL processar a fila de carros não carregados através de um tique próprio de `MID_INTERVALO_FILA_CARROS_MS` (5000ms), registrado em `cpu.js`, retirando e tentando recarregar apenas o primeiro carro da fila a cada execução.

#### Scenario: Fila vazia
- **WHEN** o tique de `mid_processaFilaCarrosNaoCarregados` executa e `mid_filaCarrosNaoCarregados` está vazia
- **THEN** nenhuma tentativa de recarregamento é disparada

#### Scenario: Fila com múltiplos carros
- **WHEN** o tique executa com mais de um piloto na fila
- **THEN** apenas o primeiro piloto (início da fila) tem suas imagens recarregadas nesse tique; os demais permanecem na fila para os próximos tiques

### Requirement: Sucesso remove o carro definitivamente da fila; falha recoloca no fim
Ao reprocessar um carro retirado da fila, o cliente HTML5 SHALL: se as 4 imagens carregarem com sucesso, manter o carro fora da fila; se qualquer uma falhar novamente (após esgotar as tentativas de `mid_carregaImagemComRetry`), reenfileirar o piloto no fim de `mid_filaCarrosNaoCarregados` para nova tentativa no próximo tique.

#### Scenario: Retry bem-sucedido
- **WHEN** um carro retirado da fila tem as 4 imagens recarregadas com sucesso
- **THEN** o `piloto.id` não retorna à fila

#### Scenario: Retry falha novamente
- **WHEN** um carro retirado da fila falha novamente em qualquer uma das 4 imagens
- **THEN** o `piloto.id` é adicionado ao fim de `mid_filaCarrosNaoCarregados`, atrás dos demais carros já enfileirados

#### Scenario: Piloto não existe mais quando o tique dispara
- **WHEN** o tique retira da fila um `pilotoId` que não é encontrado em `pilotosMap` (ex.: piloto removido da corrida)
- **THEN** nenhuma tentativa de recarregamento é disparada para esse `pilotoId` e ele não retorna à fila

### Requirement: Cache de sprite rotacionado é invalidado quando o carro finalmente carrega
Quando as 4 imagens de um carro carregam com sucesso — seja no carregamento inicial ou num retry da fila —, o cliente HTML5 SHALL invalidar (remover) todas as entradas de `mapaRotacionar` cuja chave pertença àquele `carro.id` base (`piloto.carro.id`, ignorando eventual livery), forçando `vdp_rotacionar` a recomputar o sprite rotacionado a partir da imagem já carregada na próxima vez em que for necessário.

#### Scenario: Carro carrega com sucesso após já ter sido cacheado quebrado
- **WHEN** `mapaRotacionar` já contém entradas para o `carro.id` de um piloto (potencialmente geradas antes da imagem terminar de carregar) e as 4 imagens desse piloto terminam de carregar com sucesso
- **THEN** todas as entradas de `mapaRotacionar` cuja chave comece com `carro.id + "-"` são removidas

#### Scenario: Próximo frame recomputa o sprite
- **WHEN** o desenho do carro (`vdp_desenhaCarrosCima`) precisa da rotação para um ângulo cuja entrada foi invalidada
- **THEN** `vdp_rotacionar` é chamado novamente a partir do `<img>` já carregado, e o resultado correto é recacheado em `mapaRotacionar`

### Requirement: Intervalo da fila é encerrado junto com o loop principal do jogo
O cliente HTML5 SHALL encerrar (`clearInterval`) o tique da fila de carros não carregados nos mesmos pontos em que o loop principal (`main`) é encerrado: ao sair da corrida (`cpu_sair`) e ao final da corrida (estado `24`).

#### Scenario: Jogador sai da corrida manualmente
- **WHEN** `cpu_sair()` é chamada
- **THEN** tanto o intervalo `main` quanto o intervalo da fila de carros não carregados são encerrados antes da navegação para `index.html`

#### Scenario: Corrida termina normalmente
- **WHEN** `dadosParciais.estado` chega a `24`
- **THEN** tanto o intervalo `main` quanto o intervalo da fila de carros não carregados são encerrados antes da navegação para `resultado.html`

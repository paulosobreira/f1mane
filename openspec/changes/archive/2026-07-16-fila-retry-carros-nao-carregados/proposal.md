## Why

No cliente HTML5, os sprites de um carro (visão de cima, visão de cima sem aerofólio, visão de lado, capacete) podiam falhar ao carregar por instabilidade de rede ou erro transitório do servidor. O retry existente (`mid_carregaImagemComRetry`) desistia de vez após 5 tentativas (~15s) e não avisava mais ninguém do fracasso. Pior: `vdp_rotacionar` desenha a imagem do carro num canvas na hora em que é chamada e cacheia o resultado em `mapaRotacionar` (por `carro.id-ângulo`) sem nunca invalidar essa entrada — se a imagem ainda não tinha carregado quando o preload de `mid_caregaMidia` gerou esse cache (5s após o início do jogo), o carro ficava com o sprite quebrado na tela pelo resto da corrida, mesmo que a imagem chegasse a carregar depois. Era necessário um mecanismo que continuasse tentando indefinidamente, sem sobrecarregar o servidor com tentativas paralelas, e que corrigisse a tela quando o carro finalmente carregasse.

## What Changes

- Nova fila FIFO de "carros não carregados" (`mid_filaCarrosNaoCarregados`), consumida por um tique próprio de 5 segundos (`mid_processaFilaCarrosNaoCarregados`, registrado em `cpu.js` ao lado dos intervalos `main`/`fila` já existentes).
- A cada tique, o primeiro carro da fila tem as 4 imagens (carroCima, carroCimaSemAreofolio, carroLado, capacete) recarregadas como uma unidade só via `mid_carregaImagensCarro`. Se as 4 carregarem com sucesso, o carro sai da fila definitivamente; se qualquer uma falhar (após `mid_carregaImagemComRetry` esgotar suas tentativas), o carro inteiro volta para o **fim** da fila para nova tentativa no próximo tique.
- `mid_carregaImagemComRetry` ganha um callback opcional `aoFinalizar(sucesso)`, chamado uma vez com `true` (onload) ou `false` (tentativas esgotadas) — antes ele desistia silenciosamente sem sinalizar nada.
- Nova `mid_invalidaRotacaoCarro(carroId)`: ao terminar de carregar as 4 imagens de um carro com sucesso, limpa as entradas desse carro em `mapaRotacionar`, forçando `vdp_rotacionar` a recomputar o sprite rotacionado a partir da imagem já carregada no próximo frame, em vez de manter o canvas quebrado cacheado indefinidamente.
- `mid_caregaMidia` (carregamento inicial ao entrar na corrida) passa a usar `mid_carregaImagensCarro` para os 4 sprites de cada piloto, eliminando duplicação da lógica de montagem de URL (temporada/livery) entre o carregamento inicial e o retry da fila.
- Intervalo da fila é limpo (`clearInterval`) junto com o `main` em `cpu_sair()` e ao final da corrida (estado `24`), simetricamente ao que já era feito.

### New Capabilities
- `web-fila-retry-carros-nao-carregados`: fila de retry por carro (FIFO, tique de 5s) para sprites que falharam ao carregar no cliente HTML5, incluindo a invalidação do cache de rotação quando o carro finalmente carrega.

### Modified Capabilities
(nenhuma — não há spec existente cobrindo o carregamento de sprites de carro no cliente web; o comportamento anterior nunca foi documentado como capability.)

## Impact

- `src/main/webapp/html5/js/mid.js`: novas funções `mid_carregaImagensCarro`, `mid_processaFilaCarrosNaoCarregados`, `mid_invalidaRotacaoCarro`, `mid_enfileiraCarroNaoCarregado`; `mid_carregaImagemComRetry` ganha parâmetro `aoFinalizar`; `mid_caregaMidia` refatorado para reusar `mid_carregaImagensCarro`.
- `src/main/webapp/html5/js/cpu.js`: novo `setInterval(mid_processaFilaCarrosNaoCarregados, ...)`; `clearInterval` correspondente em `cpu_sair()` e no tratamento do estado `24`.
- Sem mudanças de API/servidor — o comportamento é inteiramente client-side, reaproveitando os mesmos endpoints REST de sprites já existentes.
- Sem testes automatizados (não há infraestrutura de teste JS no projeto, apenas JUnit para Java); verificado com `node --check` nos dois arquivos.

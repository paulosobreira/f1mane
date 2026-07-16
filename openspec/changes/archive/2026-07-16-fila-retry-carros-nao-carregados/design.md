## Context

O cliente HTML5 carrega os sprites de cada piloto (`mid_caregaMidia`, chamada uma única vez por sessão via a flag `carregouMidia`) via `<img>.src` apontando para endpoints REST (`/rest/letsRace/carroCima/...`, `carroCimaSemAreofolio`, `carroLado`, `capacete`). O único retry existente (`mid_carregaImagemComRetry`) reagia a `onerror` com até 5 tentativas em ~15s e depois desistia silenciosamente — sem sinalizar sucesso nem fracasso definitivo a mais ninguém.

Separadamente, 5s após `mid_caregaMidia`, um `setTimeout` (`fnRotacionarCarro`) pré-renderiza o sprite de cada carro rotacionado em todos os ângulos possíveis da pista, cacheando o canvas resultante em `mapaRotacionar` (chave `carro.id-anguloGraus`). Essa mesma função de rotação (`vdp_rotacionar`) também é chamada a cada frame em `vdp_desenhaCarrosCima` quando falta uma entrada no cache. Como o cache nunca é invalidado, uma imagem que ainda não tinha carregado no momento da primeira chamada de `vdp_rotacionar` para aquele ângulo fica permanentemente cacheada como um canvas em branco/quebrado — esse é o bug real por trás de "carros não carregados" persistirem mesmo depois de o `<img>` eventualmente carregar.

## Goals / Non-Goals

**Goals:**
- Garantir que um carro cujas imagens falharam continue sendo tentado indefinidamente (não apenas por ~15s), sem sobrecarregar o servidor com tentativas paralelas de todos os carros ao mesmo tempo.
- Tratar as 4 imagens de um piloto como uma unidade: só sai da fila quando todas as 4 carregam.
- Corrigir a causa raiz do sprite ficar quebrado para sempre: invalidar o cache de rotação (`mapaRotacionar`) do carro quando ele finalmente carrega.
- Reaproveitar a lógica de montagem de URL (temporada normal vs. livery) entre o carregamento inicial e o retry, em vez de duplicá-la.

**Non-Goals:**
- Não altera o retry rápido existente (`mid_carregaImagemComRetry`, 5 tentativas/~15s) — a fila de 5s é um mecanismo complementar que assume quando esse retry rápido se esgota.
- Não adiciona timeout para requisições penduradas (uma imagem cujo request nunca dispara `onload`/`onerror` fica "em voo" indefinidamente e não retorna à fila); não foi um cenário relatado e adicionar isso agora seria especular sobre um problema não observado.
- Não cobre `mapaRastroFaisca`/`mapaTravadaRodaFumaca` (caches de efeitos visuais genéricos, não específicos de um carro) nem os sprites globais (`safetycar`, pneus, faróis etc.) — apenas os 4 sprites por piloto.
- Não introduz testes automatizados de JS: o projeto não tem infraestrutura de teste para o client-side (só JUnit para o servidor Java); validação foi feita com `node --check` (sintaxe) e leitura de código.

## Decisions

**Fila FIFO com tique próprio de 5s, um carro por vez, em vez de retry paralelo por imagem com backoff.**
Alternativa considerada: aumentar `MID_MAX_TENTATIVAS_IMG`/`MID_DELAY_RETRY_IMG_MS` do retry existente para tentar por mais tempo. Rejeitada porque cada imagem já retenta com seu próprio backoff independente — com muitos carros falhando ao mesmo tempo (ex.: uma instabilidade momentânea do servidor logo no início da corrida), isso geraria rajadas de requisições simultâneas repetidas. Serializar um carro por tique de 5s é mais barato para o servidor, ao custo de recuperação mais lenta quando há muitas falhas simultâneas (N carros → até N×5s para a primeira nova tentativa do último da fila).

**Unidade de retry é o carro inteiro (4 imagens), não a imagem individual.**
Simplifica o modelo mental (pedido explicitamente: "fila com as 4 imagens juntas") e evita ter 4 filas independentes por piloto. Custo: se só 1 das 4 imagens falhar, as outras 3 (já carregadas) são solicitadas de novo no retry — requisições redundantes, mas de baixo custo (sprites pequenos, com Cache-Control/servidor já tolerando pedidos repetidos).

**Invalidação do cache de rotação por prefixo de chave (`carro.id + "-"`), varrendo todo `mapaRotacionar`.**
`mapaRotacionar` não é indexado por carro, só por chave composta `carro.id-anguloGraus`. Não há estrutura auxiliar (ex.: `Map<carroId, Set<chave>>`) para localizar as entradas de um carro sem varredura. Como a invalidação só acontece quando um carro termina de carregar (evento raro, não por frame), o custo de `forEach` sobre o Map inteiro é aceitável; criar uma estrutura auxiliar só para isso seria complexidade desproporcional ao problema.

**Callback `aoFinalizar(sucesso)` adicionado a `mid_carregaImagemComRetry` em vez de uma função paralela.**
Mantém compatibilidade com a única outra chamada existente (`safetycar`, sem o 4º argumento) e evita duplicar a máquina de estados de retry (tentativas/backoff) numa segunda função só para reportar o resultado.

**Chave de invalidação usa `piloto.carro.id` (id base), não o id de livery.**
O cache em `mapaRotacionar` (tanto no preload de `mid_caregaMidia` quanto no desenho em `vdp_desenhaCarrosCima`) sempre chaveia por `piloto.carro.id`/`pl.carro.id` — o id base, ignorando eventual livery (`idCarroLivery`). A invalidação precisa usar essa mesma chave (`carroIdBase`), não o `carroId` ajustado para livery usado na URL de download, senão a entrada errada seria limpa.

## Risks / Trade-offs

- [Muitos carros falhando ao mesmo tempo] → recuperação serializada (1 a cada 5s) é mais lenta que retry paralelo. Mitigação: aceito como trade-off deliberado para não sobrecarregar o servidor; não há relato de volume alto de falhas simultâneas que justifique paralelismo.
- [Request penduradona (sem onload/onerror)] → carro fica "em voo" e não retorna à fila, ficando quebrado até a página recarregar. Mitigação: não implementado agora (non-goal); navegadores já aplicam seus próprios timeouts de rede na prática.
- [Livery com mesmo `carro.id` base que outro piloto sem livery] → `mapaRotacionar` já compartilha a mesma chave entre pilotos com o mesmo `carro.id` base independentemente de livery (comportamento pré-existente, não introduzido por esta mudança) — a invalidação replica essa mesma granularidade, não introduz nem corrige essa sobreposição.

## Migration Plan

Mudança client-side, sem estado persistente ou schema. Deploy é a publicação normal dos arquivos estáticos (`mid.js`, `cpu.js`); não há passo de migração ou rollback especial além de reverter os dois arquivos.

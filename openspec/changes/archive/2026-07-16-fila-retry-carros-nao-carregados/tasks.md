## 1. Retry por imagem com sinalização de resultado

- [x] 1.1 Adicionar parâmetro opcional `aoFinalizar(sucesso)` a `mid_carregaImagemComRetry`, chamado no `onload` (true) e ao esgotar `MID_MAX_TENTATIVAS_IMG` tentativas (false)

## 2. Carregamento por carro como unidade

- [x] 2.1 Extrair `mid_carregaImagensCarro(piloto)` reunindo a montagem de URL (temporada normal vs. livery) e o carregamento das 4 imagens (carroCima, carroCimaSemAreofolio, carroLado, capacete)
- [x] 2.2 Contabilizar as 4 conclusões e, se qualquer uma falhar definitivamente, enfileirar o piloto; se todas tiverem sucesso, invalidar o cache de rotação do carro
- [x] 2.3 Atualizar `mid_caregaMidia` para usar `mid_carregaImagensCarro` no carregamento inicial, removendo a lógica duplicada de montagem de URL

## 3. Fila de retry (FIFO) e tique de 5s

- [x] 3.1 Adicionar `mid_filaCarrosNaoCarregados` (array) e `mid_filaCarrosSet` (dedup) com `mid_enfileiraCarroNaoCarregado(pilotoId)`
- [x] 3.2 Implementar `mid_processaFilaCarrosNaoCarregados`: retira o primeiro piloto da fila e chama `mid_carregaImagensCarro` novamente
- [x] 3.3 Registrar `setInterval(mid_processaFilaCarrosNaoCarregados, MID_INTERVALO_FILA_CARROS_MS)` em `cpu.js`, ao lado dos intervalos `main`/`fila` já existentes

## 4. Invalidação do cache de rotação

- [x] 4.1 Implementar `mid_invalidaRotacaoCarro(carroId)`, removendo de `mapaRotacionar` todas as chaves com prefixo `carroId + "-"`
- [x] 4.2 Garantir que a invalidação usa o `carro.id` base (`piloto.carro.id`), não o id ajustado por livery usado na URL de download

## 5. Encerramento simétrico do intervalo

- [x] 5.1 Adicionar `clearInterval(filaCarrosNaoCarregados)` em `cpu_sair()`
- [x] 5.2 Adicionar `clearInterval(filaCarrosNaoCarregados)` no tratamento do estado `24` em `cpu_dadosParciais`

## 6. Verificação

- [x] 6.1 Validar sintaxe de `mid.js` e `cpu.js` com `node --check` (sem infraestrutura de teste JS no projeto)

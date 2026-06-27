## 1. Constantes em Global

- [x] 1.1 Adicionar em `br.nnpe.Global`: `SMOOTH_MAX_DIFF = 200`, `SMOOTH_CATCHUP_DIFF = 100`, `SMOOTH_GANHO_DIVISOR = 10`, `SMOOTH_LATERAL_REDUCAO = 0.5` como campos `public static`

## 2. Refatorar atualizacaoSuave em PainelCircuito

- [x] 2.1 Localizar o bloco de cálculo de `ganhoSuave` em `atualizacaoSuave()` (~linhas 700–715 de `PainelCircuito.java`) e substituir o cálculo por percentual pelo novo: `Math.max(1, diferencaSuavelReal / Global.SMOOTH_GANHO_DIVISOR)` com teto em `diferencaSuavelReal`
- [x] 2.2 Adicionar bloco de catch-up: quando `diferencaSuavelReal > Global.SMOOTH_CATCHUP_DIFF`, multiplicar `ganhoSuave` por 2 (antes de aplicar o teto)
- [x] 2.3 Substituir o limiar de snap de 1000 (e 2000 para traçados 4-5) por `Global.SMOOTH_MAX_DIFF` em todos os pontos do método `atualizacaoSuave()`
- [x] 2.4 Quando snap ocorrer e `piloto.getIndiceTracado() > 0`, zerar `indiceTracado` via `piloto.setIndiceTracado(0)` (verificar se setter existe ou usar alternativa equivalente)

## 3. Prioridade lateral durante transição de traçado

- [x] 3.1 Após o cálculo final de `ganhoSuave`, adicionar bloco: se `piloto.getIndiceTracado() > 0`, aplicar `ganhoSuave = Math.max(1, (int)(ganhoSuave * Global.SMOOTH_LATERAL_REDUCAO))`

## 4. Compilar e validar visualmente

- [x] 4.1 Compilar: `mvn clean package -Ph2 -DskipTests`
- [ ] 4.2 Rodar `java -jar target/flmane.jar` e observar visualmente: carros devem se mover de forma fluida sem travar, transições de traçado devem ser suaves e sem sobreposição visual
- [ ] 4.3 Observar especificamente: carros próximos durante ultrapassagem — a transição lateral deve concluir antes do carro visual alcançar o carro da frente no novo traçado
- [ ] 4.4 Se travamento ou snap excessivo observado, ajustar `SMOOTH_GANHO_DIVISOR` ou `SMOOTH_CATCHUP_DIFF` em `Global` e repetir

## 5. Validar com simulação headless

- [ ] 5.1 Rodar `java -cp target/flmane.jar br.f1mane.MainFrameSimulacao 2024 Catalunya 72` e verificar que não houve regressão na detecção de colisão física (logs `[COLISAO_EVENTO]` presentes e sem sobreposições reais)
- [ ] 5.2 Rodar `bash simulacao_batch.sh` e confirmar STATUS OK em todas as simulações

## 6. Ajuste fino e documentação

- [ ] 6.1 Registrar os valores finais de `SMOOTH_MAX_DIFF`, `SMOOTH_CATCHUP_DIFF`, `SMOOTH_GANHO_DIVISOR` e `SMOOTH_LATERAL_REDUCAO` que ficaram após validação (podem diferir dos valores iniciais propostos)
- [ ] 6.2 Verificar que `JogoCliente` (multiplayer) continua usando `MOD_GANHO_SUAVE_MULTIPLAYER` sem impacto da nova lógica

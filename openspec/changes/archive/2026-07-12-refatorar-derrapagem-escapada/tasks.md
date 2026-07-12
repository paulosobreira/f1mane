## 1. Derrapagem (traçado 0 → 1/2), desacoplada de stress/modo

- [x] 1.1 Em `Piloto.java`, extrair um novo método privado (ex.: `processaDerrapagem()`) chamado a partir de `processaEscapadaDaPista()` (fora do `if (stress > 90 && AGRESSIVO)`, mas ainda depois das guardas de safety car/qualify/box já existentes no topo do método).
- [x] 1.2 Implementar a condição: traçado 0, `getNoAtual().verificaCurvaBaixa() || getNoAtual().verificaCurvaAlta()`, `carro.getPorcentagemDesgastePneus() < 30`, `!testeHabilidadePilotoFreios()`.
- [x] 1.3 Ao disparar: manter a lógica de escolha de lado já existente (`getTracadoAntigo()` alterna 1↔2; sem anterior, sorteia `controleJogo.getRandom().intervalo(1, 2)`) e `controleJogo.travouRodas(this)`; usar `mudarTracado` NÃO forçado (sem `forcaMudar=true`), preservando o comportamento de esperar a animação em andamento (`indiceTracado == 0`) via guarda genérica de `mudarTracado`.
- [x] 1.4 Remover o bloco antigo de `processaEscapadaDaPista()` que envolvia a derrapagem no `if (stress > 90 && AGRESSIVO)` (o `else if` de curva baixa/alta + `getTracado() == 0` + pneus < 30%), já movido para o novo método.

## 2. Escapada ancorada: dois testes sequenciais e janela de 150 índices

- [x] 2.1 Em `Piloto.java`, remover `emRiscoDeEscapada()`.
- [x] 2.2 Reescrever `processaEscapadaAncoradaAoTracado()`: mudar `if (distancia > 100) return;` para `if (distancia > 150) return;` (novo `JANELA_DETECCAO_ENTRADA_ESCAPADA`); remover o early-return de topo `if (LENTO.equals(modoPilotagem)) return;` (os dois testes abaixo já incorporam essa exigência em suas próprias pré-condições).
- [x] 2.3 Ler `Boolean resultado = resultadoTesteEscapadaPorZonaNestaVolta.get(zona)` **antes** de rodar qualquer teste. O guard `if (resultado == null) { ...roda os testes... }` é o único ponto que decide "já testei essa zona nesta volta", indiferente ao valor gravado.
- [x] 2.4 Dentro do `if (resultado == null)`: implementado `testeEscapadaAgressividadeEStress()` (pré-condição AGRESSIVO+stress; jogador humano manual marcado direto sem RNG; sucesso vira LENTO).
- [x] 2.5 Implementado `testeEscapadaPneus()` (mesmo formato, pré-condição pneus<30%+!LENTO), só chamado se o teste 1 não marcou. Resultado final gravado em `resultadoTesteEscapadaPorZonaNestaVolta.put(zona, marcado)`.
- [x] 2.6 `FORCAR_ESCAPADA_TESTE` continua bypassando os dois testes (nem populam o cache), com a nova janela de 150.
- [x] 2.7 Pós `if (resultado == null)`: `Boolean.FALSE.equals(resultado)` → `return`; senão segue para a checagem de `distancia <= 0`, sem depender de `modoPilotagem`.

## 3. Trava de troca de traçado para piloto marcado

- [x] 3.1 Adicionado campo `impedidoDeMudarTracadoPorEscapada` em `Piloto`, setado para `true` dentro do `if (resultado == null)` de `processaEscapadaAncoradaAoTracado()` quando `marcado == true`.
- [x] 3.2 Guarda adicionada no topo de `mudarTracado(int, boolean, boolean)` (antes até de `isRecebeuBanderada()`): retorna `false` sem efeito colateral se o campo estiver ativo.
- [x] 3.3 `processaEscapadaAncoradaAoTracado()` limpa o campo imediatamente antes de chamar `mudarTracado(laneDeFugaDoTracadoOrigem(tracadoAtual), true)`. Rede de segurança adicional: `processaEscapadaDaPista()` também limpa o campo sempre que `noTracadoDeFuga` (traçado já é 4/5), cobrindo qualquer via alternativa de entrada.
- [x] 3.4 Confirmado por leitura: a guarda fica dentro do próprio `mudarTracado`, então cobre qualquer origem de chamada sem exceção — validação por teste fica na seção 5.

## 4. Remoção do gatilho geométrico antigo (código morto)

- [x] 4.1 Removido `Piloto.escapaTracado()` e `Piloto.processaPontoEscape()`, a chamada a `processaPontoEscape()` no ciclo principal, e o bloco `if (stress>90 && AGRESSIVO) { if (escapaTracado()) {...} }` (já esvaziado do fallback na seção 1).
- [x] 4.2 Removidos os campos `pontoEscape`, `distanciaEscape`, `indexRefEscape` de `Piloto.java`, e os getters `getPontoDerrapada()`, `getDistanciaDerrapada()`, `getIndexRefEscape()`.
- [x] 4.3 Removido o ramo morto em `Piloto.processaMudarTracado()` (`pontoEscape != null && ... && distanciaEscape < Carro.RAIO_DERRAPAGEM`).
- [x] 4.4 Em `Piloto.modoIADefesaAtaque()`, removida a variável `derrapa` e o `if` associado; `valorLimiteStressePararErrarCurva = 100` mantido incondicional (comportamento equivalente ao atual).
- [x] 4.5 Removido `PontoEscape.java`.
- [x] 4.6 Em `Circuito.java`, removidos o campo `escapeMap` e `getEscapeMap()`/`setEscapeMap()`; `gerarEscapeMap()` renomeado para `gerarTracadosDeFuga()`, só populando `pista4Full`/`pista5Full`.
- [x] 4.7 Confirmado único implementador real (`ControleRecursos`, herdado por `ControleJogoLocal`/`JogoServidor`/`JogoCliente`); removido `obterLadoEscape(Point)` de `InterfaceJogo.java` e `ControleRecursos.java`.
- [x] 4.8 Removido `Carro.RAIO_DERRAPAGEM`.
- [x] 4.9 Removidos os overlays de debug em `MainPanelEditor.java` (loop de `escapeMap` no paint) e `PainelCircuito.java` (loop de `escapeMap` em `desenhaDebugIinfo`, label `getIndexRefEscape()`, e o ramo morto de `desenhaFumacaTravaRodaCarroCima` baseado em `getPontoDerrapada()`/`obterLadoEscape`/`RAIO_DERRAPAGEM` — sempre inalcançável, simplificado pra sempre cair no fallback `desenhaFumacaTravarRodasRandom`, comportamento observável idêntico).
- [x] 4.10 Removido o `JMenuItem`/`ActionListener` de debug `escapaTracado` em `MainFrame.java`.
- [x] 4.11 `mvn compile` limpo; varredura por `PontoEscape`/`escapeMap`/`obterLadoEscape`/`RAIO_DERRAPAGEM`/`pontoEscape`/`distanciaEscape`/`indexRefEscape` em `src/main/java` não retornou nada (2 comentários javadoc históricos que citavam `escapaTracado()` também atualizados).

## 5. Testes

- [x] 5.1 `PilotoEscapadaAncoradaTracadoTest.java` reescrito (39 testes) para o novo modelo: teste 1/2 isolados (com/sem RNG conforme pré-condição), teste 1 bloqueando teste 2, LENTO nunca marcado, cache "uma vez por volta" cobrindo tanto marcado quanto não-marcado (cenário central: piloto que passa não é retestado mais perto da entrada mesmo continuando em risco), mudar pra LENTO depois de marcado não salva mais, trava de traçado (rejeita mudança enquanto marcado, libera ao cumprir), janela 150 (limite exato e +1), `FORCAR_ESCAPADA_TESTE`, jogador humano manual, retorno da escapada e redução de velocidade/modo (inalterados).
- [x] 5.2 Criado `PilotoDerrapagemTest.java` (14 testes): curva baixa/alta, pneus>=30% não derrapa, sucesso no teste de freios evita, independência de stress/modo, escolha de lado com/sem traçado anterior, guardas de safety car/qualify/box, animação em andamento adia.
- [x] 5.3 `PilotoDesconcentracaoConvertidaTest.java`: removidos os dois testes que simulavam `escapaTracado()` via reflection (e o helper `setCampo` associado); mantidos `desviaPilotoNaFrente`/`mensagemRetardatario`.
- [x] 5.4 `CircuitoMetadadosArquivoTest.java`: removida a asserção sobre `getEscapeMap().size()`.
- [x] 5.5 `CircuitoEscapadaTracadoTest.java`: javadoc atualizado (`gerarEscapeMap()` → `gerarTracadosDeFuga()`, referência a `escapaTracado()` removida).
- [x] 5.6 `mvn test`: 597 testes, 0 falhas, 0 erros, 2 skipped (pré-existentes, não relacionados). BUILD SUCCESS.

## 6. Verificação final

- [x] 6.1 `mvn test` (597 testes, 0 falhas) e `mvn clean package -Ph2 -DskipTests` — build empacota sem erros.
- [x] 6.2 Varredura repo-wide: `src/main` e `src/test` sem nenhuma referência a `pontoEscape`/`PontoEscape`/`escapaTracado`/`escapeMap`/`obterLadoEscape`/`RAIO_DERRAPAGEM`/`indexRefEscape`/`distanciaEscape`/`gerarEscapeMap` (2 javadocs remanescentes em `ObjetoEscapada.java`/`GeoUtil.java` corrigidos pra citar `gerarTracadosDeFuga()`). `CircuitoMetadadosArquivoTest.java` mantém 2 asserções `assertFalse(xml.contains("property=\"escapeMap\""))` — inofensivas (agora vacuamente verdadeiras, já que a propriedade nem existe mais), não é referência à API removida.
- [x] 6.3 Specs sincronizadas via `/opsx:archive` (`openspec-sync-specs`): `escapada-ia-corrida`, `tracado-safe-lane-change` e `dead-code-removal` atualizadas, `derrapagem-piloto` criada como capability nova — refletem o comportamento final implementado (tuning sem AGRESSIVO/LENTO especiais, sem recompensa de LENTO).

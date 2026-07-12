## Context

`Piloto.processaEscapadaDaPista()` hoje concentra duas mecânicas distintas dentro de um único `if (stress > 90 && AGRESSIVO)`:

1. Um gatilho geométrico antigo (`escapaTracado()`, baseado em `pontoEscape`/`distanciaEscape`/`indexRefEscape`, preenchidos por `processaPontoEscape()` a partir de `Circuito.getEscapeMap()`). Este gatilho está **permanentemente inerte em produção**: `Circuito.gerarEscapeMap()` mantém `escapeMap` sempre vazio (decisão documentada em `openspec/changes/archive/2026-07-11-escapada-ia-corrida-box-uniforme/design.md`), então `escapaTracado()` nunca retorna `true` fora de testes que populam os campos via reflection.
2. Um fallback (`else if`), hoje só alcançável por causa do item 1 estar sempre morto: piloto em curva baixa, no traçado 0, com pneus abaixo de 30%, é empurrado (sem teste de habilidade) para o traçado 1 ou 2. Um pedido anterior já estendeu esse fallback para também cobrir curva alta.

Separadamente, `Piloto.processaEscapadaAncoradaAoTracado()` (mecânica mais nova, traçado 1/2 → 4/5 via `ObjetoEscapada`) usa um modelo de "piloto em risco" (`emRiscoDeEscapada()`) + um único teste de habilidade por zona por volta. Esse modelo tem duas lacunas de comportamento:
- Um piloto já marcado para escapar (teste falhou) escapa da marca simplesmente mudando `modoPilotagem` para `LENTO` antes de alcançar `indiceEntrada` — o guard `if (LENTO.equals(modoPilotagem)) return;` no topo do método é reavaliado a cada ciclo e bloqueia inclusive a execução da escapada já decidida.
- Nada impede o piloto marcado de mudar de traçado (voltar ao 0, por exemplo) antes de alcançar a entrada, evitando a escapada por uma via não relacionada a ela.

Esta mudança separa as duas mecânicas em conceitos independentes — **derrapagem** (traçado 0 → 1/2, ligada a pneus/freios) e **escapada** (traçado 1/2 → 4/5, ligada a stress/agressividade/pneus) — corrige as duas lacunas acima, e remove toda a plumbing do gatilho geométrico morto.

## Goals / Non-Goals

**Goals:**
- Derrapagem (0 → 1/2) independente de `stress`/`modoPilotagem`, baseada só em pneus + teste de freios + tipo de curva.
- Escapada ancorada (1/2 → 4/5) com dois testes sequenciais e independentes por causa (stress+agressividade, depois pneus), um único por zona por piloto por volta.
- Uma vez marcado para escapar, a escapada é cumprida independentemente de mudanças posteriores de `modoPilotagem`, e o piloto não pode trocar de traçado até cumpri-la.
- Remoção completa do gatilho geométrico antigo e de toda a plumbing morta associada (`PontoEscape`, `escapeMap`, `obterLadoEscape`, `RAIO_DERRAPAGEM`, overlays de debug, ramo morto em `processaMudarTracado()`/`modoIADefesaAtaque()`).

**Non-Goals:**
- Não muda o modelo de retorno da escapada (`processaSaidaDaEscapada()`, traçado 4/5 → 1/2) nem a redução de velocidade/giro/modo enquanto no traçado de fuga — isso já está coberto pela spec `escapada-ia-corrida` existente e não foi mencionado no pedido.
- Não introduz nenhuma UI nova; o botão de debug `escapaTracado` em `MainFrame` é removido, não substituído.

**Atualização de tuning**: `Global.FORCAR_ESCAPADA_TESTE` foi removida por completo (campo, uso em `processaEscapadaAncoradaAoTracado()`, e os 4 testes que a exercitavam) — o usuário não precisa mais da flag de validação manual depois de confirmar a mecânica em corrida real. Todas as referências a ela abaixo (D2, Riscos) refletem a versão anterior a essa remoção e ficaram desatualizadas — mantidas só como histórico da decisão original.

## Decisions

### D1: Derrapagem vira seu próprio método, sem stress/AGRESSIVO
`processaEscapadaDaPista()` deixa de ter o `if (stress > 90 && AGRESSIVO)` envolvendo a derrapagem. Novo método privado `processaDerrapagem()`, com suas próprias guardas (safety car, qualify, box — repetidas, não compartilhadas via aninhamento) e chamado como irmã de `processaEscapadaDaPista()` em `processaNovoIndex()` (não mais aninhada dentro dela — decisão pós-tuning do usuário: as duas ficam lado a lado, depois de `processaStress()`, pra ler o stress já atualizado deste ciclo):

```
piloto no traçado 0
&& (noAtual.verificaCurvaBaixa() || noAtual.verificaCurvaAlta())
&& carro.getPorcentagemDesgastePneus() < 30
&& !testeHabilidadePilotoFreios()
```

Ao disparar: mesma lógica de escolha de lado de hoje (`getTracadoAntigo()` alterna 1↔2; sem traçado anterior, sorteia `intervalo(1,2)`), `controleJogo.travouRodas(this)`, sem `incStress` nem mensagem de log (mantém o único efeito colateral que o fallback atual já tem hoje).

**Alternativas consideradas**: manter dentro de `processaEscapadaDaPista()` sob outro nome de bloco — descartado porque o pedido explícito foi desacoplar completamente de stress/agressividade, e um método próprio deixa a intenção clara e testável isoladamente.

### D2: Escapada ancorada usa dois testes sequenciais, sem gate de "risco" separado, com UM ÚNICO PAR de testes por zona por volta (marcado ou não)
`emRiscoDeEscapada()` é removida. O ponto crítico, herdado sem alteração do modelo atual (que já usa `resultadoTesteEscapadaPorZonaNestaVolta` exatamente com esse propósito) e que este design deixa explícito para não ser perdido na implementação: **o par de testes (A, depois B se A não marcou) só roda UMA VEZ por `ObjetoEscapada` por piloto por volta — independentemente do resultado.** Se o piloto passar nos dois (não marcado), ele NÃO é testado de novo nesta volta para essa zona, mesmo que continue satisfazendo as pré-condições de risco em ciclos seguintes, mais perto da entrada. Isso é o que já acontece hoje (requisito pré-existente "um único teste de habilidade por zona por volta") — este change só troca o teste único por um par sequencial, sem tocar na regra de "uma vez por volta".

`processaEscapadaAncoradaAoTracado()` passa a ter, no lugar do teste único de hoje (pseudocódigo já refletindo os ajustes de tuning de D2-TUNING abaixo — não a versão inicial):

```
Boolean resultado = resultadoTesteEscapadaPorZonaNestaVolta.get(zona);
if (resultado == null) {
    // Zona ainda não avaliada nesta volta — único ponto em que os testes rodam.
    boolean marcado = testeEscapadaStress();   // teste A: stress > 90 && !testeHabilidadePiloto()
    if (!marcado) {
        marcado = testeEscapadaPneus();        // teste B, só roda se A não marcou:
                                                // pneus < 30% && stress >= 70 && !testeHabilidadePilotoFreios()
    }
    resultadoTesteEscapadaPorZonaNestaVolta.put(zona, marcado);
    resultado = marcado;
}
// A partir daqui, resultado != null SEMPRE (computado agora ou lido do cache de um
// ciclo anterior) — nenhum teste roda de novo neste ou em nenhum ciclo seguinte
// desta mesma volta, seja resultado true (marcado) ou false (passou).
if (Boolean.FALSE.equals(resultado)) {
    return; // passou nos dois testes: livre dessa zona pelo resto da volta, sem reteste
}
if (distancia <= 0 && getTracado() == tracadoAtual) {
    mudarTracado(laneDeFugaDoTracadoOrigem(tracadoAtual), true);
}
```

O `if (resultado == null)` é o único guard que decide "já testei ou não" — ele é indiferente ao valor computado (`true` ou `false`), por isso um piloto que passou nos dois testes ao entrar na janela (ex.: a 150 índices) não volta a ser testado a 100, 50 ou 10 índices da entrada, mesmo continuando `AGRESSIVO`+estressado ou com pneus baixos o tempo todo. Só a mudança de volta (`garanteCacheDeTesteEscapadaDaVoltaAtual()`, já existente, limpando o mapa) libera um novo teste para essa mesma zona.

`FORCAR_ESCAPADA_TESTE` continua bypassando os dois testes (comportamento inalterado). Jogador humano em modo manual **passa a receber o mesmo teste automático que a IA** (mudança de tuning — ver D2-TUNING item 5; a versão inicial deste design tinha uma exceção que marcava o humano manual direto, sem chance de se salvar).

Curto-circuito do `&&` evita chamar `testeHabilidadePiloto()`/`testeHabilidadePilotoFreios()` (que consomem RNG) quando a pré-condição de cada teste ainda não se aplica — preserva a determinicidade dos testes (RNG só consumido quando a pré-condição já é verdadeira) e o comportamento de `PilotoEscapadaAncoradaTracadoTest` de "teste nunca é chamado" quando a condição de entrada não é satisfeita.

**Por que dois testes e não um `||`/`&&` combinado?** O pedido explicitamente descreve dois testes em sequência com fontes de risco diferentes (agressividade vs. pneus) — cada um representa uma causa distinta de "escapar", e um teste separado por causa preserva a granularidade dos cenários de teste já existentes (ex.: `pneusBaixos_alcancaAEntradaAindaNoTracadoOrigem1_forcaEscapadaNoTracado5`).

**Recompensa por "quase escapar" — REMOVIDA no tuning (ver D2-TUNING abaixo).** A versão inicial deste design preservava `setModoPilotagem(LENTO)` no sucesso de cada teste; o usuário removeu esse efeito durante a sessão de tuning pós-implementação. Sucesso agora só evita a marca, sem nenhum efeito colateral.

### D2-TUNING: Ajustes de `testeEscapadaStress()`/`testeEscapadaPneus()` feitos na sessão de tuning (pós-implementação inicial)

Depois da primeira implementação de D2, o usuário testou em corrida e pediu os seguintes ajustes, todos já aplicados no código (`Piloto.testeEscapadaStress()`/`testeEscapadaPneus()`, `Global.LIMITE_ESTRESSE_PARA_ESCAPADA_PNEUS`):

1. **Teste A (stress) não exige mais `AGRESSIVO`** — pré-condição virou só `getStress() > Global.LIMITE_ESTRESSE_PARA_ESCAPADA_ANCORADA`. Consequência: como `LENTO` e `AGRESSIVO` eram mutuamente exclusivos, um piloto `LENTO` nunca podia ser marcado pelo teste A antes; agora pode, se o stress estiver acima do limite.
2. **Teste B (pneus) ganhou uma segunda pré-condição obrigatória de stress, não mais opcional**: `carro.getPorcentagemDesgastePneus() < 30 && getStress() >= Global.LIMITE_ESTRESSE_PARA_ESCAPADA_PNEUS` (nova constante, 70 — mais baixo que o limite do teste A, 90). As duas condições são exigidas **juntas** (`&&`), não é "pneus baixos OU stress alto" — um piloto só é candidato ao teste B se tiver pneus gastos **e** já estiver com stress elevado (ainda que abaixo do limite do teste A).
3. **Teste B passou a usar `testeHabilidadePilotoFreios()`** (não mais `testeHabilidadePiloto()`) — o teste de habilidade genérico não fazia sentido pra uma causa ligada a pneus/frenagem; o teste específico de freios já existe no codebase (`carro.testeFreios() && testeHabilidadePiloto()`) e é usado em outros contextos de frenagem/derrapagem (ex.: `Piloto.testeHabilidadePilotoFreios()`, já usado por `processaDerrapagem()`).
4. **Exclusão de `LENTO` removida do teste B** — igual ao teste A, um piloto já `LENTO` agora pode ser marcado por essa causa também. "LENTO nunca escapa" não é mais garantido para nenhuma das duas causas.
5. **Exceção de jogador humano em modo manual removida dos dois testes** — `isJogadorHumanoEmModoManual()` foi apagado (ficou sem uso); jogador humano agora passa pelo mesmo `testeHabilidadePiloto()`/`testeHabilidadePilotoFreios()` que a IA, em vez de ser marcado direto sem chance. Isso também torna a referência a "jogador humano continua sem receber teste automático" no parágrafo de `FORCAR_ESCAPADA_TESTE` acima **desatualizada especificamente pra este ponto** — a flag de teste continua bypassando tudo (isso não mudou), mas fora dela não existe mais tratamento diferenciado por jogador humano manual.
6. **Recompensa `setModoPilotagem(LENTO)` no sucesso removida dos dois testes** (ver nota acima) — sucesso simplesmente não marca, sem efeito colateral.

Ambos os métodos, no formato atual, colapsam pré-condição + teste de habilidade numa única expressão booleana com curto-circuito (`emRisco = <precondição> && !<testeDeHabilidade>`), em vez do `if` aninhado da versão inicial — comportamento equivalente (RNG só é consumido quando a pré-condição já é verdadeira), só reescrito de forma mais compacta.

### D3: Janela de detecção recalibrada para 150 índices à frente
`if (distancia > 100) return;` vira `if (distancia > 150) return;`, alinhando a janela de detecção/teste com a tolerância de entrada-já-passada (`TOLERANCIA_INDICES_ENTRADA_JA_PASSADA`, já 150) — a janela passa a ser simétrica: de -150 (tolerância de salto) a +150 (detecção). `TOLERANCIA_INDICES_ENTRADA_JA_PASSADA` não muda de valor, só passa a ser numericamente igual ao novo limite de detecção (coincidência conveniente, não uma junção de conceitos: seguem sendo dois parâmetros logicamente distintos, testados por cenários separados).

### D4: Piloto marcado ignora mudança de modo depois do teste
A execução da escapada (`if (distancia <= 0 && getTracado() == tracadoAtual) { mudarTracado(...) }`) deixa de estar atrás do guard `if (LENTO.equals(modoPilotagem)) return;` no topo do método. Esse guard só protege a ENTRADA no teste (impedindo que um piloto já LENTO seja testado — replicado agora pelas próprias condições dos testes A/B, que exigem AGRESSIVO ou `!LENTO` respectivamente). Uma vez que `resultadoTesteEscapadaPorZonaNestaVolta.get(zona) == Boolean.TRUE` (marcado), a execução ao alcançar `distancia <= 0` SHALL acontecer independente de `modoPilotagem` no momento.

Reestruturação do método: o cache (`resultado == null`, ver pseudocódigo de D2) é consultado ANTES de qualquer checagem de modo, e cobre os DOIS desfechos, não só o de marcado — ver D2 para o guard completo. Resumo do fluxo: `resultado == null` → roda os testes A/B e cacheia o que sair (`true` ou `false`); `resultado == false` (cacheado ou recém-computado) → `return`, sem reteste nem execução; `resultado == true` (cacheado ou recém-computado) → segue para a checagem de `distancia <= 0`, independente de `modoPilotagem` nesse momento (é isso que faz D4 valer mesmo se o piloto virar LENTO depois de marcado).

### D5: Bloqueio de troca de traçado para piloto marcado
Novo campo booleano em `Piloto` (ex.: `impedidoDeMudarTracadoPorEscapada`), setado ao marcar (`marcado == true`) e limpo quando `getTracado()` passa a ser 4 ou 5 (a própria execução da escapada limpa a trava, já que o objetivo — "cumprir a escapada" — foi alcançado). `mudarTracado(int, boolean, boolean)` ganha uma guarda no topo: se o campo estiver ativo, `mudarTracado` retorna `false` para qualquer chamada — **exceto** a chamada interna que a própria `processaEscapadaAncoradaAoTracado()` faz para forçar a entrada em 4/5 (para não se autobloquear). A forma mais simples de resolver isso sem acoplar `mudarTracado` a um caller específico: `processaEscapadaAncoradaAoTracado()` limpa o campo imediatamente antes de chamar `mudarTracado(laneDeFugaDoTracadoOrigem(...), true)`.

Isso vale tanto para IA quanto para jogador humano em modo manual (decisão do usuário) — a guarda fica dentro de `mudarTracado`, então cobre qualquer caminho de chamada (IA, API do jogador, etc.), sem exceção por origem.

**Risco**: um piloto marcado que colide ou precisa desviar por safety car ficaria bloqueado de desviar. **Mitigação**: fora de escopo explícito deste pedido; a trava é avaliada só pelas mudanças de traçado normais (mesmo nível de risco que `FORCAR_ESCAPADA_TESTE` já aceitava para o modelo anterior — ver nota equivalente na spec `escapada-ia-corrida`). Se isso se provar um problema em produção, é matéria de um change futuro.

### D6: Remoção completa do gatilho geométrico antigo
Removidos: `Piloto.escapaTracado()`, `Piloto.processaPontoEscape()`, campos `pontoEscape`/`distanciaEscape`/`indexRefEscape`, getters `getPontoDerrapada()`/`getDistanciaDerrapada()`/`getIndexRefEscape()`; `PontoEscape.java`; `Circuito.escapeMap`/`getEscapeMap()`/`setEscapeMap()` (e a criação do `HashMap` vazio em `gerarEscapeMap()` — o método passa a só popular `pista4Full`/`pista5Full`); `ControleRecursos.obterLadoEscape(Point)` e a assinatura em `InterfaceJogo`; `Carro.RAIO_DERRAPAGEM`; os overlays de debug que iteram `escapeMap`/`getPontoDerrapada()` em `MainPanelEditor.java` e `PainelCircuito.java`; o botão de debug `escapaTracado` em `MainFrame.java`; e o ramo morto em `Piloto.processaMudarTracado()` (`pontoEscape != null && ...`) e a variável `derrapa` (sempre falsa) em `modoIADefesaAtaque()` — esta última simplificada para usar sempre `getValorLimiteStressePararErrarCurva()` como já seria o valor efetivo hoje quando não em reta/largada (ver Riscos).

Testes afetados por essas remoções (`PilotoDesconcentracaoConvertidaTest`, `CircuitoMetadadosArquivoTest`, `CircuitoEscapadaTracadoTest`) são listados em `tasks.md`.

## Risks / Trade-offs

- [Risco] Simplificar `modoIADefesaAtaque()` removendo a checagem `derrapa` muda o valor de `valorLimiteStressePararErrarCurva` de `100` (fixo, comportamento atual real) para `getValorLimiteStressePararErrarCurva()` (90) incondicionalmente fora de reta/largada, já que a variável `derrapa` sempre avaliava falsa. → Mitigação: NÃO simplificar o valor-alvo — manter o código resultante equivalente ao comportamento atual (sempre `100`, já que `derrapa` nunca era `true`), só removendo a variável morta e o `if` que nunca disparava. Qualquer mudança de comportamento aqui é escopo de outro change.
- [Risco] Remover `obterLadoEscape` de `InterfaceJogo` é uma mudança de contrato de interface — qualquer implementador não descoberto na varredura (`ControleRecursos`, herdado por `ControleJogoLocal`) quebraria a compilação. → Mitigação: `mvn compile` valida isso imediatamente; tasks.md inclui a verificação de todos os implementadores de `InterfaceJogo` antes da remoção.
- [Risco] A trava de "impedido de mudar de traçado" (D5) pode interagir mal com guardas já existentes em `mudarTracado` (cooldown, colisão, bandeirada, box) se a ordem de checagem não for cuidadosa. → Mitigação: colocar a nova guarda o mais cedo possível no método (antes de qualquer efeito colateral como `setSetaCima`/`setSetaBaixo`), e cobrir com teste dedicado que tenta `mudarTracado` de várias origens (box, colisão, manual) enquanto marcado.
- [Risco] Testes existentes (`PilotoEscapadaAncoradaTracadoTest`) fixam o valor de pneus em 20% e a janela em 100 índices — praticamente toda a suíte precisa ser reescrita, não só estendida. → Mitigação: tasks.md trata a reescrita da suíte como tarefa explícita, não incidental.

## Migration Plan

Mudança comportamental em código de simulação local, sem estado persistido nem API pública afetada (campos removidos não eram consumidos pelo cliente web, confirmado por busca em `src/main/webapp`/`src/main/resources`). Não há passo de migração de dados. Rollback = reverter o commit/PR.

## Open Questions

Nenhuma — decisões de escopo (mapeamento de curva, efeitos colaterais da derrapagem, janela de detecção, abrangência do bloqueio de traçado) já confirmadas pelo usuário antes deste documento.

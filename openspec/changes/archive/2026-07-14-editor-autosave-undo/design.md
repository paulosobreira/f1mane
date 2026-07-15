## Context

`MainPanelEditor.salvarPista()` (linha 3801) já sabe gravar um circuito em dois arquivos (`<nome>_mro.xml` com `objetos`/`objetosCenario`, `<nome>_mro_meta.xml` com o resto — ver capability `circuito-metadados-arquivo`), usando `XMLEncoder` via `salvarCircuitoEmArquivo()` (linha 3845). Esse método hoje só é chamado por um clique no botão "Salvar Pista Atual" ou pelo atalho F8 (`EditorCircuitos.ativarKeysEditor()`, linha 77-83), e faz validações que abrem diálogo (`alertaEscapadaIncompleta`, `alertaDistanciaNaoInformada`) além de um `JOptionPane` de sucesso ao final.

A inclusão de um objeto no circuito acontece em 6 pontos distintos dentro do `MouseAdapter` de `MainPanelEditor` (em torno das linhas 1883, 1908, 1929, 1950, 1996 e 2024), um por tipo de objeto (`ObjetoTransparencia`, `ObjetoLivre`, `ObjetoGuardRails`, `ObjetoArquibancada`, `ObjetoEscapada`, e o caminho genérico de posicionamento por um único clique usado por `ObjetoConstrucao`/`ObjetoPneus`/etc.), mais um sétimo ponto: `copiarObjeto()` (linha 4385), acionado pelo atalho Alt+C, que clona o objeto atualmente selecionado e o adiciona à mesma lista do original — também uma inclusão, e tratada como tal. Todos os 7 pontos já chamam `formularioListaObjetos.listarObjetos()` e `atualizarPainelFiltroTipos()` logo após o `.add(...)`.

O painel de atalhos já existe e já fica ancorado no canto superior direito do canvas: `desenhaControles(Graphics2D)` (linha 3068), chamado no pipeline de desenho (linha 3065).

O dispatcher de teclado global vive em `EditorCircuitos.ativarKeysEditor()` (linha 69), um `KeyListener` bruto (sem `InputMap`/`ActionMap`) que hoje não checa `Ctrl` — só `Alt`, `Shift`, e teclas simples (`Z`/`X` já usadas para ângulo sem modificador).

## Goals / Non-Goals

**Goals:**
- Salvar automaticamente em disco (mesmo par de arquivos de `salvarPista()`) toda vez que um objeto é efetivamente incluído no circuito pelo editor.
- Manter uma cópia de backup do estado dos dois arquivos como estavam *antes* desse salvamento automático.
- Oferecer uma ação de desfazer (Ctrl+Z) que restaura os arquivos principais a partir do backup e recarrega o circuito exibido no editor a partir desse estado restaurado.
- Exibir o atalho Ctrl+Z no painel de atalhos já desenhado no canto superior direito do canvas.

**Non-Goals:**
- Pilha de undo com múltiplos níveis ou redo — é um único nível de backup (desfaz só a última inclusão salva automaticamente), como pedido na proposta.
- Desfazer outras ações do editor (mover objeto, editar nó de pista, apagar objeto, editar campos do circuito) — o gatilho é exclusivamente a inclusão de um objeto.
- Salvamento automático para um circuito ainda sem arquivo associado (`file == null`, circuito novo nunca salvo manualmente) — nesse caso a inclusão do objeto continua funcionando só em memória, sem autosave, até o primeiro "Salvar Pista Atual" manual.

## Decisions

### Um método central de autosave, chamado nos 7 pontos de inclusão
Em vez de duplicar lógica de backup+gravação em cada um dos 7 locais, um novo método privado (ex.: `autoSalvarComBackup()`) encapsula: checar `file != null`, fazer backup dos dois arquivos atuais, então gravar o novo estado (reaproveitando `vetorizarCircuito()` + `salvarCircuitoEmArquivo()`, sem os diálogos/validações de `salvarPista()`). Cada um dos 7 pontos de inclusão (os 6 de criação por clique, mais `copiarObjeto()`/Alt+C) passa a chamar esse método logo após `atualizarPainelFiltroTipos()`.
- **Alternativa rejeitada**: chamar `salvarPista()` diretamente — descartada porque `salvarPista()` bloqueia com `alertaEscapadaIncompleta`/`alertaDistanciaNaoInformada` e abre um `JOptionPane` de sucesso a cada clique, o que interromperia o fluxo de criação de objetos.

### Backup por cópia de arquivo, não por serialização em memória
Antes de sobrescrever `<nome>_mro.xml`/`<nome>_mro_meta.xml`, o conteúdo atual em disco é copiado (`Files.copy(..., REPLACE_EXISTING)`) para `<nome>_mro.xml.bak`/`<nome>_mro_meta.xml.bak`, no mesmo diretório. Só depois disso o novo estado é gravado por cima dos arquivos principais.
- **Alternativa rejeitada**: manter o `Circuito` anterior serializado em memória (ex.: um campo `circuitoAntesDoUltimoAutoSave`) — descartada porque não sobrevive a um fechamento/reabertura do editor, e o pedido do usuário é "manter um de backup" (arquivo), não um snapshot em memória.
- Se os arquivos principais ainda não existirem em disco (primeiro autosave de um circuito recém-criado e já salvo manualmente uma vez, mas antes de qualquer inclusão de objeto), a cópia de backup é pulada silenciosamente — não há "estado anterior" pra preservar.

### Desfazer restaura em disco e recarrega em memória
Ctrl+Z (`editor.desfazerUltimaInclusao()`, chamado a partir de um novo branch `e.isControlDown()` em `EditorCircuitos.ativarKeysEditor()`) faz: (1) copia os arquivos `.bak` de volta para os arquivos principais (`Files.copy(bak, principal, REPLACE_EXISTING)`); (2) recarrega o `Circuito` em memória a partir desses arquivos restaurados; (3) reflete esse `Circuito` na UI.
- **Implementação real** (ajustada durante a apply): `CarregadorRecursos.carregarCircuito(String)` cacheia por nome de arquivo (`bufferCircuitos`) e lê via classpath — reusá-lo depois de restaurar o backup arriscava devolver o `Circuito` antigo em cache, não o recém-restaurado. Por isso `desfazerUltimaInclusao()` decodifica os dois arquivos restaurados diretamente via `XMLDecoder`/`FileInputStream` (novo método `decodificarCircuitoDeArquivos`), e a parte de refletir na UI (`iniciaEditor()`, `atualizaListas()`, `vetorizarCircuito()`, `refletirCircuitoNosCampos()`, etc.) foi extraída de `carregarCircuitoExistente()` para um novo método `aplicarCircuitoCarregadoNaUI()`, reaproveitado pelos dois fluxos. A parte de restauração pura (arquivos + `Circuito` em memória, sem tocar na UI) também foi isolada num método próprio (`restaurarCircuitoDoBackup()`) especificamente para ser testável sem precisar de uma janela real — ver design original de `carregarCircuitoExistente()`, que já teria o mesmo problema de testabilidade hoje.
- O branch `Ctrl` é checado **antes** do `if (e.isAltDown())`/`if (e.isShiftDown())` existente, do mesmo jeito que esses dois já retornam cedo — preserva `Z` sozinho continuando a significar "menos ângulo" (linha 124), já que só entra no branch de `Ctrl+Z` quando o modificador está pressionado.
- Se não existir arquivo de backup (nenhuma inclusão de objeto ainda ocorreu nesta sessão, ou circuito nunca foi salvo manualmente), Ctrl+Z é um no-op silencioso — sem diálogo de erro, só nada acontece (evita interromper o usuário por apertar Ctrl+Z "por via das dúvidas").
- Desfazer não apaga o `.bak` depois de restaurar: apertar Ctrl+Z de novo sem nenhuma nova inclusão de objeto no meio simplesmente restaura o mesmo estado de novo (idempotente), já que não há redo a proteger.

### Autosave não roda as validações de `salvarPista()`
O método de autosave não chama `existeEscapadaIncompleta()` nem verifica `distanciaKmText` — só verifica se `vetorizarCircuito()` teve sucesso antes de gravar (mesma checagem de sanidade que `salvarPista()` já faz antes de escrever). Se `vetorizarCircuito()` falhar, o autosave é abortado silenciosamente (sem gravar, sem backup incompleto) e a inclusão do objeto em memória não é desfeita — só o disco fica sem essa atualização até o próximo autosave bem-sucedido ou um salvamento manual.

### Novo texto do atalho segue o padrão `Lang.msg(...)` existente
Uma chave nova (ex.: `atalhoDesfazer`) é adicionada em cada `mensagens_XX.properties` (pt/en/es/it), com texto "Ctrl+Z Desfazer" (e equivalentes traduzidos), desenhada em `desenhaControles()` junto das linhas de `atalhoInserir`/`atalhoApagar`/etc.

## Risks / Trade-offs

- [Risco] Gravar em disco a cada objeto incluído adiciona I/O síncrono na thread de eventos (EDT) a cada clique que finaliza um objeto → Mitigação: mesmo custo de I/O que o F8 manual já tem hoje (dois arquivos XML pequenos); nenhum objeto grande o suficiente pra isso ser perceptível.
- [Risco] Falha silenciosa de autosave (ex.: `IOException` de disco cheio/permissão) deixa o usuário sem saber que a inclusão não foi persistida → Mitigação: erro é logado via `Logger.logar` (padrão já usado no arquivo); o usuário sempre pode confirmar com um "Salvar Pista Atual" manual, que continua mostrando erro visível via `srcFrame.dialogDeErro`.
- [Risco] Backup de um único nível: duas inclusões seguidas sem desfazer entre elas tornam a primeira irrecuperável por Ctrl+Z → Mitigação: é o comportamento pedido explicitamente na proposta ("manter um de backup"), não uma pilha completa.
- [Risco] Arquivos `.bak` novos em `src/main/resources/circuitos/` aparecem como não rastreados no `git status`, gerando ruído → Mitigação: adicionar padrão `*.xml.bak` ao `.gitignore` do projeto.

## Migration Plan

Sem migração de dados — recurso novo, não altera o formato dos arquivos principais nem circuitos já existentes. Arquivos `.bak` só passam a existir a partir do primeiro autosave de cada circuito editado após esta mudança; nenhum circuito precisa ser retrocarregado ou convertido.

## Open Questions

- Nenhuma pendente — o design segue diretamente o pedido da proposta (autosave por inclusão de objeto, backup de um nível, Ctrl+Z restaura, atalho exibido no painel existente).

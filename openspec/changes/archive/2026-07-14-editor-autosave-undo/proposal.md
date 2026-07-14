## Why

Hoje, adicionar um objeto no editor de circuitos (`MainPanelEditor`) só é persistido em disco quando o usuário clica em "Salvar Pista Atual" ou aperta F8. Um clique errado ao posicionar um objeto, ou uma sequência de objetos adicionados por engano, não tem como ser desfeita — o único jeito é editar manualmente pela lista de objetos, item por item. Salvar automaticamente a cada objeto incluído, mantendo um backup do estado anterior, permite oferecer um "desfazer" (Ctrl+Z) de baixo risco sem introduzir uma pilha de undo completa.

## What Changes

- Ao incluir um objeto no circuito (qualquer um dos pontos de criação: objeto de posicionamento único, `ObjetoTransparencia`, `ObjetoLivre`, `ObjetoGuardRails`, `ObjetoArquibancada`, `ObjetoEscapada`, ou a cópia de um objeto existente via Alt+C), o editor SHALL salvar automaticamente o circuito em disco (mesmo par de arquivos `<nome>_mro.xml`/`<nome>_mro_meta.xml` já usado por `salvarPista()`), sem abrir diálogo de confirmação nem `JFileChooser`.
- Antes de sobrescrever os arquivos do circuito nesse salvamento automático, o conteúdo atual em disco (o estado de antes da inclusão) SHALL ser copiado para um par de arquivos de backup.
- Uma nova ação "Desfazer" (atalho Ctrl+Z) SHALL restaurar o circuito a partir desse backup, tanto em disco (restaurando os arquivos principais a partir do backup) quanto no editor em memória (recarregando o circuito exibido), desfazendo a última inclusão de objeto salva automaticamente.
- O atalho Ctrl+Z SHALL aparecer listado no painel de atalhos já desenhado no canto superior direito do canvas (`desenhaControles`), junto dos demais atalhos existentes (Inserir, Apagar, Sobe/Desce Nível etc.).
- Salvamento automático sem arquivo associado (circuito novo, ainda não salvo manualmente uma primeira vez) SHALL ser ignorado silenciosamente — a inclusão do objeto continua funcionando normalmente em memória, só o salvamento automático em disco não ocorre até o usuário salvar manualmente pela primeira vez.

## Capabilities

### New Capabilities
- `editor-autosave-undo`: salvamento automático do circuito ao incluir objetos no editor, com backup do estado anterior e desfazer (Ctrl+Z) restaurando esse backup, incluindo a exibição do atalho no painel de atalhos do canvas.

### Modified Capabilities
(nenhuma — o formato de arquivos e o comportamento de `salvarPista()` continuam os mesmos; esta mudança adiciona um gatilho automático e uma ação de restauração em cima do que já existe, sem alterar os requisitos de `circuito-metadados-arquivo` ou `dev-editor-tools`)

## Impact

- `src/main/java/br/f1mane/editor/MainPanelEditor.java`: pontos de inclusão de objeto (em torno das linhas 1883, 1908, 1929, 1950, 1996, 2024, mais `copiarObjeto()`/Alt+C) passam a disparar o salvamento automático; novo método de restauração a partir do backup; `desenhaControles()` ganha uma linha de atalho a mais.
- `src/main/java/br/f1mane/editor/EditorCircuitos.java`: `ativarKeysEditor()` ganha um branch para `Ctrl+Z` (atualmente só `Z` sozinho já é usado para ângulo, então o desfazer precisa checar `e.isControlDown()`).
- Arquivos de circuito em `src/main/resources/circuitos/`: novos arquivos de backup (ex.: `<nome>_mro.xml.bak`/`<nome>_mro_meta.xml.bak`) passam a ser criados/sobrescritos ao lado dos arquivos principais durante a edição.
- Mensagens de idioma (`src/main/resources/idiomas/mensagens_*.properties`): nova chave para o texto do atalho de desfazer.
- Testes existentes que dependem de `MainPanelEditorTestDouble` (ex.: `MainPanelEditorEscapadaCliqueTest`, `MainPanelEditorSalvarDistanciaTest`) precisam continuar não disparando diálogos reais; qualquer novo alerta de erro do salvamento automático/backup deve seguir o mesmo padrão (método `protected` sobrescrito no double).

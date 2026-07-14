## 1. Salvamento automático

- [x] 1.1 Em `MainPanelEditor`, extrair de `salvarPista()` a parte de gravação pura em disco (vetorizar + gravar `_mro.xml`/`_mro_meta.xml` via `salvarCircuitoEmArquivo`) para um método reaproveitável pelos dois fluxos (manual e automático), sem os diálogos de validação nem o `JOptionPane` de sucesso.
- [x] 1.2 Implementar `autoSalvarComBackup()`: no-op se `file == null`; senão, copiar os arquivos principais atuais (se existirem) para `<nome>_mro.xml.bak`/`<nome>_mro_meta.xml.bak`, depois gravar o novo estado usando o método do item 1.1; abortar sem gravar (e sem tocar no backup) se `vetorizarCircuito()` retornar falso.
- [x] 1.3 Chamar `autoSalvarComBackup()` nos 6 pontos de inclusão de objeto em `MainPanelEditor` (em torno das linhas 1883, 1908, 1929, 1950, 1996 e 2024 — `ObjetoTransparencia`, `ObjetoLivre`, `ObjetoGuardRails`, `ObjetoArquibancada`, `ObjetoEscapada`, e o posicionamento genérico de um único clique), logo após `atualizarPainelFiltroTipos()`.
- [x] 1.3b Chamar `autoSalvarComBackup()` também em `copiarObjeto()` (atalho Alt+C, linha ~4385) — copiar um objeto existente conta como uma inclusão e também deve disparar o salvamento automático.
- [x] 1.4 Logar (via `Logger.logarExept`, sem diálogo) qualquer exceção capturada durante o autosave, para não interromper o fluxo de criação de objetos.

## 2. Desfazer (Ctrl+Z)

- [x] 2.1 Implementar `desfazerUltimaInclusao()` em `MainPanelEditor`: no-op silencioso se os arquivos de backup não existirem; senão, copiar `.bak` de volta para os arquivos principais e recarregar o circuito. **Nota de implementação**: `CarregadorRecursos.carregarCircuito()` cacheia por nome de arquivo (`bufferCircuitos`) e lê via classpath, não do `File` local — reusá-lo depois de restaurar o backup arriscava devolver o circuito antigo em cache. Em vez disso, `desfazerUltimaInclusao()` decodifica os dois arquivos restaurados diretamente via `XMLDecoder`/`FileInputStream` (novo método `decodificarCircuitoDeArquivos`) e reaproveita só a parte de refletir na UI, extraída de `carregarCircuitoExistente()` para um novo método `aplicarCircuitoCarregadoNaUI()`.
- [x] 2.2 Em `EditorCircuitos.ativarKeysEditor()`, adicionar um branch para `e.isControlDown() && keyCode == KeyEvent.VK_Z` chamando `editor.desfazerUltimaInclusao()`, posicionado antes dos branches existentes de `Alt`/`Shift`, garantindo que `Z` sozinho continue disparando `menosAngulo()` sem o modificador.
- [x] 2.3 Confirmar que o branch novo retorna cedo (como os de `Alt`/`Shift`) para não cair no `else if` de teclas simples do mesmo evento.

## 3. Painel de atalhos no canvas

- [x] 3.1 Adicionar chave de idioma (ex.: `atalhoDesfazer`) em `mensagens_pt.properties`, `mensagens_en.properties`, `mensagens_es.properties` e `mensagens_it.properties`, com o texto do atalho ("Ctrl+Z Desfazer" e equivalentes traduzidos).
- [x] 3.2 Em `MainPanelEditor.desenhaControles()`, adicionar uma linha `g2d.drawString(Lang.msg("atalhoDesfazer"), x, y)` junto das demais linhas de atalho já desenhadas (Inserir/Apagar/Sobe Nível/Desce Nível). Também aumentada a altura do painel de fundo (`fillRoundRect`) de 260 para 280 pra caber a linha extra.

## 4. Configuração de repositório

- [x] 4.1 Adicionar padrão `*.xml.bak` ao `.gitignore` do projeto, para que os arquivos de backup gerados em `src/main/resources/circuitos/` não apareçam como não rastreados no `git status`.

## 5. Testes

- [x] 5.1 **Não foi necessário**: autosave/desfazer não abrem nenhum diálogo (erros só vão para `Logger.logarExept`, sem `JOptionPane`), então não há novo método de alerta `protected` para sobrescrever em `MainPanelEditorTestDouble`.
- [x] 5.2 Teste: incluir um objeto com `file` apontando para um diretório temporário grava `_mro.xml`/`_mro_meta.xml` atualizados, sem abrir diálogo. (`MainPanelEditorAutoSaveUndoTest.incluirObjeto_comFileAssociado_gravaArquivosSemBackupNaPrimeiraVez`)
- [x] 5.2b Teste: copiar um objeto existente via `copiarObjeto()` (Alt+C) também dispara o salvamento automático. (`MainPanelEditorAutoSaveUndoTest.copiarObjeto_altC_tambemDisparaAutoSave`)
- [x] 5.3 Teste: incluir um objeto sem `file` associado (`file == null`) não grava nada em disco, mas o objeto aparece normalmente na lista/circuito em memória. (`MainPanelEditorAutoSaveUndoTest.incluirObjeto_semFileAssociado_naoGravaMasIncluiEmMemoria`)
- [x] 5.4 Teste: duas inclusões em sequência mantêm no backup apenas o estado anterior à última inclusão (não ao dois-antes). (`MainPanelEditorAutoSaveUndoTest.autoSalvarComBackup_chamadoDuasVezes_backupReflecteApenasOEstadoAnteriorAoUltimo`)
- [x] 5.5 Teste: a restauração a partir do backup (`restaurarCircuitoDoBackup()`, parte de `desfazerUltimaInclusao()` sem a UI — ver nota da tarefa 2.1) devolve os arquivos principais e o `circuito` em memória ao estado anterior à última inclusão. (`MainPanelEditorAutoSaveUndoTest.desfazer_restauraArquivosEModeloEmMemoria`)
- [x] 5.6 Teste: `restaurarCircuitoDoBackup()` sem backup existente não lança exceção e não altera nenhum arquivo. (`MainPanelEditorAutoSaveUndoTest.desfazer_semBackupDisponivel_naoLancaExcecaoNemAlteraNada`)
- [ ] 5.7 **Não automatizado.** Testar o dispatch de `KeyEvent` real em `EditorCircuitos.ativarKeysEditor()` exigiria instanciar `EditorCircuitos` — cujo construtor já roda `srcFrame.setVisible(true)` e monta a janela inteira — algo que nenhum teste deste projeto faz hoje (os atalhos pré-existentes Z/X/Delete/Insert/F8 também não têm cobertura automatizada nesse nível). Fica coberto por revisão de código (o branch novo já foi conferido manualmente: `Ctrl+Z` retorna cedo antes do `else if` que trata `Z` sozinho) e pela verificação manual da tarefa 6.2.

## 6. Verificação manual

- [ ] 6.1 **Pendente do usuário** — não pude rodar a UI Swing interativamente neste ambiente (sem display/terminal interativo). Rodar o editor de circuitos, incluir um objeto, confirmar visualmente que `_mro.xml`/`_mro_meta.xml` e os `.bak` correspondentes são atualizados no diretório do circuito.
- [ ] 6.2 **Pendente do usuário** — mesma limitação acima. Pressionar Ctrl+Z após incluir um objeto e confirmar que ele desaparece do canvas/lista e que os arquivos em disco voltam ao estado anterior.
- [ ] 6.3 **Pendente do usuário** — mesma limitação acima. Confirmar visualmente que a linha "Ctrl+Z" aparece no painel de atalhos no canto superior direito do canvas.

## ADDED Requirements

### Requirement: Salvar o circuito é bloqueado se houver ObjetoEscapada incompleta
`MainPanelEditor.salvarPista()` SHALL verificar, antes de gravar o circuito em arquivo, se todo `ObjetoEscapada` presente em `circuito.getObjetos()` tem um ponto de saída ancorado (`getIndiceSaida() != -1`). Quando existir pelo menos uma `ObjetoEscapada` com `indiceSaida == -1`, o salvamento SHALL ser abortado e um alerta (`JOptionPane`, via chave `Lang.msg(...)`) SHALL ser exibido informando que há uma escapada incompleta, seguindo o mesmo padrão de método `protected` overridable de `alertaPontoEscapadaInvalido()` (com override correspondente em `MainPanelEditorTestDouble` para testes).

#### Scenario: Salvar com escapada sem saída definida é bloqueado
- **WHEN** o usuário clica em salvar um circuito que contém uma `ObjetoEscapada` cuja saída nunca foi finalizada (`indiceSaida == -1`)
- **THEN** o circuito não é gravado em arquivo, e um alerta é exibido informando que existe uma escapada incompleta

#### Scenario: Salvar com todas as escapadas completas prossegue normalmente
- **WHEN** o usuário clica em salvar um circuito em que toda `ObjetoEscapada` presente tem `indiceEntrada` e `indiceSaida` ambos definidos (`!= -1`)
- **THEN** a verificação de escapada incompleta não bloqueia o salvamento, e o fluxo de `salvarPista()` prossegue normalmente (demais validações e gravação em arquivo)

#### Scenario: Circuito sem nenhuma ObjetoEscapada não é afetado
- **WHEN** o usuário clica em salvar um circuito que não contém nenhuma `ObjetoEscapada`
- **THEN** a verificação de escapada incompleta não bloqueia o salvamento

## ADDED Requirements

### Requirement: Salvar o circuito é bloqueado se distanciaKm não tiver sido informada
`MainPanelEditor.salvarPista()` SHALL verificar, antes de gravar o circuito em arquivo, se o campo de distância (`distanciaKmText`) foi informado com um valor numérico maior que zero. Quando o campo estiver vazio ou o valor informado for `<= 0`, o salvamento SHALL ser abortado antes de qualquer `Integer.parseInt(...)` sobre esse campo, e um alerta (`JOptionPane`, via chave `Lang.msg(...)`) SHALL ser exibido informando que a distância do circuito não foi informada, seguindo o mesmo padrão de método `protected` overridable de `alertaPontoEscapadaInvalido()` (com override correspondente em `MainPanelEditorTestDouble` para testes).

#### Scenario: Salvar com campo de distância vazio é bloqueado
- **WHEN** o usuário clica em salvar um circuito com o campo de distância em quilômetros vazio
- **THEN** o circuito não é gravado em arquivo, nenhuma `NumberFormatException` é lançada, e um alerta é exibido informando que a distância não foi informada

#### Scenario: Salvar com distância zero é bloqueado
- **WHEN** o usuário clica em salvar um circuito com o campo de distância em quilômetros preenchido com `0`
- **THEN** o circuito não é gravado em arquivo, e um alerta é exibido informando que a distância não foi informada

#### Scenario: Salvar com distância maior que zero prossegue normalmente
- **WHEN** o usuário clica em salvar um circuito com o campo de distância em quilômetros preenchido com um valor maior que zero
- **THEN** a verificação de distância não informada não bloqueia o salvamento, `circuito.setDistanciaKm(...)` é chamado normalmente, e o fluxo de `salvarPista()` prossegue (demais validações e gravação em arquivo)

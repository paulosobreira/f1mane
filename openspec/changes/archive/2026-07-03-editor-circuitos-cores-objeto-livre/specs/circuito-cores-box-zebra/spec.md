## ADDED Requirements

### Requirement: Circuito expõe cores customizáveis de box e zebra
`Circuito` SHALL expor quatro propriedades `Color` — `corBox1`, `corBox2`, `corZebra1`, `corZebra2` — persistidas via `XMLEncoder`/`XMLDecoder` do mesmo jeito que `corFundo`/`corAsfalto` (campo transiente, getter/setter simples), com valor `null` quando não definidas explicitamente.

#### Scenario: Circuito recém-criado nasce sem cores de box/zebra definidas
- **WHEN** um novo `Circuito` é criado pelo editor sem que o usuário defina explicitamente `corBox1`, `corBox2`, `corZebra1` ou `corZebra2`
- **THEN** os quatro getters correspondentes retornam `null`

#### Scenario: XML sem as novas propriedades carrega normalmente
- **WHEN** um arquivo XML de circuito existente é decodificado por `XMLDecoder` e não contém `<void property="corBox1">` (ou as demais três)
- **THEN** o `Circuito` resultante carrega sem erro, com os getters de cor de box/zebra retornando `null`

### Requirement: Editor de circuitos permite definir as cores de box e zebra
O editor de circuitos SHALL exibir quatro indicadores de cor clicáveis para `corBox1`, `corBox2`, `corZebra1` e `corZebra2`, posicionados junto aos indicadores existentes de "Cor de Fundo" e "Cor do Asfalto", cada um abrindo um seletor de cores e gravando a cor escolhida no circuito em edição.

#### Scenario: Definir cor de zebra 1
- **WHEN** o usuário clica no indicador de "Cor Zebra 1" e escolhe uma cor no seletor
- **THEN** `circuito.getCorZebra1()` passa a retornar a cor escolhida e o indicador reflete essa cor

#### Scenario: Definir cores de box
- **WHEN** o usuário clica nos indicadores de "Cor Box 1" e "Cor Box 2" e escolhe cores no seletor para cada um
- **THEN** `circuito.getCorBox1()` e `circuito.getCorBox2()` passam a retornar as cores escolhidas

### Requirement: Cores de zebra customizadas valem só nas curvas, com fallback independente branco/vermelho
O desenho da zebra (`PainelCircuito.desenhaTintaPistaZebra` e `DesenhoProceduralCircuito.desenhaTintaPistaEZebra`) SHALL pintar a tinta de borda da pista fora das curvas sempre com `Color.WHITE`, independentemente das cores customizadas. Nos segmentos de curva (`CURVA_ALTA`/`CURVA_BAIXA`), a faixa de zebra SHALL alternar `corZebra1` (fundo sólido) e `corZebra2` (listras), cada uma caindo no seu próprio fallback quando `null` — `Color.WHITE` para `corZebra1` e `Color.RED` para `corZebra2` — sem exigir que as duas estejam definidas juntas.

#### Scenario: Zebra usa cores customizadas do circuito só nas curvas
- **WHEN** um circuito tem `corZebra1` e `corZebra2` definidas e a pista é desenhada
- **THEN** a faixa de zebra das curvas alterna `corZebra1` e `corZebra2`, e a borda da pista fora das curvas permanece branca

#### Scenario: Zebra mantém branco/vermelho em circuito sem cores definidas
- **WHEN** um circuito não tem `corZebra1`/`corZebra2` definidas (`null`) e a zebra é desenhada em uma curva
- **THEN** os segmentos de zebra são pintados com `Color.WHITE` e `Color.RED`, como antes desta mudança

#### Scenario: Só uma cor definida usa fallback apenas na outra
- **WHEN** um circuito tem apenas `corZebra2` definida (`corZebra1` = `null`) e a zebra é desenhada em uma curva
- **THEN** a faixa de zebra alterna `Color.WHITE` (fallback de `corZebra1`) e a `corZebra2` customizada

### Requirement: Cor de box do circuito é usada quando não há carro associado ao slot
O desenho da área de box SHALL usar `circuito.getCorBox1()`/`getCorBox2()` como cor padrão dos slots de box quando não houver um carro/time associado ao contexto de desenho (preview no editor, geração de imagem em memória); quando essas cores forem `null`, o sistema SHALL manter a aparência atual (cor neutra/cinza-claro) para esse mesmo contexto. Em corrida real, com um carro ocupando o slot, o desenho SHALL continuar priorizando as cores do carro (`carro.getCor1()`/`getCor2()`) exatamente como antes desta mudança.

#### Scenario: Editor usa cores de box do circuito no preview
- **WHEN** o editor de circuitos desenha a área de box de um circuito com `corBox1`/`corBox2` definidas, sem contexto de carro/corrida
- **THEN** os slots de box são desenhados com `corBox1` e `corBox2`

#### Scenario: Corrida real mantém cor do carro no box
- **WHEN** uma corrida está em andamento e um carro ocupa um slot de box, independentemente de `circuito.getCorBox1()`/`getCorBox2()` estarem definidas
- **THEN** o slot de box desse carro continua sendo pintado com `carro.getCor1()`/`getCor2()`, sem alteração de comportamento

#### Scenario: Preview sem cores de box definidas mantém aparência atual
- **WHEN** o editor de circuitos desenha a área de box de um circuito sem `corBox1`/`corBox2` definidas
- **THEN** os slots de box são desenhados exatamente como antes desta mudança

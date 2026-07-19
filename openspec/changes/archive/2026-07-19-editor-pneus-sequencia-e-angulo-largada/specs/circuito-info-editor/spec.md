## ADDED Requirements

### Requirement: Circuito tem uma propriedade opcional anguloLargada
`Circuito` SHALL expor uma propriedade `anguloLargada` (graus, `Double` opcional, valor padrão não definido), com getter/setter, persistida no arquivo de metadados do circuito apenas quando definida. Quando `anguloLargada` está definida, ela SHALL sobrepor o cálculo automático do ângulo da linha de largada (direção local da pista no nó de Largada); quando não está definida, o comportamento SHALL ser idêntico ao existente antes desta mudança (cálculo automático), preservando compatibilidade com circuitos já persistidos.

#### Scenario: Circuito recém-criado não tem anguloLargada definido
- **WHEN** um novo `Circuito` é instanciado sem que `anguloLargada` seja definida explicitamente
- **THEN** `circuito.getAnguloLargada()` retorna `null`, e a linha de largada é desenhada com o ângulo calculado automaticamente

#### Scenario: Definir anguloLargada sobrepõe o cálculo automático
- **WHEN** `circuito.setAnguloLargada(...)` é chamado com um valor não nulo
- **THEN** a linha de largada passa a ser desenhada usando esse ângulo, em vez do calculado a partir da direção local da pista

#### Scenario: Circuito sem override não grava a propriedade no arquivo de metadados
- **WHEN** um circuito sem `anguloLargada` definida é persistido no arquivo de metadados
- **THEN** o arquivo não contém a propriedade `anguloLargada`, preservando compatibilidade com o formato usado por circuitos já existentes

### Requirement: Editor exibe um campo para ajustar o ângulo da linha de largada
O editor de circuitos SHALL exibir um spinner numérico (0 a 360 graus) para editar `circuito.anguloLargada`, posicionado ao lado do campo de largura da pista. O spinner SHALL ser pré-preenchido com o valor de `circuito.getAnguloLargada()` quando já definido, ou com o ângulo calculado automaticamente a partir da direção local da pista quando ainda não houver override. Alterar o valor do spinner SHALL gravar esse valor em `circuito.anguloLargada`.

#### Scenario: Spinner nasce preenchido com o ângulo calculado quando não há override
- **WHEN** o editor carrega um circuito sem `anguloLargada` definida
- **THEN** o spinner de ângulo da largada exibe o valor calculado a partir da direção local da pista no nó de Largada, sem que isso por si só grave um override em `circuito.anguloLargada`

#### Scenario: Spinner nasce preenchido com o override já salvo
- **WHEN** o editor carrega um circuito com `anguloLargada` já definida
- **THEN** o spinner de ângulo da largada exibe esse valor salvo, não o valor calculado automaticamente

#### Scenario: Alterar o spinner grava o override
- **WHEN** o usuário altera o valor do spinner de ângulo da largada
- **THEN** `circuito.getAnguloLargada()` passa a retornar o novo valor, e a linha de largada é redesenhada com esse ângulo

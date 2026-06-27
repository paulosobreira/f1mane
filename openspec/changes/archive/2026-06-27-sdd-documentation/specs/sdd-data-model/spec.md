## ADDED Requirements

### Requirement: Piloto documentado
O SDD SHALL descrever os campos arquiteturalmente relevantes de `Piloto`.

#### Scenario: Campos de Piloto descritos
- **WHEN** o leitor consulta o modelo de dados
- **THEN** o SDD documenta: campos de identidade (`id`, `nome`, `nomeAbreviado`, `habilidade`), campos de corrida (`posicao`, `numeroVolta`, `ptosPista`, `velocidade`, `stress`, `modoPilotagem: AGRESSIVO|NORMAL|LENTO`), campos de estado (`box`, `desqualificado`, `jogadorHumano`, `ativarErs`, `ativarDRS`), e relação com `Carro` e `No noAtual`

### Requirement: Carro documentado
O SDD SHALL descrever constantes e campos de `Carro`.

#### Scenario: Constantes de Carro descritas
- **WHEN** o leitor consulta tipos de pneu e estados de dano
- **THEN** o SDD lista os tipos de pneu (`TIPO_PNEU_MOLE`, `TIPO_PNEU_DURO`, `TIPO_PNEU_CHUVA`), os estados de dano (`PNEU_FURADO`, `PERDEU_AEREOFOLIO`, `BATEU_FORTE`, `PANE_SECA`, `EXPLODIU_MOTOR`), as posições de asa (`MAIS_ASA`, `ASA_NORMAL`, `MENOS_ASA`) e os valores de giro (`GIRO_MIN_VAL=1`, `GIRO_NOR_VAL=5`, `GIRO_MAX_VAL=9`)

#### Scenario: Campos de consumo e desgaste de Carro descritos
- **WHEN** o leitor consulta o estado físico do carro
- **THEN** o SDD descreve: `porcentagemCombustivel`, `porcentagemDesgastePneus`, `porcentagemDesgasteMotor`, `potencia`, `aerodinamica`, `freios`, `cargaErs`, `temperaturaMotor`, `temperaturaPneus`

### Requirement: No documentado
O SDD SHALL descrever a estrutura de `No` e suas constantes de tipo.

#### Scenario: Tipos de No descritos
- **WHEN** o leitor consulta o modelo de nó de pista
- **THEN** o SDD descreve que `No` usa constantes de cor como tipo: `LARGADA=Color.BLUE`, `RETA=Color.GREEN`, `CURVA_ALTA=Color.YELLOW`, `CURVA_BAIXA=Color.RED`, `BOX=Color.CYAN`, `PARADA_BOX=Color.ORANGE`, `FIM_BOX=Color.PINK`; e que cada nó tem `point: Point`, `index: int` e `tracado: int`

### Requirement: Circuito documentado
O SDD SHALL descrever a estrutura de `Circuito` e como ela agrega nós.

#### Scenario: Estrutura de Circuito descrita
- **WHEN** o leitor consulta o modelo de circuito
- **THEN** o SDD descreve que `Circuito` mantém `pista: List<No>` (nós brutos do XML) e `pistaFull: List<No>` (interpolados por `vetorizarPista()`), separados de `box: List<No>` e `boxFull: List<No>` para o pit lane; e campos de configuração: `nome`, `probalidadeChuva`, `velocidadePista`, `noite`, `ladoBox`

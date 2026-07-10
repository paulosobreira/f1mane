## ADDED Requirements

### Requirement: Vão entre os módulos do CAMINHAO é menor que a margem de borda
`ObjetoConstrucao` do tipo `CAMINHAO` SHALL desenhar o vão entre os dois módulos (cabine e carroceria) usando uma constante própria (`VAO_MODULOS_CAMINHAO`) igual à metade da margem usada entre a forma externa e interna de cada módulo (`MARGEM_INTERNA`), sem alterar `MARGEM_INTERNA` em si, que continua regendo a borda aninhada de todos os tipos, incluindo os dois módulos do próprio CAMINHAO.

#### Scenario: Vão entre cabine e carroceria é a metade da margem de borda
- **WHEN** um `ObjetoConstrucao` do tipo `CAMINHAO` é desenhado
- **THEN** a distância entre a borda direita do módulo da cabine e a borda esquerda do módulo da carroceria é igual à metade do valor de `MARGEM_INTERNA`

#### Scenario: Margem de borda dos módulos permanece inalterada
- **WHEN** um `ObjetoConstrucao` do tipo `CAMINHAO` é desenhado
- **THEN** a margem entre a forma externa e a forma interna de cada módulo (cabine e carroceria) continua usando o valor de `MARGEM_INTERNA` sem redução, e o comportamento de `QUADRADO`/`REDONDO`/`BARCO` (que não têm dois módulos) não é afetado

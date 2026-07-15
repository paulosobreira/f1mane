# transparencia-intervalo-no

## Purpose

Comportamento de renderização do `ObjetoTransparencia` (recorte de transparência no sprite do carro, visão de cima): o recorte só se aplica quando o nó atual do carro está dentro de `[inicioTransparencia, fimTransparencia]` (ou sempre, se ambos forem 0), e isso vale de forma consistente para piloto, safety car e preview de teste do editor — nenhum desses três "carros" deve ficar sujeito a um comportamento diferente dos outros.

## Requirements

### Requirement: Recorte de transparência respeita o intervalo de nó e a restrição de box, para qualquer carro desenhado
Quando o sistema desenha o recorte de transparência de um `ObjetoTransparencia` (visão de cima) para qualquer carro — piloto, safety car ou carro de teste do editor — SHALL aplicar a mesma checagem: se `inicioTransparencia != 0` e `fimTransparencia != 0`, o recorte só é desenhado quando o índice do nó atual do carro está dentro de `[inicioTransparencia, fimTransparencia]`; se ambos forem `0`, o recorte se aplica em qualquer ponto do circuito. Além disso, se `transparenciaBox` for `true`, o recorte só é desenhado enquanto o carro estiver nos nós do box; caso contrário (carro fora do box), o recorte é pulado independentemente do intervalo de nó. Essa checagem SHALL ser a única autoridade sobre quando o recorte se aplica — nenhum outro campo do objeto (em particular `nivelDesenho`/`isPintaEmcima`, que só afeta a ordem de desenho no editor) SHALL impedir o recorte de ser aplicado a qualquer um dos três carros.

#### Scenario: Piloto fora do intervalo de nó não sofre o recorte
- **WHEN** o piloto está em um nó cujo índice é menor que `inicioTransparencia` ou maior que `fimTransparencia` de um `ObjetoTransparencia` com ambos os campos diferentes de zero
- **THEN** o sprite do piloto (visão de cima) é desenhado sem o recorte de transparência desse objeto

#### Scenario: Piloto dentro do intervalo de nó sofre o recorte
- **WHEN** o piloto está em um nó cujo índice está dentro de `[inicioTransparencia, fimTransparencia]` de um `ObjetoTransparencia` com ambos os campos diferentes de zero
- **THEN** o sprite do piloto (visão de cima) é desenhado com o recorte de transparência desse objeto

#### Scenario: Safety car fora do intervalo de nó não sofre o recorte
- **WHEN** o safety car está na pista, em um nó cujo índice é menor que `inicioTransparencia` ou maior que `fimTransparencia` de um `ObjetoTransparencia` com ambos os campos diferentes de zero
- **THEN** o sprite do safety car (visão de cima) é desenhado sem o recorte de transparência desse objeto

#### Scenario: Safety car dentro do intervalo de nó sofre o recorte
- **WHEN** o safety car está na pista, em um nó cujo índice está dentro de `[inicioTransparencia, fimTransparencia]` de um `ObjetoTransparencia` com ambos os campos diferentes de zero
- **THEN** o sprite do safety car (visão de cima) é desenhado com o recorte de transparência desse objeto

#### Scenario: Carro de teste do editor fora do intervalo de nó não sofre o recorte
- **WHEN** o usuário está com o teste de pista do editor ativo e o carro de teste está em um nó cujo índice é menor que `inicioTransparencia` ou maior que `fimTransparencia` de um `ObjetoTransparencia` com ambos os campos diferentes de zero
- **THEN** o preview do carro de teste (visão de cima) é desenhado sem o recorte de transparência desse objeto

#### Scenario: Carro de teste do editor dentro do intervalo de nó sofre o recorte
- **WHEN** o usuário está com o teste de pista do editor ativo e o carro de teste está em um nó cujo índice está dentro de `[inicioTransparencia, fimTransparencia]` de um `ObjetoTransparencia` com ambos os campos diferentes de zero
- **THEN** o preview do carro de teste (visão de cima) é desenhado com o recorte de transparência desse objeto

#### Scenario: Objeto sem intervalo configurado continua se aplicando ao circuito inteiro
- **WHEN** um `ObjetoTransparencia` tem `inicioTransparencia == 0` e `fimTransparencia == 0` (valor padrão, ou circuito com XML antigo sem esses campos)
- **THEN** o recorte de transparência desse objeto é desenhado para o piloto, o safety car e o carro de teste do editor em qualquer ponto do circuito, como já acontecia antes desta mudança

#### Scenario: Objeto restrito ao box não sofre recorte fora do box
- **WHEN** um `ObjetoTransparencia` tem `transparenciaBox == true` e o carro (piloto, safety car ou carro de teste) não está nos nós de box
- **THEN** o recorte de transparência desse objeto não é desenhado para esse carro, independentemente do intervalo de nó configurado

#### Scenario: Objeto restrito ao box sofre recorte dentro do box
- **WHEN** um `ObjetoTransparencia` tem `transparenciaBox == true` e o carro (piloto, safety car ou carro de teste) está nos nós de box
- **THEN** o recorte de transparência desse objeto é desenhado para esse carro (sujeito ainda ao intervalo de nó, se configurado)

#### Scenario: Nível de desenho não impede o recorte no safety car
- **WHEN** um `ObjetoTransparencia` tem `nivelDesenho` maior ou igual a 1 (por exemplo, o padrão 100 aplicado a objetos recém-criados no editor, que torna `isPintaEmcima()` verdadeiro) e o safety car está dentro do intervalo de nó configurado (ou o objeto não tem intervalo configurado)
- **THEN** o recorte de transparência desse objeto continua sendo desenhado para o safety car, exatamente como aconteceria para o piloto

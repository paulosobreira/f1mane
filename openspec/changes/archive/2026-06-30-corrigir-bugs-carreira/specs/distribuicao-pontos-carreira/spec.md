## MODIFIED Requirements

### Requirement: Custo de upgrade deve ser simétrico ao refund de downgrade
O sistema SHALL calcular o custo de cada ponto de upgrade com base na faixa do nível-alvo, de forma que o custo de subir N→N+1 seja sempre igual ao refund de descer N+1→N, incluindo a transição 998→999 (custo: 50 pontos).

Tabela de custo por faixa:

| Nível-alvo  | Custo por ponto |
|-------------|-----------------|
| ≤ 599       | 1               |
| 600 – 699   | 2               |
| 700 – 799   | 10              |
| 800 – 899   | 20              |
| 900 – 999   | 50              |

#### Scenario: Upgrade do nível 998 para 999
- **WHEN** jogador incrementa qualquer atributo de 998 para 999
- **THEN** o sistema SHALL debitar exatamente 50 pontos do pool de construtores

#### Scenario: Downgrade do nível 999 para 998
- **WHEN** jogador reduz qualquer atributo de 999 para 998
- **THEN** o sistema SHALL creditar exatamente 50 pontos ao pool de construtores

#### Scenario: Ciclo upgrade/downgrade no nível 999 não gera pontos
- **WHEN** jogador faz downgrade de 999→998 e em seguida upgrade de 998→999
- **THEN** o saldo do pool de construtores SHALL ser igual ao saldo antes do ciclo

#### Scenario: Upgrade abaixo de 999 mantém custo correto
- **WHEN** jogador incrementa um atributo de qualquer nível entre 400 e 997
- **THEN** o custo debitado SHALL corresponder à faixa do nível-alvo conforme tabela acima

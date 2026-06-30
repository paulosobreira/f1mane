## Context

O sistema de carreira multiplayer é acessado exclusivamente via REST (`LetsRace.gravarEquipe` → `ControleJogosServer.gravarEquipe` → `ControleClassificacao.atualizaCarreira`). A distribuição de pontos usa uma função de custo não-linear em `Util.processaValorPontosCarreira` que calcula incremento de custo (`inc`) com base no nível-alvo. O bug está na última faixa: a condição `proximoValor < 999` exclui o próprio nível 999, tornando o upgrade 998→999 gratuito enquanto o downgrade 999→998 devolve 50 pontos.

Tabela de custo atual vs. correto:

| Transição  | Custo atual | Custo correto |
|------------|-------------|---------------|
| 998 → 999  | 0           | 50            |
| 999 → 998  | +50 (refund)| +50 (refund)  |

## Goals / Non-Goals

**Goals:**
- Tornar o custo de upgrade simétrico ao refund de downgrade em todos os níveis, incluindo 999
- Remover instrução duplicada `setIdCarroLivery` em `atualizaCarreira`

**Non-Goals:**
- Rebalancear as faixas de custo de outros níveis
- Alterar a lógica de carreira no cliente Swing (não é o canal suportado)
- Migrar pontos de jogadores que exploraram o bug

## Decisions

**Correção da condição de fronteira**
Alterar `proximoValor < 999` para `proximoValor <= 999` em `Util.processaValorPontosCarreira`.
- Alternativa considerada: adicionar um bloco `else if (proximoValor == 999)` separado — descartado por ser redundante; a condição `<= 999` cobre o caso sem duplicação.

**Não retroativo**
Jogadores que já acumularam pontos via bug não terão saldo corrigido. O custo de detectar e reverter abusos supera o benefício em um jogo casual.

## Risks / Trade-offs

- [Mudança de comportamento para nível 999] → Jogadores que estavam a um passo de atingir 999 sem custo perderão esse "desconto". Risco mínimo dado que o bug é recente.
- [Sem migração de dados] → Saldos inflados por abuso permanecem. Aceitável para o escopo atual.

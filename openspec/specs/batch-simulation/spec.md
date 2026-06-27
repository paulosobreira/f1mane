# Spec: batch-simulation

## Purpose

Script de automação para executar múltiplas simulações sequenciais e analisar logs de colisão, facilitando a detecção de sobreposições problemáticas entre carros.

## Requirements

### Requirement: Script de simulação em lote
O repositório SHALL conter o script `simulacao_batch.sh` na raiz do projeto que executa N simulações com combinações aleatórias de temporada/circuito/voltas e filtra os logs de colisão gerados.

#### Scenario: Script executa múltiplas simulações sequencialmente
- **WHEN** `simulacao_batch.sh` é executado sem argumentos
- **THEN** o script executa pelo menos 5 simulações com parâmetros variados (ex: 2024 com diferentes circuitos e 30-72 voltas)
- **THEN** ao final de cada simulação o script extrai e exibe as linhas `[COLISAO_EVENTO]` do log gerado

#### Scenario: Script indica ausência de colisões problemáticas
- **WHEN** nenhuma linha `[COLISAO_EVENTO]` contendo tipo `DIANTEIRA_CENTRO` é encontrada nos logs
- **THEN** o script exibe mensagem `OK: sem sobreposicoes detectadas` para aquela simulação

#### Scenario: Script indica colisões encontradas
- **WHEN** linhas `[COLISAO_EVENTO]` com `DIANTEIRA_CENTRO` são encontradas nos logs de uma simulação
- **THEN** o script exibe `ATENCAO: sobreposicoes detectadas em <temporada> <circuito> <voltas>` e conta o número de ocorrências

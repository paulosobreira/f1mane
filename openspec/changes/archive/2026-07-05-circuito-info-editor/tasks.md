## 1. Circuito ganha a propriedade ciclo

- [x] 1.1 Adicionar campo `ciclo` (int, default ~160) com getter/setter a `Circuito`, seguindo o padrão de `probalidadeChuva`
- [x] 1.2 Incluir `ciclo` na cópia de metadados (`Circuito.copiaParaArquivoMetadados()`, ver capability `circuito-metadados-arquivo`) — mantido junto dos demais campos leves
- [x] 1.3 Atualizar `ControleRecursos.carregarCircuitos()` para não ler mais o terceiro campo de `circuitos.properties` como ciclo (linha passa a ter `NomeExibicao,ativo`)
- [x] 1.4 Atualizar `ControleJogoLocal.tempoCicloCircuito()` e `JogoCliente.tempoCicloCircuito()` para retornar `circuito.getCiclo()` em vez de `circuitosCiclo.get(circuito.getNome())`
- [x] 1.5 Remover (ou deixar de popular, se algum outro consumidor ainda depender) `ControleRecursos.circuitosCiclo`

## 2. Migração dos circuitos existentes

- [x] 2.1 Escrever um utilitário (classe `main` temporária) que, para cada linha de `circuitos.properties`, lê o valor de ciclo, grava esse valor no campo `ciclo` do circuito correspondente (arquivo de metadados) e remove o campo de ciclo da linha — `br.f1mane.recursos.MigracaoCicloCircuito`
- [x] 2.2 Rodar o utilitário e conferir que todos os circuitos existentes têm `ciclo` migrado corretamente, e que `circuitos.properties` ficou só com `NomeExibicao,ativo` por linha — 37/37 circuitos migrados, conferido via grep no arquivo de metadados e nas linhas de circuitos.properties

## 3. Tempo de volta estimado

- [x] 3.1 Definir as constantes de ganho médio por tipo de nó (`GANHO_MEDIO_RETA`, `GANHO_MEDIO_CURVA_ALTA`, `GANHO_MEDIO_CURVA_BAIXA`), documentando a faixa de `Piloto.calculaModificadorPrincipal()` de onde vieram — `Circuito.estimarTempoVoltaMs()`
- [x] 3.2 Implementar o cálculo: somar `1 / ganhoMedio(tipo)` por nó de `pistaFull`, multiplicar pelo `ciclo` atual, formatar como tempo (mm:ss.SSS) — cálculo em `Circuito.estimarTempoVoltaMs()`; formatação reaproveita `ControleEstatisticas.formatarTempo(Long)`
- [x] 3.3 Adicionar um `JLabel` somente-leitura no editor mostrando o tempo de volta estimado
- [x] 3.4 Recalcular esse label quando `ciclo` for alterado (spinner) e quando o circuito for revetorizado

## 4. Distância em quilômetros (campo informado, não calculado)

- [x] 4.1 Adicionar campo `distanciaKm` (double, default 0) com getter/setter a `Circuito`
- [x] 4.2 Incluir `distanciaKm` na cópia de metadados (`Circuito.copiaParaArquivoMetadados()`)
- [x] 4.3 Adicionar um campo numérico editável no editor (`JTextField`/`JSpinner`, mesmo padrão de `probalidadeChuvaText`) para `distanciaKm`, gravando em `circuito.setDistanciaKm(...)` quando o usuário altera o valor

## 5. Editor — campo de ciclo

- [x] 5.1 Adicionar um spinner de `ciclo` na barra superior do editor (substituindo a dependência de `circuitos.properties` para esse valor), gravando em `circuito.setCiclo(...)` e disparando o recálculo de tempo de volta estimado

## 6. Testes

- [x] 6.1 Teste unitário: `Circuito` sem `ciclo` definido explicitamente assume o valor padrão — `CircuitoInfoEditorTest`
- [x] 6.2 Teste unitário: `tempoCicloCircuito()` retorna `circuito.getCiclo()`, não depende de `circuitosCiclo`/`circuitos.properties` — `ControleJogoLocalTempoCicloCircuitoTest`
- [x] 6.3 Teste unitário: cálculo de tempo de volta estimado para um circuito de teste com contagens conhecidas de nós por tipo, conferindo a fórmula (soma de ticks × ciclo) — `CircuitoInfoEditorTest`
- [x] 6.4 Teste unitário: `distanciaKm` tem default zero e reflete o valor informado via setter, sem ser alterada por `vetorizarPista()` — `CircuitoInfoEditorTest`
- [x] 6.5 Teste unitário: migração preserva o valor de `ciclo` de `circuitos.properties` para o circuito correspondente — `MigracaoCicloCircuitoTest` (após parametrizar `MigracaoCicloCircuito.migrarLinha` com o diretório de circuitos, pra ser testável sem tocar os recursos reais)

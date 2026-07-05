## 1. Circuito ganha a propriedade ciclo

- [ ] 1.1 Adicionar campo `ciclo` (int, default ~160) com getter/setter a `Circuito`, seguindo o padrão de `probalidadeChuva`
- [ ] 1.2 Incluir `ciclo` na cópia de metadados (`Circuito.copiaParaArquivoMetadados()`, ver capability `circuito-metadados-arquivo`) — mantido junto dos demais campos leves
- [ ] 1.3 Atualizar `ControleRecursos.carregarCircuitos()` para não ler mais o terceiro campo de `circuitos.properties` como ciclo (linha passa a ter `NomeExibicao,ativo`)
- [ ] 1.4 Atualizar `ControleJogoLocal.tempoCicloCircuito()` e `JogoCliente.tempoCicloCircuito()` para retornar `circuito.getCiclo()` em vez de `circuitosCiclo.get(circuito.getNome())`
- [ ] 1.5 Remover (ou deixar de popular, se algum outro consumidor ainda depender) `ControleRecursos.circuitosCiclo`

## 2. Migração dos circuitos existentes

- [ ] 2.1 Escrever um utilitário (classe `main` temporária) que, para cada linha de `circuitos.properties`, lê o valor de ciclo, grava esse valor no campo `ciclo` do circuito correspondente (arquivo de metadados) e remove o campo de ciclo da linha
- [ ] 2.2 Rodar o utilitário e conferir que todos os circuitos existentes têm `ciclo` migrado corretamente, e que `circuitos.properties` ficou só com `NomeExibicao,ativo` por linha

## 3. Tempo de volta estimado

- [ ] 3.1 Definir as constantes de ganho médio por tipo de nó (`GANHO_MEDIO_RETA`, `GANHO_MEDIO_CURVA_ALTA`, `GANHO_MEDIO_CURVA_BAIXA`), documentando a faixa de `Piloto.calculaModificadorPrincipal()` de onde vieram
- [ ] 3.2 Implementar o cálculo: somar `1 / ganhoMedio(tipo)` por nó de `pistaFull`, multiplicar pelo `ciclo` atual, formatar como tempo (mm:ss.SSS)
- [ ] 3.3 Adicionar um `JLabel` somente-leitura no editor mostrando o tempo de volta estimado
- [ ] 3.4 Recalcular esse label quando `ciclo` for alterado (spinner) e quando o circuito for revetorizado

## 4. Distância em quilômetros

- [ ] 4.1 Definir a constante `METROS_POR_PIXEL` (derivada de `Carro.ALTURA` e o comprimento aproximado de um carro real)
- [ ] 4.2 Implementar o cálculo: `pistaFull.size() * METROS_POR_PIXEL / 1000`
- [ ] 4.3 Adicionar um `JLabel` somente-leitura no editor mostrando a distância em km, recalculado junto com o tempo de volta estimado

## 5. Editor — campo de ciclo

- [ ] 5.1 Adicionar um spinner de `ciclo` na barra superior do editor (substituindo a dependência de `circuitos.properties` para esse valor), gravando em `circuito.setCiclo(...)` e disparando o recálculo de tempo de volta estimado

## 6. Testes

- [ ] 6.1 Teste unitário: `Circuito` sem `ciclo` definido explicitamente assume o valor padrão
- [ ] 6.2 Teste unitário: `tempoCicloCircuito()` retorna `circuito.getCiclo()`, não depende de `circuitosCiclo`/`circuitos.properties`
- [ ] 6.3 Teste unitário: cálculo de tempo de volta estimado para um circuito de teste com contagens conhecidas de nós por tipo, conferindo a fórmula (soma de ticks × ciclo)
- [ ] 6.4 Teste unitário: cálculo de distância em km para um `pistaFull` de tamanho conhecido
- [ ] 6.5 Teste unitário: migração preserva o valor de `ciclo` de `circuitos.properties` para o circuito correspondente

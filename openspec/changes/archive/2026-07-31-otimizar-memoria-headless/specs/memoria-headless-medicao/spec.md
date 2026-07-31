## ADDED Requirements

### Requirement: Linha de base de memória do servidor headless medida e registrada
O projeto SHALL dispor de um procedimento reprodutível, versionado em `utilitarios/`, que meça o consumo de memória do processo headless em dois pontos — heap usado após a subida completa (pré-geração pulada, porta bindada) e heap usado com N corridas ativas — e registre o resultado num formato comparável entre execuções.

#### Scenario: Medição pós-boot
- **WHEN** o procedimento de medição é executado contra um servidor headless recém-subido, sem nenhuma corrida
- **THEN** ele reporta o heap usado após coleta, o número de classes carregadas e o uso de metaspace

#### Scenario: Medição com corridas ativas
- **WHEN** o procedimento é executado com N sessões jogando no servidor
- **THEN** ele reporta o custo incremental de memória por sessão, permitindo comparar antes e depois desta mudança

#### Scenario: Cenário de carga montado a partir do próprio servidor
- **WHEN** o procedimento precisa escolher temporada, circuito e piloto para criar a carga
- **THEN** ele os obtém dos endpoints REST do servidor em execução, sem valores fixos no script que quebrem quando o conteúdo do jogo muda

#### Scenario: Execução sem ferramentas externas
- **WHEN** o procedimento roda no container de produção
- **THEN** ele usa apenas ferramentas presentes na imagem (JDK/JRE embutido e utilitários do sistema), sem exigir instalação adicional

### Requirement: Ganho de memória comprovado por número, não por impressão
A conclusão desta mudança SHALL ser condicionada à comparação das medições antes e depois usando o mesmo procedimento e o mesmo cenário de carga, com os dois resultados registrados no repositório.

#### Scenario: Resultado registrado
- **WHEN** as alterações de runtime e de remoção de Swing do caminho servidor estão implementadas
- **THEN** existem no repositório os números de antes e de depois (pós-boot e por corrida), obtidos pelo mesmo procedimento e cenário

#### Scenario: Ausência de ganho é reportada
- **WHEN** a medição posterior não mostra redução relevante em algum dos pontos medidos
- **THEN** o fato é registrado explicitamente junto aos números, em vez de a mudança ser declarada concluída com base em expectativa

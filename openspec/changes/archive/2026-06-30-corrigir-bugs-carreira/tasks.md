## 1. Corrigir bug de custo no nível 999

- [x] 1.1 Em `Util.java:423`, alterar condição `proximoValor < 999` para `proximoValor <= 999` em `processaValorPontosCarreira`
- [x] 1.2 Verificar que o upgrade 998→999 agora debita 50 pontos (custo simétrico ao downgrade)

## 2. Remover setter duplicado

- [x] 2.1 Em `ControleClassificacao.java:417`, remover a segunda chamada duplicada a `setIdCarroLivery`

## 3. Verificação

- [x] 3.1 Compilar o projeto sem erros (`mvn clean package -Ph2 -DskipTests`)
- [x] 3.2 Simular ciclo downgrade 999→998 + upgrade 998→999 e confirmar que o pool de construtores volta ao valor original

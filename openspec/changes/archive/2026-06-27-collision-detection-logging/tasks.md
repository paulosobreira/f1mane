## 1. Flag e infraestrutura de log

- [x] 1.1 Adicionar `public static boolean LOG_COLISAO = false;` em `br.f1mane.Global`
- [x] 1.2 Em `MainFrameSimulacao.main()`, definir `Global.LOG_COLISAO = true` antes de iniciar a corrida

## 2. Logging detalhado em processaColisao

- [x] 2.1 Após `centralizaCarroColisao()` em `processaColisao()`, quando `Global.LOG_COLISAO` for true, emitir log `[COLISAO]` com: nome do piloto, `noAtual.getIndex()`, `tracado`, bounds de `diateiraColisao`/`centroColisao`/`trazeiraColisao`, e `ganho`
- [x] 2.2 Quando colisão detectada (`colisao != null`), emitir log `[COLISAO_EVENTO]` com nomes dos dois pilotos, índices na pista e tipo da colisão (DIANTEIRA_TRAZEIRA, DIANTEIRA_CENTRO ou CENTRO_TRAZEIRA) determinado pelos flags `colisaoDiantera`/`colisaoCentro` e qual hitbox do carro à frente foi intersectada

## 3. Bloqueio físico em processaPenalidadeColisao

- [x] 3.1 Em `processaPenalidadeColisao()`, quando `getColisao() != null`, obter o `ganho` do piloto à frente (`getColisao().getGanho()`)
- [x] 3.2 Aplicar cap: se `colisaoDiantera` e interseção com `centroColisao` do piloto à frente → `ganho = Math.min(ganho, ganhoFrente)`
- [x] 3.3 Aplicar desaceleração: se `colisaoDiantera` e interseção apenas com `trazeiraColisao` → `ganho = Math.min(ganho * 0.7, ganhoFrente)`
- [x] 3.4 Aplicar cap para colisão centro-traseira: se `colisaoCentro` e não `colisaoDiantera` → `ganho = Math.min(ganho, ganhoFrente)`
- [x] 3.5 Garantir que o cap de ganho não é aplicado quando os pilotos estão em traçados diferentes

## 4. Primeira simulação e análise de logs

- [x] 4.1 Compilar o projeto: `mvn clean package -Ph2 -DskipTests`
- [x] 4.2 Rodar simulação: `java -cp target/flmane.jar br.f1mane.MainFrameSimulacao 2024 Catalunya 72`
- [x] 4.3 Analisar logs em `logs/` buscando linhas `[COLISAO_EVENTO]` com tipo `DIANTEIRA_CENTRO` — verificar se sobreposições ocorrem
- [x] 4.4 Se sobreposições encontradas, ajustar lógica de cap (tasks 3.x) e repetir; se ausentes, prosseguir

## 5. Script de simulação em lote

- [x] 5.1 Criar `simulacao_batch.sh` na raiz do projeto com pelo menos 5 simulações (2024/2023 + diferentes circuitos + 30-72 voltas)
- [x] 5.2 O script deve extrair linhas `[COLISAO_EVENTO]` de cada log e exibir resumo: `OK` ou `ATENCAO` por simulação
- [x] 5.3 Rodar `simulacao_batch.sh` e confirmar que nenhuma sobreposição (`DIANTEIRA_CENTRO`) é detectada

## 6. Validação final com atualização suave

- [x] 6.1 Testar com `MainFrame` (modo visual com `ControleCiclo.VALENDO=true`) e verificar que não há regressões visuais de ultrapassagem ou comportamento estranho de carros
- [x] 6.2 Verificar que `Global.LOG_COLISAO` está `false` no `MainFrame`/`MainLauncher` (sem impacto em logs de produção)

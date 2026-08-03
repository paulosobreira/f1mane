# Medições de memória — modo headless

Registro dos números de antes e depois desta mudança. Toda linha aqui vem de
`utilitarios/medir_memoria_headless.sh`, sempre no mesmo cenário — comparar
execuções feitas em cenários diferentes não vale como evidência.

## Ferramenta

A imagem de produção é `eclipse-temurin:25-jre-alpine`, uma JRE: **não tem
`jcmd`**, só `java` e `jfr`. A medição usa, por isso:

- **RSS do processo** — `grep VmRSS /proc/1/status` dentro do container, é o
  número que o runtime/cgroup enxerga e o que motiva OOM-kill;
- **Flight Recorder** — injetado no boot via `JAVA_TOOL_OPTIONS`
  (`FLMANE_JAVA_TOOL_OPTIONS` no compose, vazio no uso normal), lido com `jfr`
  depois da parada do container, para `jdk.GCHeapSummary` (heap após GC),
  `jdk.MetaspaceSummary` e `jdk.ClassLoadingStatistics`.

Nenhuma ferramenta externa é instalada e o `ENTRYPOINT` de produção não é
alterado para medir.

## Cenário de carga fixo

Toda medição SEGUE exatamente estes passos (é o que o script automatiza):

1. `compose -f docker-compose.yaml down` — derruba a stack anterior (só
   `flmane` + `db`, sem phpmyadmin/sonarqube do override de dev).
2. Sobe a stack de produção com o Flight Recorder ligado.
3. Espera `GET /flmane/rest/letsRace/verificaServico` responder. A imagem já
   traz as imagens assadas no build, então a pré-geração é **pulada pelo
   marcador** — o ponto pós-boot mede o regime, não o pico de pré-geração.
4. **Ponto 1 — pós-boot:** RSS com zero corridas, 5 s após o servidor responder.
5. Cria **N = 3 corridas** (default), uma por sessão visitante:
   `criarSessaoVisitante` → `jogar/{temporada}/{piloto}/{circuito}/10/M/50/5/false`,
   com 10 voltas, pneu `M`, combustível 50, asa 5, fora do modo carreira.
   Temporada, circuito e piloto vêm do primeiro item que o próprio servidor
   devolve, para não depender de dados hardcoded que mudam entre temporadas.
6. Deixa as corridas rodarem por 30 s.
7. **Ponto 2 — com carga:** RSS com as N corridas ativas.
8. Para o container (o JFR é gravado por `dumponexit`) e extrai os eventos.

Custo por corrida = (RSS com carga − RSS pós-boot) / N.

Comando:

```bash
utilitarios/medir_memoria_headless.sh --rotulo antes    # linha de base
utilitarios/medir_memoria_headless.sh --rotulo depois   # após as mudanças
```

## Linha de base (antes)

Imagem `sowbreira/flmane:latest` construída antes desta mudança (`ENTRYPOINT`
sem `-Djava.awt.headless=true` e sem `-XX:MaxRAMPercentage`, sem `mem_limit` no
compose). Host com ~31 GB de RAM.

```
================ MEDICAO: antes ================
data:                2026-07-31T14:40:24-03:00
sessoes jogando:     1
cenario:             temporada=2026 circuito=montreal_mro.xml piloto=2 voltas=10

RSS pos-boot:        146924 kB (143.5 MiB)
RSS com carga:       342480 kB (334.5 MiB)
custo por sessao:    191.0 MiB

--- eventos JFR ---
heap usado apos GC:  31.0 MB
heap committed:      120.0 MB
metaspace usado:     59.2 MB
classes carregadas:  11712
================================================
```

Numa execução anterior do mesmo cenário, **sem** nenhuma corrida (só as
chamadas REST de metadados), o heap committed chegou a **500 MB** com apenas
**14.5 MB** usados após GC, e o RSS a **455 MiB** — o `reservedSize` era de
7.7 GB, ou seja, 1/4 da RAM do host. É esse descolamento entre o que a corrida
usa e o que a JVM reserva que o `MaxRAMPercentage` + `mem_limit` atacam.

Ressalvas honestas desta linha de base:

- Só **1 sessão** entrou na corrida das 3 tentadas; o "custo por sessão" de
  191 MiB é, na prática, o custo de *ativar* uma corrida (carregar circuito,
  grid, iniciar o ciclo), não o custo marginal de um jogador a mais.
- `ControlePaddockServidor.jogar` entra no primeiro jogo existente, então
  N sessões não viram N corridas — o cenário mede uma corrida com N jogadores.

## Depois

Imagem reconstruída com o `ENTRYPOINT` novo (`-Djava.awt.headless=true`,
`-XX:MaxRAMPercentage=75`), `mem_limit: 1g` no compose, liberação dos caches de
pré-geração, `atualizaInfoDebug()` guardado e `InterfaceJogo` sem Swing.

```
================ MEDICAO: depois ================
data:                2026-07-31T15:09:48-03:00
sessoes jogando:     1
cenario:             temporada=2026 circuito=montreal_mro.xml piloto=2 voltas=10

RSS pos-boot:        104384 kB (101.9 MiB)
RSS com carga:       285224 kB (278.5 MiB)
custo por sessao:    176.6 MiB

--- eventos JFR ---
heap usado apos GC:  38.7 MB
heap committed:      65.1 MB
metaspace usado:     58.7 MB
classes carregadas:  11715
=================================================
```

Verificações do container novo:

- `cat /proc/1/cmdline` → `java -Djava.awt.headless=true -XX:MaxRAMPercentage=75 -jar app.jar --headless`
- `MaxHeapSize = 805306368` (768 MiB = 75% do `mem_limit: 1g`), contra os 7.7 GB de heap reservado do antes
- log do boot: `IMAGENS HEADLESS JA PRE-GERADAS, REAPROVEITANDO: /app/imagens-headless`
- serving validado ponta a ponta: `circuitoBg` 508 KB, `carroLado` 11 KB, `jogar` HTTP 200 com 22 pilotos no grid

## Conclusão

| Métrica | Antes | Depois | Δ |
|---|---|---|---|
| RSS pós-boot | 143.5 MiB | 101.9 MiB | **−29%** |
| RSS com carga | 334.5 MiB | 278.5 MiB | **−17%** |
| Heap committed | 120 MB | 65.1 MB | **−46%** |
| Heap reservado | 7.7 GB | 768 MiB | limitado pelo cgroup |
| Metaspace usado | 59.2 MB | 58.7 MB | ~igual |
| Classes carregadas | 11712 | 11715 | ~igual |

O ganho veio de onde a análise apontava: **dimensionamento de heap** (o G1 deixou de
reservar 1/4 da RAM do host e de commitar muito além do uso real) e **liberação dos
caches de pré-geração** (o pós-boot caiu 41 MiB).

Pontos **sem ganho relevante**, registrados como tal em vez de mascarados:

- **Metaspace e classes carregadas ficaram praticamente iguais** (59.2 → 58.7 MB;
  11712 → 11715 classes). A separação `InterfaceJogo`/`InterfaceJogoVisual` não
  reduziu a árvore de classes carregadas de forma mensurável: `ControleJogoLocal`
  (superclasse de `JogoServidor`) ainda referencia `GerenciadorVisual` e
  `PainelTabelaResultadoFinal` nos corpos de método, e `LetsRace` ainda instancia
  `PainelCircuito` no fallback de fundo de circuito. O valor entregue ali é de
  contrato/robustez, não de bytes — e continuará assim até `br.flmane.visao` sair
  também dos corpos de método do controle, o que é uma mudança maior.
- **`heap usado após GC` subiu** (31.0 → 38.7 MB). Não é regressão: com heap
  committed muito menor, o G1 coleta em outro ritmo e a amostra é de um instante
  diferente da corrida. O número que importa para o container — RSS — caiu nos dois
  pontos.
- **O custo por sessão quase não mudou** (191.0 → 176.6 MiB) porque nenhuma das
  mudanças ataca o custo de ativar uma corrida (carregar circuito, montar o grid,
  iniciar o ciclo). Reduzir isso é trabalho para outra mudança.

## Conclusão

<!-- Preenchido pela tarefa 7.2 — inclusive pontos sem ganho relevante -->

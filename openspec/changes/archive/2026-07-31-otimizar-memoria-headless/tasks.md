## 1. Medição — linha de base

- [x] 1.1 Criar `utilitarios/medir_memoria_headless.sh`: sobe a stack, mede RSS do processo em cada ponto e reporta heap após GC, metaspace e classes carregadas via Flight Recorder (a imagem JRE não tem `jcmd`)
- [x] 1.2 Definir e documentar o cenário de carga fixo usado na medição (boot limpo com imagens já assadas; depois N corridas ativas), para que antes e depois sejam comparáveis
- [x] 1.3 Rodar a medição contra a imagem atual e registrar os números de linha de base em `openspec/changes/otimizar-memoria-headless/medicoes.md`

## 2. Runtime da JVM no container

- [x] 2.1 Alterar o `ENTRYPOINT` do `flmane.dockerfile` para incluir `-Djava.awt.headless=true` e `-XX:MaxRAMPercentage=<valor>`, mantendo a forma `exec`
- [x] 2.2 Adicionar teste que lê `flmane.dockerfile` e falha se o `ENTRYPOINT` não contiver `-Djava.awt.headless=true`
- [x] 2.3 Documentar as flags de runtime headless na seção Docker Compose do `CLAUDE.md`
- [x] 2.4 Rebuild da imagem (`utilitarios/build_container.sh`) e verificação de que o servidor sobe, pula a pré-geração pelo marcador e serve imagens e corrida

## 3. Estruturas estáticas de vida longa

- [x] 3.1 Converter `Logger.topExceptions` em `LinkedHashMap` com `removeEldestEntry` no limite de 10.000 (descarte LRU em vez de parar de registrar)
- [x] 3.2 Teste unitário: acima do limite, o mapa permanece no tamanho máximo e mantém as assinaturas mais recentes
- [x] 3.3 Liberar em `MainLauncher`, ao final de `garantirImagensHeadless`, as estruturas usadas só pela pré-geração (listas de pilotos/carros por temporada, circuitos desserializados, caches auxiliares) antes do bind da porta
- [x] 3.4 Teste de que um boot com marcador presente não desserializa circuito nem lista de pilotos por conta da pré-geração

## 4. Corrida headless sem thread de rendering

- [x] 4.1 Introduzir sinal explícito de "sem rendering" nos construtores de `ControleJogoLocal`, substituindo o `if (!(this instanceof JogoServidor))`
- [x] 4.2 ~~Fazer `MainFrameSimulacao` usar o construtor sem rendering~~ — **não aplicável**: `MainFrameSimulacao extends MainFrame extends JFrame` e desenha de verdade (só desliga camadas via `PainelCircuito.desenhaBkg/desenhaImagens/desenhaPista`); é modo GUI, não headless
- [x] 4.3 Fazer `JogoServidor` usar o sinal explícito em vez de depender do `instanceof` da superclasse
- [x] 4.4 Teste: corrida criada em modo sem rendering não instancia `GerenciadorVisual` e não inicia thread de rendering
- [x] 4.5 ~~Verificar por simulação de seed fixa~~ — **não aplicável**: nenhum modo perdeu o `GerenciadorVisual`. `JogoServidor` já não o criava (via `instanceof`); a mudança só troca o mecanismo de decisão por um parâmetro explícito, coberto por `ControleJogoLocalRenderingOptInTest`
- [ ] 4.6 Verificar manualmente que o modo solo (`br.f1mane.MainFrame`) continua com rendering normal — **pendente do usuário**: exige abrir a GUI, não verificável nesta sessão

## 5. Componentes Swing sob demanda

- [x] 5.1 Guardar `ControleEstatisticas.atualizaInfoDebug()` para retornar cedo quando `infoTextual` for nulo, sem montar o texto nem chamar `SwingUtilities.invokeLater`
- [x] 5.2 Teste: atualizar estatísticas sem painel criado não instancia componente, não agenda tarefa na EDT e não lança exceção
- [x] 5.3 Conferir que `painelNarracao()`, `painelDebug()` e `obterResultadoFinal()` só alocam quando chamados, e que nenhum caminho de corrida servidor os chama

## 6. Isolamento do contrato de visão

- [x] 6.1 Extrair de `InterfaceJogo` os pontos que devolvem componentes gráficos para uma interface separada (`InterfaceJogoVisual`), implementada por `ControleJogoLocal`
- [x] 6.2 Ajustar `MainFrame`, applet e demais consumidores gráficos para o novo contrato, sem mudança de comportamento
- [x] 6.3 Remover `import javax.swing` e os tipos de `br.flmane.visao` de `InterfaceJogo`
- [x] 6.4 Teste: rodar uma corrida completa em modo headless e verificar que nenhuma classe de rendering de `br.flmane.visao` foi carregada por conta do caminho de corrida
- [x] 6.5 Rodar `mvn test` completo e confirmar suíte verde

## 7. Fechamento

- [x] 7.1 Rodar a medição pós-mudança no mesmo cenário e registrar os números em `medicoes.md`, ao lado da linha de base
- [x] 7.2 Registrar explicitamente qualquer ponto medido sem ganho relevante, em vez de declarar conclusão por expectativa
- [x] 7.3 Rodar `mvn clean package -Ph2 -DskipTests` para manter `target/flmane.jar` atualizado
- [x] 7.4 Reconstruir a imagem de produção e validar boot, serving de imagens e uma corrida ponta a ponta pelo cliente web

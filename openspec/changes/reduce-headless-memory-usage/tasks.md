## 1. Flag de JVM headless no processo filho

- [ ] 1.1 Adicionar `-Djava.awt.headless=true` ao comando montado em `MainLauncher.iniciarProcessoServidor` (`MainLauncher.java:136-150`)
- [ ] 1.2 Confirmar manualmente que `GraphicsEnvironment.isHeadless()` retorna `true` no processo filho e que `ImageUtil.toCompatibleImage` (`ImageUtil.java:294-320`) entra no atalho headless (log/verificação pontual, sem teste automatizado dedicado)

## 2. Diretório de imagens pré-geradas

- [ ] 2.1 Criar utilitário para o diretório temporário de imagens headless (`<tmp>/flmane-imagens-headless/{circuitos,carros,capacetes}/`), recriado do zero a cada boot, no mesmo padrão de `extrairWebapp()` (`MainLauncher.java:152-191`)
- [ ] 2.2 Definir convenção de nome de arquivo por asset (circuito+jpg/mini; temporada+carro para carro-lado/carro-cima com e sem aerofólio; temporada+piloto para capacete) de forma determinística e reproduzível pelos endpoints de leitura

## 3. Flag de modo headless-disco em `CarregadorRecursos`/`SpriteSheet`

- [ ] 3.1 Introduzir flag estática (ex.: `modoHeadlessDisco`) em `CarregadorRecursos`, setada por `MainLauncher.iniciarServidorHeadless()` antes de qualquer geração de imagem
- [ ] 3.2 Quando a flag estiver ativa, impedir que `obterCarroCima`/`obterCarroLado`/`obterCapacete`, `pintarModeloV2`, `pintarMonocromatico`, `desenhaCapacete` (`CarregadorRecursos.java`) insiram resultado em `bufferImages`/`bufferCarros`/`cacheModeloV2`/`cacheMonocromatico`
- [ ] 3.3 Aplicar o mesmo comportamento em `SpriteSheet.carregar` (`SpriteSheet.java:40-52`) quando a flag estiver ativa

## 4. Pré-geração sequencial no boot

- [ ] 4.1 Em `MainLauncher.iniciarServidorHeadless()` (`MainLauncher.java:95-134`), após subir os componentes essenciais, iterar circuitos com `ativo=true` (via `ControleRecursos.carregarCircuitos()`/`circuitoAtivo`) gerando fundo (`DesenhoProceduralCircuito.geraImagem` ou `PainelCircuito.desenhaCircuito`, conforme `Global.MODO_HOMENAGEM`) e miniatura (`Circuito.desenhaMiniCircuito`), gravando cada uma no diretório do passo 2 e descartando a referência em memória logo em seguida
- [ ] 4.2 Iterar temporadas configuradas (`carregarTemporadas()`) e, para cada carro/piloto, gerar carro-lado, carro-cima (com e sem aerofólio) e capacete, gravando em disco e descartando a referência em memória logo em seguida
- [ ] 4.3 Logar e pular (sem abortar o boot) qualquer asset cuja geração falhe, registrando o erro

## 5. Endpoints de `LetsRace` servindo do disco

- [ ] 5.1 Alterar `circuitoJpg`, `circuitoBg`, `circuitoMini` (`LetsRace.java:322-378`) para, em modo headless-disco, ler os bytes do arquivo pré-gerado correspondente em vez de gerar/cachear um `BufferedImage`
- [ ] 5.2 Alterar `carroCimaTemporadaCarro`, `carroCimaSemAreofolioTemporadaCarro`, `capaceteTemporadaPiloto`, `carroLadoTemporadaCarro` (`LetsRace.java:418-492`) da mesma forma
- [ ] 5.3 Implementar fallback preguiçoso: se o arquivo esperado não existir (circuito/temporada/carro adicionado após o boot), gerar sob demanda, gravar no mesmo diretório e servir — usando um lock por chave (caminho do arquivo) para evitar geração duplicada em requisições concorrentes, sem cachear o `BufferedImage` resultante

## 6. Liberação de objetos de desenho de `Circuito`

- [ ] 6.1 Auditar (grep/leitura) quais campos de `Circuito`/`No` são lidos por `ControleCorrida`, `ControleSafetyCar`, `ControleBox` e `ControleAutomacao` durante uma corrida, distinguindo-os dos campos usados só por `DesenhoProceduralCircuito`/`PainelCircuito` para desenho
- [ ] 6.2 Implementar `Circuito.liberarObjetosDesenho()`, zerando apenas as referências identificadas como exclusivas de desenho no passo 6.1
- [ ] 6.3 Chamar `liberarObjetosDesenho()` logo após a gravação em disco da imagem do circuito, tanto na pré-geração do boot (passo 4.1) quanto no fallback sob demanda (passo 5.3)
- [ ] 6.4 Rodar `./simulacao.sh` para uma corrida completa após a liberação e comparar o resultado (posições finais, sem exceções) com uma execução antes da mudança, confirmando que a mecânica de jogo não foi afetada

## 7. Build e verificação manual

- [ ] 7.1 Rodar `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar` após as alterações em `src/main/java` (obrigatório por `CLAUDE.md` antes de considerar a tarefa concluída)
- [ ] 7.2 Rodar `java -jar target/flmane.jar --headless` (ou `docker compose`) e verificar no client web (`http://localhost:8080/flmane/html5/index.html`) que fundo de circuito, carros e capacetes carregam corretamente
- [ ] 7.3 Verificar manualmente que o diretório de imagens pré-geradas contém os arquivos esperados após o boot e que os caches estáticos de `CarregadorRecursos`/`SpriteSheet` não crescem ao longo de múltiplas requisições em modo headless
- [ ] 7.4 Confirmar que o modo GUI/solo (`MainFrame`) continua funcionando sem alterações (fora do escopo desta mudança, mas não deve regredir)

## 1. Constantes de teto

- [x] 1.1 Adicionar `TETO_VELOCIDADE_MIN` (370) e `TETO_VELOCIDADE_MAX` (375) como constantes em `Piloto.java`. (Nota: `290`, base da fórmula de fallback, e `320`, usado em efeito visual de chuva não coberto por nenhuma tarefa, foram mantidos intactos — só o `330` da tarefa 4.1 era de fato uma referência de teto.)

## 2. Cálculo de velocidade real

- [x] 2.1 Em `Piloto.calculoVelocidade(double ganho)`, quando `controleJogo.getCircuito().getDistanciaKm() != 0`, calcular `velocidadeKmh = (ganho * distanciaKm * 3600.0) / (nosDaPista.size() * tempoCicloCircuito())` (arredondando pra inteiro), em vez da fórmula baseada em `ganhoMax`/combustível.
- [x] 2.2 Manter a fórmula atual (`val`/`ganhoMax`/combustível) como fallback quando `getDistanciaKm() == 0`.
- [x] 2.3 Extrair o cálculo real pra um método privado dedicado (`calculoVelocidadeReal`), mantendo `calculoVelocidade` como o ponto único que decide entre fórmula real e fallback.

## 3. Teto com oscilação

- [x] 3.1 Adicionar campos de estado por piloto (`velocidadeTetoOscilacao`, `velocidadeTetoSubindo`) em `Piloto.java`.
- [x] 3.2 Implementar a lógica de oscilação: quando o valor calculado (real ou fallback) for `>= TETO_VELOCIDADE_MIN`, avançar a oscilação um passo em direção ao extremo oposto (subindo até `TETO_VELOCIDADE_MAX`, depois descendo até `TETO_VELOCIDADE_MIN`, repetindo), e usar esse valor oscilado como velocidade final em vez do valor calculado.
- [x] 3.3 Quando o valor calculado cair abaixo de `TETO_VELOCIDADE_MIN` novamente, voltar a usar o valor calculado diretamente (sem oscilação) e resetar o estado de oscilação.

## 4. Velocímetro (UI)

- [x] 4.1 Em `PainelCircuito.desenhaVelocidade()`, trocar o `330` fixo em `OcilaCor.porcentVermelho100Verde0((100 * velocidade / 330))` pela constante `TETO_VELOCIDADE_MAX` de `Piloto`.

## 5. Testes

- [x] 5.1 Teste unitário: circuito com `distanciaKm` informado produz velocidade proporcional a `ganho`, `distanciaKm`, `nosDaPista.size()` e `tempoCicloCircuito()`, batendo com a fórmula (antes do teto).
- [x] 5.2 Teste unitário: dois circuitos com `distanciaKm` diferentes (mesmo número de nós e mesmo ciclo) produzem velocidades diferentes para o mesmo `ganho`.
- [x] 5.3 Teste unitário: circuito com `distanciaKm == 0` produz o mesmo resultado que a fórmula antiga (fallback), pro mesmo `ganho`/combustível/reta-ou-curva.
- [x] 5.4 Teste unitário: velocidade calculada acima de `TETO_VELOCIDADE_MIN` nunca resulta em valor final acima de `TETO_VELOCIDADE_MAX`, e o valor final oscila entre `TETO_VELOCIDADE_MIN` e `TETO_VELOCIDADE_MAX` ao longo de ciclos consecutivos nessa condição.
- [x] 5.5 Teste unitário: dois pilotos distintos que atingem o teto em ciclos diferentes oscilam de forma independente (não compartilham fase).
- [x] 5.6 Rodar `mvn test` completo pra garantir que nenhum teste existente de `Piloto`/`PainelCircuito`/`ControleCorrida` quebrou com a mudança de fórmula. (556 testes, 0 falhas, 0 erros, 2 skipped — igual ao baseline.)

## 6. Validação manual

- [x] 6.1 Conferir, com o circuito real do Albert Park (`distanciaKm=5303`, `pistaFull.size()=18575`, `ciclo=140`), que `calculoVelocidadeReal` produz valores plausíveis pra faixa de `ganho` já usada pelo jogo (10-50 nas escadas de reta/curva de `calculaModificadorPrincipal`): 73–367 km/h, e que valores de `ganho` mais altos (60-80, fora da faixa normal) disparam corretamente o teto de 370–375. (`./simulacao.sh` citado no CLAUDE.md não existe no repo — validação feita carregando o `Circuito` real via `CarregadorRecursos.carregarCircuito` e chamando o cálculo diretamente, sem depender da simulação gráfica Swing.)

## 7. Reta sustentada (rampa artificial até o teto, só no valor exibido)

- [x] 7.1 Adicionar campo `tempoContinuoNaRetaMs` em `Piloto.java` e o método `atualizaTempoContinuoNaReta()`, que acumula `tempoCicloCircuito()` por ciclo enquanto `noAtual.verificaRetaOuLargada()` for verdadeiro e o piloto não estiver no traçado de fuga (`getTracado() == 4 || == 5`), zerando assim que uma das duas condições deixar de valer.
- [x] 7.2 Adicionar constantes `LIMIAR_RETA_SUSTENTADA_MS` (3000) e `INCREMENTO_VELOCIDADE_RETA_SUSTENTADA` (2).
- [x] 7.3 (Revertido e movido — ver 7.6) Primeira versão aplicava o incremento em `calculoVelocidade`/`velocidade`; corrigido a pedido do usuário ("incremento somente no valor visual da quilometragem") pra não vazar pra `ControleJogosServer` (rede) nem pros efeitos de chuva em `PainelCircuito`, que também leem `velocidade`.
- [x] 7.4 Testes unitários (versão final, sobre `velocidadeExibir`): (a) abaixo de 3s a suavização normal continua valendo; (b) ao cruzar 3s, `velocidadeExibir` passa a subir +2/ciclo ignorando `velocidade`; (c) mudar pra um nó de curva interrompe a rampa e volta à suavização normal no ciclo seguinte; (d) estar no traçado de fuga nunca ativa a rampa, mesmo com o nó da pista principal marcado como reta; (e) `velocidade` (real) nunca é alterada pela rampa, em nenhum dos cenários acima.
- [x] 7.5 Rodar `mvn test` completo pra confirmar que nenhum teste existente quebrou. (560 testes, 0 falhas, 0 erros, 2 skipped.)
- [x] 7.6 Mover o efeito de `calculoVelocidade` pra `calculaVelocidadeExibir()`: novos campos `velocidadeExibirTetoOscilacao`/`velocidadeExibirTetoSubindo` (estado de teto/oscilação dedicado, independente de `velocidadeTetoOscilacao`/`velocidadeTetoSubindo` usados pela velocidade real) e método `aplicaRetaSustentadaNaVelocidadeExibir()`, que incrementa `velocidadeExibir` em vez de `velocidade`. `calculoVelocidade` volta a conter só a fórmula real/fallback + teto, sem nenhuma menção a reta sustentada.

## 8. Zona de frenagem desativa o incremento artificial

- [x] 8.1 Em `calculaVelocidadeExibir()`, gatear a ativação de `aplicaRetaSustentadaNaVelocidadeExibir()` também em `!controleJogo.isNoZonaFrenagem(noAtual)`, além do limiar de 3s já existente — sem resetar `tempoContinuoNaRetaMs` ao entrar na zona (só suspende a aplicação, retomando no mesmo ciclo em que sai da zona se a contagem já estiver acima do limiar).
- [x] 8.2 Testes unitários: (a) na zona de frenagem, a rampa nunca ativa mesmo com 4s contínuos em reta; (b) ao sair da zona de frenagem com a contagem já acima do limiar, a rampa ativa no mesmo ciclo, sem precisar de novos 3s.
- [x] 8.3 Rodar `mvn test` completo pra confirmar que nenhum teste existente quebrou. (561 testes, 0 falhas, 0 erros, 2 skipped.)

## 9. Incremento tênue acima de 300 km/h

- [x] 9.1 Adicionar constantes `LIMIAR_VELOCIDADE_INCREMENTO_TENUE` (300), `LIMIAR_VELOCIDADE_INCREMENTO_MUITO_TENUE` (340) e `CICLOS_POR_INCREMENTO_MUITO_TENUE` (3) em `Piloto.java`.
- [x] 9.2 Extrair `calculaIncrementoRetaSustentada()`: abaixo de 300, retorna `INCREMENTO_VELOCIDADE_RETA_SUSTENTADA` (2); de 300 a 340, retorna 1; a partir de 340, retorna 1 só quando `(tempoContinuoNaRetaMs / tempoCicloCircuito()) % CICLOS_POR_INCREMENTO_MUITO_TENUE == 0`, senão 0 (reaproveitando `tempoContinuoNaRetaMs`, sem novo campo de estado).
- [x] 9.3 `aplicaRetaSustentadaNaVelocidadeExibir()` passa a somar `calculaIncrementoRetaSustentada()` em vez do incremento fixo.
- [x] 9.4 Testes unitários: (a) abaixo de 300, incremento continua 2; (b) entre 300 e 340, incremento cai pra 1; (c) a partir de 340, só incrementa 1 a cada 3 ciclos, ficando parado nos outros dois.
- [x] 9.5 Rodar `mvn test` completo pra confirmar que nenhum teste existente quebrou. (564 testes, 0 falhas, 0 erros, 2 skipped.)

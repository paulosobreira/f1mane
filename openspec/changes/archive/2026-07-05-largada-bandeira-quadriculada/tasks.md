## 1. Método de desenho compartilhado

- [x] 1.1 Implementar `DesenhoProceduralCircuito.desenhaLinhaDeLargada(Graphics2D, Circuito, double zoom)`: localizar o nó `No.LARGADA` em `pistaKey`, estimar a direção local da pista a partir do nó vizinho, calcular o vetor perpendicular escalado pela largura da pista
- [x] 1.2 Desenhar a faixa quadriculada (grade preto/branco) usando esse vetor perpendicular, com uma espessura fixa ao longo da direção da pista — grade `LARGADA_COLUNAS` × 2 quadrados
- [x] 1.3 Tratar o caso de não encontrar nó de largada: retornar sem desenhar, sem lançar exceção — `localizaNoLargada`/`localizaVizinhoNaPistaFull` retornam `null`, método retorna cedo

## 2. Integração no editor e na geração em memória

- [x] 2.1 Chamar `desenhaLinhaDeLargada` dentro de `DesenhoProceduralCircuito.desenha()`

## 3. Integração no jogo (PainelCircuito)

- [x] 3.1 Chamar `DesenhoProceduralCircuito.desenhaLinhaDeLargada` a partir de `PainelCircuito.desenhaBackGround()`, no ramo de imagem de fundo (depois do `drawImage`) e no ramo de fallback (`desenhaBackGroundComStrokes`)
- [x] 3.2 Confirmar que o zoom/offset de viewport (`zoom`, `descontoCentraliza`) usado por `PainelCircuito` é compatível com os parâmetros esperados pelo método compartilhado (ajustar assinatura se necessário para aceitar um offset de centralização) — sobrecarga `desenhaLinhaDeLargada(Graphics2D, Circuito, double, Point)` aceita o offset

## 4. Testes

- [x] 4.1 Teste unitário: `desenhaLinhaDeLargada` não lança exceção para um circuito de teste com um nó de largada válido — `DesenhoProceduralCircuitoTest.desenhaLinhaDeLargada_naoLancaExcecao_paraCircuitoComNoDeLargada`
- [x] 4.2 Teste unitário: `desenhaLinhaDeLargada` não lança exceção e não desenha nada para um circuito sem nó de largada — `DesenhoProceduralCircuitoTest.desenhaLinhaDeLargada_naoDesenhaNada_paraCircuitoSemNoDeLargada`
- [x] 4.3 Teste unitário/visual: renderizar um circuito de teste pequeno e conferir (via amostra de pixels ou imagem gerada) que aparecem pixels pretos e brancos alternados na posição esperada do nó de largada — `DesenhoProceduralCircuitoTest.desenhaLinhaDeLargada_desenhaQuadriculadoPretoEBrancoPertoDoNoDeLargada`
- [x] 4.4 Verificação manual: como não há display interativo disponível neste ambiente para rodar o jogo/editor Swing ao vivo, validar via renderização direta (gerar uma imagem de amostra) e documentar a limitação — imagem de amostra gerada via `DesenhoProceduralCircuito.geraImagem` (circuito Interlagos real) e conferida por amostragem de pixel; ver nota abaixo

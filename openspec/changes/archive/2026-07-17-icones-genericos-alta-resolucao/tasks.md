# Tasks — Ícones genéricos em alta resolução

## 1. Infraestrutura do pipeline

- [x] 1.1 Criar `utilitarios/gerar_icones.py` com esqueleto: canvas supersampled 8×, downscale Lanczos em dois estágios (supersample → master em `utilitarios/icones_hires/`, master → tamanho de jogo em `src/main/resources/png/`), exportação sempre RGBA, e tabela central de contratos de dimensão (142×142, 44×44, 35×35, 25×25, 90×90, 150×89)
- [x] 1.2 Capturar referências dos assets atuais antes de substituir (copiar os PNGs originais para pasta de trabalho temporária fora do repo, para comparação lado a lado de orientação/enquadramento)

## 2. Pneus genéricos

- [x] 2.1 Desenhar no script o pneu genérico parametrizado por cor de faixa: anel de borracha preto, faixa lateral colorida com marcações neutras (traços, sem texto/logotipo), aro cinza com raios; gerar `pneuMole.png` (amarelo, 142×142 — normalizado de 144×142), `pneuDuro.png` (branco, 142×142) e `pneuChuva.png` (azul, 142×142)
- [x] 2.2 Gerar `pneuMoleMenor.png`, `pneuDuroMenor.png`, `pneuChuvaMenor.png` (44×44 — normalizado de 44×43 no mole) por downscale Lanczos dos mesmos masters
- [x] 2.3 Verificar no Swing que os resizes de runtime (`geraResize(0.3)` ≈ 43 px no HUD e `geraResize(0.15)` ≈ 21 px nas minis do mapa) continuam legíveis com a nova arte

## 3. Clima e combustível

- [x] 3.1 Desenhar e gerar `sol.png` (disco amarelo com raios), `lua.png` (crescente claro), `nublado.png` (nuvem + sol parcial), `chuva.png` (nuvem + gotas azuis) — flat, conteúdo ocupando ~90% do quadro, 35×35
- [x] 3.2 Desenhar e gerar `fuel.png` — bomba de combustível flat exclusivamente em tons de cinza, 25×25

## 4. Safety car e faróis de largada

- [x] 4.1 Determinar a orientação (heading) exata do `sfcima.png` atual por inspeção comparativa com os sprites de carro de cima; desenhar o safety car flat visto de cima (corpo prata/branco, detalhes escuros) na mesma orientação e gerar em 90×90
- [x] 4.2 Desenhar o painel de faróis flat com geometria única (5 colunas de luzes vermelhas) e gerar os seis estados: `farois-apagados.png` (0 acesas), `farois1.png`–`farois4.png` (1–4 acesas), `farois.png` (5 acesas), todos 150×89

## 5. Limpeza e contrato

- [x] 5.1 Remover `src/main/resources/png/tyre.png` (asset órfão — sem referências em Java/JS/HTML)
- [x] 5.2 Criar `IconesPngDimensoesTest` em `src/test/java/br/f1mane/recursos/` afirmando, para cada PNG do conjunto, as dimensões do contrato e a presença de canal alfa (sem componentes Swing — só decodificação via `ImageIO`/`CarregadorRecursos`); afirmar também que `png/tyre.png` não existe mais no classpath
- [x] 5.3 Rodar `mvn test` e garantir suíte verde

## 6. Validação visual

- [x] 6.1 Validar no Swing: abrir `MainFrame` (menu local — seleção de pneus e clima) e uma corrida (HUD com pneus, fuel, clima/lua; largada com faróis; safety car em pista com orientação correta)
- [x] 6.2 Validar no web: subir `MainLauncher`, conferir `html5/testes/teste-png.html` (todos os endpoints `/letsRace/png/*` do conjunto), `controles.html` (pneus Menor) e a animação de largada/safety car no canvas
- [x] 6.3 Comparar screenshots antes/depois e iterar no script até a arte ficar condizente com os gráficos do jogo (tema pastel do redesign recente no web)

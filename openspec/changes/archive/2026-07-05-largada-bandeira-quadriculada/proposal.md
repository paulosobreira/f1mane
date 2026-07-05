## Why

O nó de largada (`No.LARGADA`) hoje só existe como marcador lógico (define onde a corrida começa e onde a volta é contabilizada); nem o editor de circuitos nem o jogo desenham qualquer indicação visual de onde fica a linha de largada/chegada, ao contrário de curvas (zebra) e box (vagas). Uma linha de largada em xadrez (bandeirada) é a referência visual padrão em jogos de corrida e ajuda tanto quem edita o circuito (a saber onde a largada está) quanto quem joga (a ver a linha de chegada a cada volta).

## What Changes

- Um novo método de desenho compartilhado (`DesenhoProceduralCircuito`) localiza o nó `No.LARGADA` no traçado, calcula a direção da pista naquele ponto (a partir do nó vizinho) e desenha uma faixa quadriculada (preto/branco, estilo bandeira de largada) perpendicular à pista, cobrindo a largura da pista naquele ponto.
- Esse desenho passa a fazer parte de `DesenhoProceduralCircuito.desenha()` — então aparece no editor de circuitos (modo sem imagem de fundo) e na imagem de fundo gerada em memória (`Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA`).
- O mesmo desenho passa a ser chamado também a partir de `PainelCircuito` (o painel de corrida do jogo solo/multiplayer), como uma camada desenhada por cima do fundo (seja a imagem estática `_mro.jpg`, seja a gerada em memória) — assim a linha de largada aparece em corrida independente de qual fonte de imagem de fundo o circuito usa.

## Capabilities

### New Capabilities
- `largada-bandeira-quadriculada`: Desenho da linha de largada como uma faixa quadriculada perpendicular à pista, no nó `No.LARGADA`, reaproveitado tanto pelo editor de circuitos/geração de imagem em memória (`DesenhoProceduralCircuito`) quanto pelo desenho ao vivo do jogo (`PainelCircuito`).

## Impact

- `br.f1mane.entidades.DesenhoProceduralCircuito` — novo método (ex. `desenhaLinhaDeLargada`), chamado dentro de `desenha()`.
- `br.f1mane.visao.PainelCircuito` — chama o mesmo método logo após desenhar o fundo (`desenhaBackGround`/`desenhaBackGroundComStrokes`), como uma camada sempre desenhada, independente da imagem de fundo usada.
- Nenhuma mudança em `Circuito`/`No` — a posição da largada já é identificável via `No.LARGADA` em `pistaFull`/`pistaKey`, sem precisar de novo dado persistido.

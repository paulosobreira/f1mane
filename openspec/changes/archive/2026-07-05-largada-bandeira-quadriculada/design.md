## Context

`DesenhoProceduralCircuito` (extraído do editor para ser reutilizável entre o editor de circuitos, modo sem imagem de fundo, e a geração em memória da imagem de fundo de corrida) desenha pista, zebra e box a partir de `circuito.getPistaKey()`/`getBoxKey()`, mas não trata `No.LARGADA` de forma especial em nenhum ponto — o nó de largada só é usado hoje para lógica de jogo (início de corrida, contagem de volta via `No.verificaRetaOuLargada()`). `PainelCircuito` (o painel de corrida do jogo solo/multiplayer) desenha o fundo via `desenhaBackGround(Graphics2D)`: quando há uma imagem de fundo (estática `_mro.jpg` ou gerada em memória), desenha essa imagem diretamente (`g2d.drawImage`); quando não há, cai em `desenhaBackGroundComStrokes(Graphics2D)`, que redesenha pista/zebra/box do zero (duplicando parte da lógica de `DesenhoProceduralCircuito`, um código mais antigo). Como a imagem de fundo padrão é estática (arquivo `.jpg` já existente, sem a linha de largada), só adicionar o desenho em `DesenhoProceduralCircuito` não seria suficiente pra aparecer em corrida — precisa também de uma camada desenhada ao vivo em `PainelCircuito`, sempre, independente da fonte da imagem de fundo.

## Goals / Non-Goals

**Goals:**
- Um único método de desenho da linha de largada, reaproveitado tanto por `DesenhoProceduralCircuito` (editor sem imagem de fundo, geração em memória) quanto por `PainelCircuito` (corrida ao vivo, com fundo estático ou gerado em memória).
- A linha aparece perpendicular à pista, na posição do nó `No.LARGADA`, cobrindo a largura da pista naquele ponto, em padrão quadriculado preto/branco.

**Non-Goals:**
- Não mudar a lógica de início de corrida/contagem de volta — só o desenho visual.
- Não adicionar nenhum campo novo a `Circuito`/`No` — a posição já é obtida buscando o nó com `tipo == No.LARGADA` em `pistaKey`/`pistaFull`.
- Não desenhar a linha de largada nos objetos de cenário (`objetosCenario`) nem torná-la editável/configurável (cor, tamanho) neste momento — é um elemento fixo do traçado, no mesmo espírito da zebra.

## Decisions

### Método compartilhado em `DesenhoProceduralCircuito`, chamado por dois lugares
Novo método público estático `desenhaLinhaDeLargada(Graphics2D g2d, Circuito circuito, double zoom)`:
1. Localiza o nó com `tipo == No.LARGADA` em `circuito.getPistaKey()` (a lista de nós-chave, mais barata que `pistaFull`) e o nó adjacent mais próximo em `pistaFull` pra estimar a direção local da pista.
2. Calcula o vetor perpendicular a essa direção, escalado pela largura da pista naquele ponto (mesma fórmula de `Carro.LARGURA * 1.5 * circuito.getMultiplicadorLarguraPista() * zoom` já usada em `desenhaPistaZebraEBox`).
3. Desenha uma faixa de quadrados alternados preto/branco (uma grade pequena, N colunas × 2 linhas) cobrindo essa largura, com uma espessura fixa ao longo da direção da pista (ex. do tamanho de um "quadrado" do xadrez).

Chamado de dois pontos:
- Dentro de `DesenhoProceduralCircuito.desenha()` (cobre editor sem imagem de fundo + geração em memória).
- Em `PainelCircuito.desenhaBackGround(Graphics2D)`, logo após o desenho do fundo (tanto no ramo de imagem quanto no ramo de fallback `desenhaBackGroundComStrokes`), como uma camada sempre desenhada — garante que a largada aparece em corrida mesmo quando o circuito usa a imagem `.jpg` estática antiga (sem a linha "assada" nela).
- Alternativa descartada — desenhar a linha só dentro da imagem de fundo gerada em memória (`DesenhoProceduralCircuito`), sem tocar `PainelCircuito`: não cumpriria "vale pro jogo também", já que a maioria dos circuitos hoje usa a imagem `.jpg` estática (a geração em memória é opt-in via `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA`, default `false`).

### Duplicar o desenho quando a imagem em memória já inclui a linha é aceito, não evitado
Quando `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` está ativa, a imagem de fundo gerada já inclui a linha de largada (via `DesenhoProceduralCircuito.desenha()`) e `PainelCircuito` desenha a mesma linha de novo por cima, no mesmo lugar — redundante, mas inofensivo (mesmos pixels, mesmo lugar, custo desprezível de desenhar alguns quadrados a mais por frame). Evitar essa redundância exigiria um parâmetro extra pra suprimir o desenho em `PainelCircuito` só nesse caso — complexidade desnecessária frente ao custo real.

### Padrão xadrez: grade fixa, sem novas propriedades configuráveis
Número de colunas, tamanho de cada quadrado e espessura da faixa ficam como constantes internas do método (não propriedades de `Circuito`/editor) — o pedido é ter a linha desenhada, não torná-la configurável; simplifica a primeira versão e evita mexer no formato persistido do circuito.

## Risks / Trade-offs

- [Nó de largada não encontrado em `pistaKey` (circuito malformado, embora o editor já exija um nó de largada pra salvar)] → Método retorna sem desenhar nada (`no-op` seguro) se não encontrar o nó, em vez de lançar exceção.
- [Direção da pista mal estimada em curvas muito fechadas bem no ponto de largada] → Aceitável pro objetivo (indicação visual aproximada da linha), não crítico pra jogabilidade.
- [Duplicação de desenho quando a imagem em memória já inclui a linha] → Ver decisão acima; aceito conscientemente.

## Open Questions

- Tamanho exato dos quadrados do xadrez e quantas colunas cabem por padrão ficam a critério de quem implementar/ajustar visualmente — não afeta a forma pública do método nem os cenários de spec.

## Verificação manual (limitação do ambiente)

Não há display interativo disponível no ambiente de implementação pra rodar o editor Swing ou o jogo ao vivo. A verificação visual foi feita renderizando um circuito real (Interlagos) via `DesenhoProceduralCircuito.geraImagem`, recortando a região ao redor do nó de largada e inspecionando o PNG resultante: a faixa quadriculada aparece perpendicular à pista, cruzando a largura do traçado com espessura fixa ao longo da direção da pista — como esperado. Fica como pendência de quem rodar o jogo/editor de fato conferir visualmente em tempo real (zoom, scroll, imagem de fundo estática `.jpg`).

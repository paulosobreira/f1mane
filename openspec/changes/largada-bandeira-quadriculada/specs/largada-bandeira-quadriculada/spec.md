## ADDED Requirements

### Requirement: Linha de largada desenhada em padrão quadriculado
O sistema SHALL desenhar, na posição do nó `No.LARGADA` do circuito, uma faixa em padrão quadriculado (preto/branco, estilo bandeira de largada) perpendicular à direção da pista naquele ponto, cobrindo a largura da pista.

#### Scenario: Linha de largada aparece perpendicular à pista
- **WHEN** o desenho do circuito é gerado (editor sem imagem de fundo, geração de imagem em memória, ou corrida ao vivo)
- **THEN** uma faixa quadriculada aparece na posição do nó de largada, perpendicular à direção local da pista, cobrindo a largura da pista

#### Scenario: Circuito sem nó de largada não lança exceção
- **WHEN** o desenho da linha de largada é chamado para um circuito cujo `pistaKey` não contém nenhum nó com `tipo == No.LARGADA`
- **THEN** o sistema não desenha nada e não lança exceção

### Requirement: Linha de largada aparece tanto no editor quanto em corrida
O desenho da linha de largada SHALL usar a mesma rotina em `DesenhoProceduralCircuito` (editor de circuitos sem imagem de fundo, e geração em memória da imagem de fundo) e em `PainelCircuito` (corrida ao vivo, solo ou multiplayer), independente de o circuito usar uma imagem de fundo estática (`.jpg`) ou gerada em memória.

#### Scenario: Linha de largada aparece no editor de circuitos
- **WHEN** o editor de circuitos desenha um circuito em modo sem imagem de fundo
- **THEN** a linha de largada quadriculada aparece desenhada, na mesma posição que apareceria em corrida

#### Scenario: Linha de largada aparece em corrida com imagem de fundo estática
- **WHEN** uma corrida solo ou multiplayer carrega um circuito cuja imagem de fundo é o arquivo `.jpg` estático (sem a linha de largada "assada" nela)
- **THEN** a linha de largada quadriculada ainda aparece desenhada, como uma camada por cima da imagem de fundo

#### Scenario: Linha de largada aparece em corrida com imagem gerada em memória
- **WHEN** `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` está ativa e uma corrida carrega o circuito
- **THEN** a linha de largada quadriculada aparece desenhada (já incluída na imagem gerada, e/ou redesenhada por cima pela mesma rotina)

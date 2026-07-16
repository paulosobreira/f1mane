# web-ui-tema-pastel

## Purpose

Definir e aplicar de forma consistente uma paleta de cores em tons pastéis no cliente web (`src/main/webapp/html5/`), centralizada em variáveis CSS, com contraste mínimo AA (WCAG) para texto e componentes interativos. `corrida.html` (canvas de renderização da corrida) é deliberadamente excluída, por não usar componentes estilizados por essas folhas e por ser sensível a performance durante a corrida.

## Requirements

### Requirement: Paleta de cores pastel via variáveis CSS
O cliente web SHALL definir sua paleta de cores em tons pastéis através de variáveis CSS (`:root`) em `flmane.css`, substituindo os valores hexadecimais fixos hoje espalhados pelo arquivo.

#### Scenario: Carregamento de qualquer página do cliente web
- **WHEN** qualquer página em `src/main/webapp/html5/*.html` é carregada
- **THEN** as cores de fundo, texto e bordas dos componentes são resolvidas a partir das variáveis CSS do tema pastel definidas em `flmane.css`

### Requirement: Contraste mínimo para legibilidade
Todo texto e elemento interativo exibido sobre um fundo em tom pastel SHALL manter contraste suficiente para leitura, usando como referência a razão mínima AA do WCAG (4.5:1 para texto normal, 3:1 para texto grande/ícones).

#### Scenario: Texto sobre fundo pastel
- **WHEN** um componente exibe texto sobre um fundo em tom pastel do novo tema
- **THEN** a razão de contraste entre a cor do texto e a cor de fundo atende no mínimo 4.5:1

#### Scenario: Borda de componente selecionável
- **WHEN** um componente selecionável (ex.: item de piloto, item de carousel) é exibido em estado normal ou selecionado
- **THEN** a borda ou destaque do componente é visualmente distinguível do fundo pastel ao redor, com contraste mínimo de 3:1

### Requirement: Aplicação consistente das folhas de estilo do tema
Toda página do cliente web que usa componentes Bootstrap ou botões estilizados SHALL carregar as mesmas folhas de estilo do tema (`flmane.css` e `mdb-btns.css`). `corrida.html` é uma exceção deliberada: é a tela de renderização da corrida em canvas, não usa nenhum componente Bootstrap/`.btn`, e não deve ser alterada por esta mudança.

#### Scenario: Página de menu que hoje não carrega mdb-btns.css
- **WHEN** `jogar.html`, `equipe.html` ou `controles.html` é carregada
- **THEN** o tema pastel definido em `flmane.css` e `mdb-btns.css` é aplicado da mesma forma que nas páginas que já os carregavam

#### Scenario: corrida.html permanece inalterada
- **WHEN** `corrida.html` é carregada
- **THEN** ela continua carregando apenas `flmane.css` (sem `mdb-btns.css` nem `bootstrap.min.css`), já que não usa nenhum componente estilizado por essas folhas e é uma página sensível a performance durante a corrida

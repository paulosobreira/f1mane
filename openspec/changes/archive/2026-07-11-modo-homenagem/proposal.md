## Why

O jogo hoje usa os nomes reais de equipes, carros e pilotos históricos de F1 diretamente nos arquivos de temporada (`carros.properties`, `pilotos.properties`), sem nenhuma camada de indireção. Isso não deixa espaço pra rodar o jogo num modo alternativo onde nomes e imagens de referência reais não sejam usados diretamente — hoje a única alternativa seria editar os arquivos originais na mão, perdendo os dados reais. "Modo homenagem" introduz uma segunda identidade (nome + comportamento de imagem) por carro e por piloto, comutável por um único flag global, sem exigir a edição destrutiva dos dados originais.

## What Changes

- `carros.properties` de cada temporada ganha uma nova coluna `nomeHomenagem`, inicialmente seedada com o nome canônico da equipe derivado do nome de arquivo da imagem existente (removendo prefixo/sufixo de versão/modelo e a extensão `.png` — ex.: `tn_1972voi-lotus72d.png` → `lotus`), editável depois pelo usuário.
- `pilotos.properties` de cada temporada ganha uma nova coluna `nomeHomenagem` (posicionada após `habilidade`), inicialmente seedada com um sobrenome fictício de mesma nacionalidade do piloto real e mesma inicial de primeiro nome (ex.: Ayrton Senna → `A. Silva`; Fernando Alonso → `F. Almodóvar`), editável depois.
- Novo flag global `Global.MODO_HOMENAGEM` (default `true`), que **substitui** `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` (retirado): quando ativo, o jogo exibe os nomes-homenagem em vez dos nomes reais, gera a imagem do circuito proceduralmente em memória (em vez de carregar o arquivo de fundo real) e, pra carros/pilotos, prioriza uma representação genérica (modelo colorido com `cor1`/`cor2` do carro) sobre o sprite sheet, caindo pro arquivo individual só como último recurso (e de volta pro modelo colorido se nem isso existir); quando inativo, tudo é real (nomes reais, imagem de fundo real do circuito, sprite/arquivo real do carro).
- `EditorCoresCarros` é renomeado e estendido pra um editor combinado de carros e pilotos: navegação por temporada existente é preservada, e um seletor no topo permite alternar entre editar carros (cores + nome-homenagem, como hoje + o campo novo) ou pilotos (nome-homenagem + habilidade daquela temporada, com imagem de carro/capacete exibida somente leitura).
- Preenchimento de `nomeHomenagem` (carros e pilotos) pra todas as ~30+ temporadas já nesta mudança, não incremental.

## Capabilities

### New Capabilities
- `nome-homenagem-carros`: coluna `nomeHomenagem` em `carros.properties`, regra de derivação do nome canônico a partir do arquivo de imagem, e edição desse valor.
- `nome-homenagem-pilotos`: coluna `nomeHomenagem` em `pilotos.properties`, regra de derivação de um sobrenome de mesma nacionalidade/inicial, e edição desse valor.
- `modo-homenagem-toggle`: o flag `Global.MODO_HOMENAGEM`, como ele resolve nome/imagem de piloto e carro em tempo de execução, e como substitui `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` pra decidir a geração de imagem do circuito.

### Modified Capabilities
- `dev-editor-tools`: `EditorCoresCarros` passa a editar também pilotos (nome-homenagem + habilidade), com um seletor de modo carros/pilotos, mantendo a navegação por temporada já documentada.
- `objetos-cenario-circuito`: os requisitos que hoje dependem de `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` passam a depender de `Global.MODO_HOMENAGEM`.

## Impact

- Todo `src/main/resources/properties/t*/carros.properties` e `pilotos.properties` (~30+ temporadas) ganham uma coluna nova, populada nesta mudança — trabalho de conteúdo (derivação/preenchimento do nome-homenagem de cada carro/piloto), não só de código.
- `br/nnpe/Global.java` — novo flag `MODO_HOMENAGEM`, remoção de `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` **e** de `FORCE_MODELO_V2` (achado durante a implementação: já existia e já fazia exatamente a cadeia modelo-colorido→sprite→arquivo pedida — ver design.md decisão 5).
- `br/f1mane/entidades/DesenhoProceduralCircuito.java`, `br/f1mane/recursos/CarregadorRecursos.java`, `br/f1mane/servidor/rest/LetsRace.java` — os 3 pontos que hoje checam `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` passam a checar `MODO_HOMENAGEM`.
- Camada de resolução de nome/imagem em tempo de execução (`Piloto`, `Carro`, `CarregadorRecursos`) — nome resolvido uma vez no carregamento (`MODO_HOMENAGEM ? nomeHomenagem : nomeOriginal`), substituindo as duas chamadas a `Util.substVogais` usadas hoje pra piloto/carro (a função em si permanece, ainda usada pra nomes de circuito, fora do escopo desta mudança).
- Resolução de imagem de carro/piloto (modelo colorido genérico → sprite → arquivo → modelo colorido de novo) — mecanismo já existente (`CarregadorRecursos.pintarModeloV2`, 4 call sites que checavam `FORCE_MODELO_V2`), só reamarrado pra checar `MODO_HOMENAGEM` no lugar.
- `src/main/java/br/f1mane/editor/EditorCoresCarros.java` — renomeado e estendido (seletor de modo, novos campos de piloto).
- `Carro` ganha um campo `nomeOriginal` (hoje só `Piloto` tem).
- Nenhuma migração de dados destrutiva: os campos reais existentes (chaves de `carros.properties`/`pilotos.properties`) permanecem intactos; só uma coluna nova é adicionada.

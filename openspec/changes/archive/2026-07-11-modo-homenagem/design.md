## Context

Hoje, ao carregar uma temporada, `CarregadorRecursos` transforma o nome real de cada piloto/carro (a chave em `pilotos.properties`/`carros.properties`, ex.: `"Ayrton Senna"`, `"Lotus-72D"`) através de `Util.substVogais(name)` — uma função de substituição de vogais que ofusca o nome real, aplicada incondicionalmente:

```java
piloto.setNomeOriginal(name);                       // nome real, sem alteração
piloto.setNome(Util.substVogais(name));              // nome exibido: sempre ofuscado
...
carro.setNome(Util.substVogais(name));               // Carro não guarda nome real algum
```

`Carro` não tem um campo `nomeOriginal` equivalente ao de `Piloto` — o nome real da chave é descartado depois do `substVogais` no carregamento.

`Util.substVogais` **também** é usado pra nomes de circuito (`PainelCircuito`, `PainelMenuLocal`, `ControleJogosServer`, `ControleCampeonatoServidor`, `CarregadorRecursos` linha 1126) — ou seja, é uma utilidade compartilhada, não exclusiva de carros/pilotos.

`carros.properties` tem o formato `Chave=potencia,cor1-r,cor1-g,cor1-b,imagem[;imagem2;...],cor2-r,cor2-g,cor2-b,aero,freios` (10 campos posicionais, os 2 últimos opcionais — código já trata `values.length > 8/9`). `imagem` pode ter múltiplos arquivos separados por `;` (sorteio de variante de livery).

`pilotos.properties` tem o formato `Chave=Carro,Habilidade` (2 campos).

`EditorCoresCarros` (`br/f1mane/editor/EditorCoresCarros.java`, já documentado em `dev-editor-tools`) é uma ferramenta Swing standalone que lê/edita `carros.properties` de uma temporada por vez, com navegação Anterior/Próxima, mostrando um card por carro com preview (lado/cima/capacete via `SpriteSheet`) e color pickers.

`SpriteSheet.isDisponivel(temporada)` indica se a temporada tem sprite sheet (`sprites/tANO.png`); quando disponível, o jogo usa `getCarroLado/getCarroCima/getCapacete` (imagens genéricas, por índice); quando não, cai pros arquivos individuais em `carros/`/`capacetes/` (referenciados pela coluna `imagem`).

`Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` (default `true` no código atual) é checado em 3 lugares: `DesenhoProceduralCircuito` (decide se desenha o circuito proceduralmente a partir dos objetos do traçado), `CarregadorRecursos` (decide se gera a imagem de fundo em memória em vez de carregar um arquivo `.jpg` real do circuito) e `LetsRace` (endpoint REST `/circuitoJpg/{nmCircuito}`, mesma decisão pro cliente multiplayer). O usuário pediu explicitamente pra **substituir** esse flag pelo `MODO_HOMENAGEM` — ou seja, esses 3 pontos passam a checar `MODO_HOMENAGEM` em vez de `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA`, que é removido.

## Goals / Non-Goals

**Goals:**
- Adicionar uma segunda identidade (nome) por carro e por piloto, guardada como dado (`nomeHomenagem`), sem apagar o nome real.
- Um único flag global (`Global.MODO_HOMENAGEM`, default `true`) decide, no carregamento da temporada, se `Piloto.nome`/`Carro.nome` vem do nome real ou do nome-homenagem, e substitui `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` pra decidir a geração de imagem do circuito.
- Popular `nomeHomenagem` com um valor derivado (canônico, pra carros; pseudônimo de mesma nacionalidade/inicial, pra pilotos) em todas as ~30+ temporadas, nesta mesma mudança, editável depois via editor.
- Estender `EditorCoresCarros` (renomeado) pra também editar pilotos (nome-homenagem + habilidade da temporada), com um seletor de modo carros/pilotos no topo, preservando a navegação por temporada já existente.
- Em modo homenagem, carros/pilotos preferem uma representação genérica (modelo colorido com `cor1`/`cor2`) sobre imagens específicas (sprite ou arquivo).

**Non-Goals:**
- Não mexe em `Util.substVogais` usado pra nomes de **circuito** — esse uso é uma feature separada, fora do que foi pedido (o pedido do usuário foi especificamente sobre nomes de equipe/piloto). `Util.substVogais` como utilitário **não é removido**, só os dois pontos de uso em `CarregadorRecursos` referentes a piloto/carro deixam de chamá-lo.
- Não migra/recalcula automaticamente `nomeHomenagem` depois de editado manualmente — uma vez que o usuário edita no editor, o valor gravado é definitivo (mesmo padrão do resto do arquivo).
- Não adiciona um terceiro estado (ex.: "meio homenagem") — o flag é binário.
- Não altera fisicamente quais arquivos de imagem/circuito existem — a lógica de qual imagem é usada em cada modo reaproveita mecanismos já existentes (`SpriteSheet`, `DesenhoProceduralCircuito`, coluna `imagem`), sem introduzir um pipeline de assets novo — só um renderer novo e simples pro "modelo colorido genérico" (ver decisão 5).

## Decisions

### 1. `nomeHomenagem` como coluna nova, no fim da lista posicional

Em `carros.properties`, adicionar `nomeHomenagem` como o 11º campo posicional (depois de `freios`), com `values.length > 10` decidindo se está presente — mesmo padrão já usado pra `aero`/`freios` opcionais. Em `pilotos.properties`, adicionar como o 3º campo (depois de `Habilidade`) — já é o formato "depois da habilidade" pedido.

**Alternativa considerada:** um arquivo `homenagem.properties` separado, mapeando chave → nome-homenagem. Rejeitada — duas fontes de verdade pra manter sincronizadas (e o pedido original foi explicitamente "criar uma nova coluna", não um arquivo à parte).

### 2. Derivação inicial do nome-homenagem de carro: canonicalização do nome de imagem

Pra cada carro, tomar o primeiro nome de arquivo da coluna `imagem` (antes do primeiro `;`), remover a extensão, remover prefixos de convenção (ex.: `tn_`, ano de 4 dígitos, `voi-`) e o sufixo de modelo/versão (ex.: `72d`, `-R27`, `312b3`), normalizando pro nome da equipe em minúsculas. Exemplos do pedido: `tn_1972voi-lotus72d.png` → `lotus`; `Renault-R27.png` → `renault`.

Como os prefixos/sufixos variam bastante entre eras (temporadas mais antigas usam `tn_ANOvoi-...`, temporadas mais novas usam `Equipe-Modelo.png` puro), essa canonicalização **não é 100% mecanizável por uma única regex** — é tratada como uma passada de conteúdo, temporada por temporada, com heurísticas (strip de prefixo numérico/`voi-`/`tn_`, strip de sufixo alfanumérico de modelo) validadas manualmente por amostragem, não uma função determinística nova em produção. Nenhum código novo de "canonicalização automática em tempo de execução" é necessário — o valor final já fica salvo na properties.

**Alternativa considerada:** calcular o nome canônico em tempo de execução a partir do nome de imagem, toda vez que o carro é carregado (sem persistir). Rejeitada — o pedido explícito foi ter uma coluna editável, e o cálculo automático não seria estável o bastante (várias exceções/casos especiais por temporada) pra confiar em tempo de execução; melhor persistir o valor já revisado.

### 3. Derivação inicial do nome-homenagem de piloto: pseudônimo por nacionalidade + inicial

Pra cada piloto, usar conhecimento histórico de F1 (nacionalidade real do piloto) pra escolher um sobrenome comum daquela mesma nacionalidade, mantendo a inicial do primeiro nome como aparece hoje formatado nas properties (ex.: piloto real brasileiro cuja chave é `A.Senna` → `nomeHomenagem = A.Silva`; piloto real espanhol `F.Alonso` → `nomeHomenagem = F.Almodovar`). Isso é, assim como a derivação de carro, uma tarefa de conteúdo (uma linha por piloto, em cada temporada), não uma função de runtime — evita depender de uma lista de nacionalidades embutida no código/dados do jogo, que não existe hoje.

**Alternativa considerada:** gerar pseudônimos aleatórios sem relação com a nacionalidade real. Rejeitada — contraria explicitamente o pedido ("busque a nacionalidade... altere pra um sobrenome de mesma origem").

### 4. `Global.MODO_HOMENAGEM` resolvido uma vez, no carregamento

Seguindo o mesmo padrão de `Util.substVogais` hoje (aplicado uma vez em `CarregadorRecursos.carregarListaPilotos`/`carregarListaCarrosArquivo`, não em cada ponto de renderização), `Piloto.nome`/`Carro.nome` passam a ser escolhidos ali: `MODO_HOMENAGEM ? nomeHomenagem : nomeOriginal`. Isso **substitui** as chamadas a `Util.substVogais(name)` nesses dois pontos — não desliga o utilitário em si (que continua vigente pra nomes de circuito).

`Carro` ganha um campo `nomeOriginal` (que hoje não existe), espelhando `Piloto.nomeOriginal`, pra guardar a chave real antes da escolha condicional.

**Alternativa considerada:** checar `Global.MODO_HOMENAGEM` em cada local que hoje lê `piloto.getNome()`/`carro.getNome()` (renderização, HUD, etc.), sem resolver no carregamento. Rejeitada — dezenas de pontos de leitura espalhados (mesmo padrão que já levou a preferir resolver `substVogais` uma vez só, no carregamento); resolver uma vez é consistente com o design existente e evita bugs de um local mostrar o nome errado.

### 5. Efeito de `MODO_HOMENAGEM` sobre imagens: circuito procedural + cadeia de fallback pra carro/piloto

Duas partes, confirmadas com o usuário:

**Circuito:** `MODO_HOMENAGEM` ativo ⇒ gera a imagem do circuito proceduralmente em memória (mecanismo já existente de `DesenhoProceduralCircuito`, hoje gatilhado por `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA`) em vez de carregar o arquivo de fundo real (`.jpg`) do autódromo. `MODO_HOMENAGEM` inativo ⇒ usa a imagem de fundo real, como hoje quando `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` é `false`.

**Carro/piloto — correção após investigação de código:** o "modelo colorido genérico" **já existe** e já é o comportamento **default atual do jogo inteiro**, não algo a construir do zero. `CarregadorRecursos.pintarModeloV2(assetPath, cor1, cor2, w, h)` pinta um template (`png/carro-lado-v2.png`, `png/carro-cima-v2.png`, `png/capacete-v2.png`) recolorindo matiz/saturação por HSB preservando o shading — usado por `desenhaCarroLado`/`desenhaCarroCima`/`desenhaCapacete`. Um flag pré-existente, `Global.FORCE_MODELO_V2` (default `true`), já força esse caminho em `obterCapacete`, `obterCarroLado`, `obterCarroCimaSemAreofolio` e `obterCarroCima` — quando `false`, cada um desses métodos já faz exatamente a cadeia sprite → arquivo individual → modelo colorido (fallback final) que o usuário descreveu. Ou seja, a "cadeia de fallback" pedida já estava implementada; só a amarração ao conceito certo (homenagem vs. real) estava faltando, presa a um flag de nome desconectado (`FORCE_MODELO_V2`, sobre migração de arte anterior, não sobre homenagem).

Implementação: os 4 `if (Global.FORCE_MODELO_V2)` em `CarregadorRecursos` passam a ser `if (Global.MODO_HOMENAGEM)`, e `Global.FORCE_MODELO_V2` é removido (sem outros usos no código). Como `MODO_HOMENAGEM` também nasce com default `true` (decisão 4), o comportamento visual observável não muda por padrão — a diferença é que agora "modo real" (`MODO_HOMENAGEM=false`) volta a ativar de verdade o caminho sprite/arquivo, que hoje é código morto (nunca alcançado, já que `FORCE_MODELO_V2=true` sempre retorna antes).

**Alternativa considerada (descartada após achar `pintarModeloV2`/`FORCE_MODELO_V2`):** implementar um renderer novo do zero pra silhueta colorida. Descartada — reinventaria algo que já existe, testado e em produção; a mudança real necessária é só trocar qual flag os 4 call sites checam.

### 6. `EditorCoresCarros` vira editor combinado, com seletor de modo

Adicionar um seletor (ex.: toggle/combobox) no topo da janela: "Carros" (comportamento atual + campo `nomeHomenagem` editável por card) ou "Pilotos" (nova visão: um card por piloto daquela temporada, mostrando imagem do carro/capacete daquele piloto **somente leitura**, com campos editáveis pra `nomeHomenagem` e `habilidade`). A navegação Anterior/Próxima de temporada existente é compartilhada pelos dois modos. A classe `EditorCoresCarros` é renomeada pra `EditorCarrosPilotos` (nome escolhido nesta mudança), com a referência em `dev-editor-tools` atualizada.

**Alternativa considerada:** dois editores Swing separados (um de carros, outro de pilotos). Rejeitada — o próprio usuário pediu explicitamente pra reaproveitar um editor só, com seletor de modo, em vez de duplicar janela/navegação de temporada.

### 7. Propagar edição de `nomeHomenagem` pra todas as temporadas da mesma identidade, via checkbox

Um mesmo carro (equipe) ou piloto aparece em várias temporadas com uma chave de properties diferente por versão (carro: `McLaren-MP4/1`, `McLaren-MP4/4`, ... — o modelo muda, mas o nome de imagem canonicaliza sempre pro mesmo nome, ex.: `mclaren`; piloto: a chave real geralmente se repete idêntica entre as temporadas em que correu). O usuário pediu que, ao editar `nomeHomenagem`, apareça um checkbox pra propagar a mudança pra toda ocorrência dessa mesma identidade em qualquer temporada, em vez de só a linha da temporada aberta no momento.

Implementação (refinada durante a implementação — ver alternativa revertida abaixo): ao salvar com o checkbox marcado, o editor varre todos os `carros.properties`/`pilotos.properties` de `src/main/resources/properties/t*/` (não só a temporada carregada) procurando linhas cuja identidade bate com a do item editado:
- **Carro:** o valor de `nomeHomenagem` que a linha editada tinha **antes** desta edição (lido do arquivo, não recalculado) igual ao `nomeHomenagem` atual de outras linhas.
- **Piloto:** chave real (a chave da properties) igual à do piloto editado.

E reescreve só o campo `nomeHomenagem` de cada linha encontrada, preservando os demais campos e a formatação do arquivo — mesmo padrão de escrita pontual já usado por "Salvar" hoje, só que iterando sobre várias temporadas em vez de uma.

**Alternativa considerada:** propagar automaticamente sempre (sem checkbox, sempre todas as temporadas). Rejeitada — o usuário pediu explicitamente a opção de escolher (só a temporada atual OU todas), não um comportamento fixo.

**Alternativa considerada e depois revertida:** usar um nome canônico re-derivado do nome de imagem (recalculado em tempo real no editor, com uma cópia da mesma lógica/dicionário usada na tarefa de conteúdo) como critério de agrupamento pra carro, em vez do valor atual de `nomeHomenagem`. Descartada durante a implementação: como a tarefa de conteúdo (grupo 5) já preencheu `nomeHomenagem` de forma consistente com o nome canônico em todas as 26 temporadas de uma vez, os dois critérios coincidem hoje — mas usar o valor atual de `nomeHomenagem` (lido do arquivo antes da edição) é estritamente melhor: (1) não duplica a lógica/dicionário de derivação no editor; (2) respeita edições manuais anteriores feitas sem o checkbox — se o usuário já tinha customizado uma temporada isoladamente, uma propagação futura a partir de *outra* temporada não sobrescreve silenciosamente essa customização, porque o valor dela já divergiu do valor-âncora original.

### 8. Simplificação de UI: checkbox + salvar lado a lado, remoção do diálogo "Editar Cores", ícone no lugar do texto, nome final da classe

Pitaco de design do usuário, aplicado depois do editor já funcional:

- O checkbox "Aplicar a todas as temporadas" e o botão de salvar passam a ficar na mesma linha (lado a lado), em vez de linhas separadas — em ambos os modos (Carros e Pilotos).
- O botão "Editar Cores" (que abria um diálogo modal com spinners RGB) é removido — os botões inline "Cor 1"/"Cor 2" (que já abrem `JColorChooser` diretamente no card) cobrem essa necessidade sem precisar de um diálogo redundante. O diálogo, `criarSpinners`, e a interface auxiliar `ChangeListener<T>` (usada só por ele) foram removidos por completo.
- O texto "Salvar" no botão vira um ícone (💾), com tooltip "Salvar" — mesmo padrão de símbolo Unicode já usado no arquivo (◀/▶ pra navegação, ⇄ pra trocar cores), em vez de introduzir um asset de imagem novo.
- A classe é renomeada de `EditorHomenagemCarrosPilotos` (nome escolhido na decisão 6) pra `EditorCarrosPilotos` — mais curto, sem perder clareza (o editor já deixa claro que edita nome-homenagem via os campos, não precisa repetir "Homenagem" no nome da classe).

Com a remoção do diálogo, a altura do card de carro caiu de 258 pra 226px (uma linha inteira a menos).

### 9. Bugs encontrados pelo usuário testando o editor + pedidos de sprite original

**Bug raiz — editor lia de classpath, salvava em filesystem:** `EditorCarrosPilotos.carregarCarros/carregarPilotos` chamavam `CarregadorRecursos.getCarregadorRecursos(...).carregarListaCarros/carregarListaPilotos`, que resolvem `pilotos.properties`/`carros.properties` via `Class.getResourceAsStream(...)` — ou seja, do **classpath** (`target/classes` quando rodando `java -cp target/classes ...`). Já `salvarCarroNoArquivo`/`salvarPilotoNoArquivo` sempre escreveram direto em `src/main/resources/properties/...` (filesystem, código-fonte). Essas duas localizações só ficam sincronizadas depois de um `mvn compile`/rebuild — então editar, salvar, e reabrir o editor **sem** rebuildar mostrava o valor antigo (o nome canônico originalmente semeado), não o que acabou de ser editado. Esse é o mesmo motivo, à parte, de o **jogo em si** mostrar o nome canônico em vez do homenageado recém-editado: o jogo (corretamente) só lê do classpath — a única forma de ele enxergar uma edição feita no editor é rebuildar. Isso não é um bug do jogo (é o comportamento esperado de recursos via classpath, o mesmo já valia pra edição de cor1/cor2 no editor original), mas o editor em si não tinha motivo pra sofrer desse problema, já que só edita arquivos-fonte.

Correção: `EditorCarrosPilotos` ganhou `lerCarrosDoArquivo`/`lerPilotosDoArquivo`, que leem `src/main/resources/properties/<temporada>/{carros,pilotos}.properties` diretamente do filesystem (mesmo padrão de leitura de `salvarCarroNoArquivo`), com pareamento piloto↔carro feito localmente (mesma lógica de `chaveCarro`/`nomeOriginal` de `CarregadorRecursos.ligarPilotosCarros`). O editor não depende mais de `CarregadorRecursos.getCarregadorRecursos/carregarListaCarros/carregarListaPilotos/ligarPilotosCarros` — só de `pintarModeloV2`/`invalidarCacheModeloV2` (utilitários de desenho, sem estado de carregamento) e agora `SpriteSheet`/`indiceTime`/`indicePiloto`/`extrairTime` (ver abaixo). Isso garante que salvar e reabrir (mesmo sem rebuild) sempre reflete o que está gravado.

**Ícone do botão salvar não aparecia:** emoji (`💾`) como texto de `JButton` depende da fonte do sistema ter esse glifo — não é garantido em toda instalação Windows/JRE. Trocado por um ícone desenhado em código (`IconeDisquete`, implementa `javax.swing.Icon`, desenha um disquete simples via `Graphics2D`), igual em espírito aos outros elementos gráficos do editor (que já desenham tudo via `Graphics2D`/`pintarModeloV2`), sem depender de cobertura de fonte.

**Pedido: modo Pilotos mostra só o capacete, e do sprite (não do modelo colorido):** trocado `PreviewCanvas` (3 partes pintadas) por um único `ImagemCanvas` (64x64) mostrando `SpriteSheet.getCapacete(temporada, indicePiloto(...))` — a arte real do piloto, não a silhueta genérica colorida — com fallback pro modelo colorido só se a temporada não tiver sprite sheet ou o piloto não for encontrado nele. Isso é deliberadamente **independente de `Global.MODO_HOMENAGEM`**: o editor é uma ferramenta de autoria de conteúdo, quer mostrar a arte real de referência enquanto edita, não simular o que o jogo mostraria em modo homenagem.

**Pedido: modo Carros mostra o sprite original ao lado do pintado:** adicionada uma segunda linha no card, abaixo do preview pintado, com `SpriteSheet.getCarroLado`/`getCarroCima` da equipe (via `indiceTime`/`extrairTime`, agora `public` em `CarregadorRecursos` — eram `private`, únicas mudanças de visibilidade necessárias) — `capacete` não entra aqui porque é indexado por piloto, não por equipe, então não faz sentido pra esse card. Se a temporada não tiver sprite sheet, mostra "(sem sprite sheet pra esta temporada)" em vez do canvas. A rolagem vertical pedida já existia (`JScrollPane` com `VERTICAL_SCROLLBAR_AS_NEEDED`) — cresce automaticamente com a altura maior do card (226→284px), sem precisar de mudança estrutural.

### 10. Segunda passada de otimização de espaço, a pedido do usuário

Depois de ver o resultado da decisão 9, o usuário pediu pra apertar ainda mais o layout:

- **Sprite original sem rótulo, ocupando a linha toda:** removido o texto "Sprite original:"; `lado`/`cima` passam a preencher a largura útil do card (`CARD_W - 12`), com `cima` fixo em `spriteRowH` (56px, quadrado) e `lado` ocupando o resto — em vez de miniaturas de tamanho fixo (100/48px) com rótulo do lado. `ImagemCanvas` já escala mantendo proporção (decisão 9), então só mudar os `bounds` bastou.
- **Sliders de Pot/Aero/Freios viram spinners, numa linha só:** três `JSpinner` (100-999, mesmo range dos sliders antigos) lado a lado numa única linha, em vez de sliders empilhados um por linha — `adicionarSlider` foi removido e substituído por `adicionarSpinner` (rótulo curto de 2-3 letras + spinner compacto). `Freios` ganhou controle na UI pela primeira vez nesta mudança (antes só carregava/salvava, sem edição possível).
- **Modo Pilotos, cabeçalho numa linha só:** capacete (reduzido de 64x64 pra 44x44) + nome do piloto (linha de cima) + nome do carro (linha de baixo) lado a lado, em vez de capacete numa linha e carro noutra.
- **Modo Pilotos, Homenagem + Habilidade na mesma linha:** campo de texto (nome-homenagem) e spinner (habilidade) lado a lado, com rótulo de habilidade abreviado ("Hab:") pra caber.

Resultado: `CARD_H_CARRO` 284→266px, `CARD_H_PILOTO` 190→124px (quase pela metade) — cabem bem mais cards visíveis por rolagem.

### 11. Bug: propagação de `nomeHomenagem` de carro parava de funcionar depois do 1º save sem checkbox marcado

**Sintoma relatado pelo usuário:** editou `Mercedes` → `Meredith` em t2026, marcou "Aplicar a todas as temporadas" e salvou — mas nenhuma outra temporada foi atualizada (confirmado via grep: só t2026 tinha "Meredith", as outras 12 temporadas com Mercedes continuavam com o valor semeado "mercedes"). O mesmo já tinha acontecido, sem o usuário ainda ter percebido, com `Williams` → `Wilton`.

**Causa raiz:** `propagarNomeHomenagemCarro` usa "o valor de `nomeHomenagem` que a linha editada tinha antes desta edição" como chave de correspondência pra decidir quais outras temporadas atualizar (decisão 7) — mas esse valor "antes da edição" era relido do **disco**, na própria temporada que acabou de ser salva, bem antes da chamada de propagação (`lerNomeHomenagemAtual`). Isso funciona na primeira vez que a linha é editada. Só que se o usuário salva uma vez **sem** marcar o checkbox (ou clica só "Salvar Todos", que salva a temporada mas não propaga sozinho), o valor em disco daquela temporada já vira "Meredith" — e numa tentativa seguinte de salvar com o checkbox marcado, `lerNomeHomenagemAtual` já vai ler "Meredith" (não mais "mercedes") como o "valor antes da edição", que não bate com nenhuma outra temporada (ainda em "mercedes") — a propagação roda, não encontra nenhuma linha correspondente, e termina silenciosamente sem alterar nada e sem erro. Uma vez que isso acontece, **nenhum save seguinte consegue mais se recuperar sozinho**, porque a referência do valor compartilhado original já foi perdida — reler do disco não ajuda, ela só reflete o próprio valor já divergente.

Confirmado isolando `propagarNomeHomenagemCarro` (via reflection, chamando o método real da classe compilada) com os argumentos exatos que o editor teria usado: com `nomeAntesDaEdicao="mercedes"` (o valor histórico correto, compartilhado por todas as 13 temporadas com Mercedes antes de qualquer edição), a propagação encontra e atualiza corretamente as 12 outras temporadas; com `nomeAntesDaEdicao="Meredith"` (o valor já lido de volta do disco após o 1º save), a mesma chamada não encontra nenhuma correspondência — reproduzindo exatamente o bug relatado.

**Correção:** `CarroEntry` ganha um campo `nomeHomenagemCarregado`, capturado uma única vez na construção do entry (= `carro.getNomeHomenagem()`, o valor lido do disco quando a temporada foi carregada nesta sessão do editor) — em vez de reler do disco a cada save. Esse campo só é atualizado (pro novo valor) depois de uma propagação **bem-sucedida**; um save individual sem propagar não o toca. Assim, mesmo que o usuário salve várias vezes sem marcar o checkbox, a âncora de propagação continua apontando pro último valor que de fato era compartilhado por todas as temporadas — não pro valor já sobrescrito só localmente. `lerNomeHomenagemAtual` (agora sem chamadores) foi removido. O diálogo de "salvo" também passou a indicar explicitamente quando a propagação ocorreu ("Aplicado a todas as temporadas."), pra eliminar a ambiguidade que motivou o relato original — antes não havia nenhuma confirmação visível de que a propagação tinha (ou não) rodado.

Os dados reais do usuário já presos nesse estado (`Mercedes`→`Meredith` e `Williams`→`Wilton`, ambos só em t2026) foram propagados manualmente pras 24 outras temporadas depois da correção, usando o mesmo método já corrigido com o valor-âncora histórico correto (`mercedes`/`williams`, confirmado via inspeção de todas as temporadas antes de aplicar).

**Verificado, não é o problema:** a releitura de temporada (`carregarTemporada`/`navegarTemporada`) já sempre chama `lerCarrosDoArquivo`/`lerPilotosDoArquivo` do zero a cada troca de temporada ou de modo — não existe nenhum cache intermediário de `CarroEntry`/`PilotoEntry` entre navegações. A segunda preocupação do usuário ("verifica se quando eu mudo de temporada recarrega o que pode ter sido alterado por outra temporada") já era satisfeita antes desta correção; não precisou de mudança.

## Risks / Trade-offs

- [Canonicalização do nome de carro e escolha de pseudônimo de piloto são tarefas de conteúdo manuais/assistidas por IA, cobrindo ~30+ temporadas — risco de inconsistência ou erro humano/IA em casos históricos obscuros] → Mitigação: cobrir todas as temporadas já nesta mudança (decisão do usuário), com o editor (`EditorCoresCarros` estendido/renomeado) disponível pra correção posterior sem precisar editar o `.properties` na mão.
- [Adicionar campo posicional novo em `carros.properties` (11º campo) quebra parsers que assumem `values.length` fixo] → Mitigação: seguir o padrão já existente de checar `values.length > N` antes de ler campos opcionais (usado hoje pra aero/freios); qualquer outro leitor de `carros.properties` fora de `CarregadorRecursos`/`EditorCoresCarros` precisa ser auditado nos tasks.
- [Retirar `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` afeta 3 pontos já documentados em specs existentes (`objetos-cenario-circuito`, e referenciado em `largada-bandeira-quadriculada`/`zona-frenagem-visualizacao-editor`) — risco de quebrar esses requisitos se a migração pra `MODO_HOMENAGEM` não preservar o comportamento equivalente] → Mitigação: `objetos-cenario-circuito` entra como capability modificada nesta mudança (delta spec); os outros dois specs só *referenciam* o flag como precondição, então continuam válidos desde que `MODO_HOMENAGEM` dispare a mesma geração procedural que `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` disparava.
- [`objetos-cenario-circuito` documenta o default de `GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` como `false`, mas o código atual (`Global.java:42`) tem default `true` — spec já estava desatualizada antes desta mudança] → Não é causado por esta mudança, mas o novo requisito de `MODO_HOMENAGEM` (default `true`, confirmado pelo usuário) deve documentar o valor real, evitando repetir a mesma divergência.
- [Propagar `nomeHomenagem` pra todas as temporadas (decisão 7) exige varrer ~26 pares de arquivo a cada salvamento com o checkbox marcado — custo de I/O, mas é uma ação manual do usuário no editor (não roda em tempo de jogo), então a latência é aceitável] → Mitigação: nenhuma otimização especial necessária; é uma operação pontual disparada por clique, não um caminho quente.
- [Nome canônico da imagem pode, em casos raros, ser ambíguo ou diferente do esperado se duas equipes distintas acabarem canonicalizando pro mesmo nome (colisão de nomenclatura entre temporadas) — propagação afetaria carros de equipes diferentes por engano] → Mitigação: ao implementar, o editor deveria listar/confirmar quais temporadas serão afetadas antes de propagar (evita surpresa), e a passada de conteúdo (grupo 5 dos tasks) deve checar por colisões de nome canônico entre equipes genuinamente diferentes.

## Migration Plan

Sem migração de esquema destrutiva — a coluna nova é *append-only* posicionalmente, e os dados reais (chaves) não mudam. Todas as ~30+ temporadas são populadas com `nomeHomenagem` (carros e pilotos) nesta mesma mudança; o carregamento ainda trata a ausência da coluna (temporada não migrada) como fallback pro nome real, por robustez, mas isso não deve ocorrer em nenhuma temporada depois desta mudança. `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` é removido do código — nenhum dado persistido depende dele. Rollback: reverter os commits de código; os arquivos `.properties` com a coluna nova continuam válidos mesmo se o código for revertido (coluna extra ignorada pelo parser antigo, que só lê os primeiros N campos).

## Open Questions

(nenhuma pendente — a única em aberto, forma do "modelo colorido genérico", foi resolvida: já existe via `CarregadorRecursos.pintarModeloV2`/`Global.FORCE_MODELO_V2`, ver decisão 5.)

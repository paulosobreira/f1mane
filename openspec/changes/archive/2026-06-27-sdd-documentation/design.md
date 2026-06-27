## Context

O F1 Mane é um simulador de corridas de Fórmula 1 em Java, desenvolvido ao longo de vários anos. O mesmo JAR (`flmane.jar`) serve três modos distintos: servidor web com Tomcat embutido, jogo solo Swing e cliente multiplayer. O SDD deve descrever a arquitetura real — o que está compilado e rodando — sem idealizar funcionalidades incompletas (ex.: `JogoCliente` implementa parcialmente `InterfaceJogo`).

O documento alvo é `docs/sdd.md`, escrito em Markdown, com diagramas textuais onde necessário (ASCII ou Mermaid inline).

## Goals / Non-Goals

**Goals:**
- Documentar os três modos de execução com seus entry points e o que cada um inicializa
- Documentar a engine de corrida: o tick loop de `ControleCiclo`, o hub `ControleJogoLocal` e cada subsistema delegado
- Documentar a camada REST/multiplayer: `PaddockServer` como singleton, `LetsRace` endpoints, `SessaoCliente`, fluxo de criação de jogo servidor
- Documentar o modelo de dados central: campos relevantes de `Piloto`, `Carro`, `No`, `Circuito` com constantes reais do código
- Documentar a camada de persistência JPA com os dois perfis Maven e as entidades que estendem `F1ManeDados`
- Documentar o pipeline de rendering: como `PainelCircuito` é chamado, layout do `SpriteSheet`, flags de debug
- Documentar o carregamento de recursos: `CarregadorRecursos` como singleton com cache, XMLDecoder para circuitos, bundles i18n

**Non-Goals:**
- Documentar funcionalidades não implementadas (ex.: `getVantagem()` retorna null por design)
- Gerar Javadoc ou comentários inline no código
- Documentar o editor de circuitos (`MainFrameEditor`) em profundidade — ele é uma ferramenta auxiliar

## Decisions

**D1 — Um único arquivo `docs/sdd.md`**
Um SDD monolítico em Markdown é mais fácil de manter e navegar do que múltiplos arquivos fragmentados. Seções ancoradas com `#` permitem links diretos.

**D2 — Foco em fatos observáveis do código**
Cada afirmação no SDD deve corresponder a um campo, método ou constante real. Nomes de classe, valores de constantes (ex.: `LADO_W = 180`, `MaxJogo = 5`) e caminhos de arquivo devem ser literais.

**D3 — Diagramas textuais simples**
Blocos de código ASCII para mostrar hierarquia de composição dos controllers e fluxo do tick loop. Sem dependência de ferramentas externas.

**D4 — Não documentar `JogoCliente` como implementação completa**
`JogoCliente` implementa `InterfaceJogo` com ~40 métodos que delegam apenas a campos locais ou retornam zero/null. O SDD deve indicar que é uma implementação parcial do protocolo de jogo para o cliente web, não uma réplica completa do engine.

## Risks / Trade-offs

- **[Risco] SDD desatualiza rapidamente se o código muda** → Mitigation: estruturar por módulo estável (entidades, controllers, REST) em vez de por comportamento dinâmico; o SDD descreve arquitetura, não fluxos de dados frame-a-frame
- **[Trade-off] Nível de detalhe dos campos de Piloto/Carro** → Documentar campos arquiteturalmente relevantes (posição, volta, combustível, dano) e omitir campos puramente de display; o código-fonte é a referência canônica para campos individuais

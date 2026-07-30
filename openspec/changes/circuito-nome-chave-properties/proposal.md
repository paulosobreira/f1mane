## Why

Hoje `Circuito.nome` é gravado como campo próprio no XML de metadados (`<nome>_mro_meta.xml`) via `Circuito.copiaParaArquivoMetadados()`, mas o nome de exibição realmente usado pelo jogo (menu, HUD, servidor) vem de `circuitos.properties` (`arquivoXml=NomeExibicao,ativo`). O editor grava o nome só no XML e nunca atualiza `circuitos.properties`, então os dois valores podem divergir sem que ninguém perceba. Além disso, `Util.substVogais` distorce a primeira vogal do nome do circuito em várias telas de jogo por padrão — comportamento de "easter egg" que o usuário quer remover.

## What Changes

- **BREAKING**: `Circuito.nome` deixa de ser persistido no XML (`_mro_meta.xml`). O nome de exibição do circuito passa a ser resolvido a partir do valor associado à chave (nome do arquivo XML) em `circuitos.properties`, não mais de um campo serializado no bean.
- `Circuito.getNome()`/`setNome()` passam a ler/escrever através de `circuitos.properties` (via `CarregadorRecursos`/`ControleRecursos`), não mais um campo Java Bean serializado.
- Editor (`MainPanelEditor`): ao salvar o campo "nome" (`nomePistaText`), a alteração SHALL escrever o novo nome de exibição na linha correspondente de `circuitos.properties` (hoje `atualizarAtivoEmCircuitosProperties` só regrava o campo `ativo` — precisa ganhar irmã ou ser estendida para regravar também o nome) e o arquivo properties SHALL ser recarregado em memória (cache/buffer do `CarregadorRecursos`) após a escrita, para refletir a mudança imediatamente sem exigir restart.
- Remover completamente `Util.substVogais` (flag em `Util.java:27`, método em `Util.java:344-368`) e todos os call sites (`CarregadorRecursos.java:1280`, `ControleRecursos.java:485`, `ControleCampeonatoServidor.java:214`, `ControleJogosServer.java:330`, `PainelMenuLocal.java:1267,1361,1569,2548`, `PainelCircuito.java:4278`), passando a usar o nome puro em todos os pontos.
- Remover também a flag `"real"` de `MainFrame.java:441-447` que hoje desliga `substVogais` (fica sem sentido sem a feature).
- Migração de dados existentes: XMLs de metadados já gravados continuam com `<nome>` no arquivo (campo morto, ignorado na leitura) — não é necessário reescrever XMLs existentes em massa, mas o decoder/encoder deixa de ler/escrever esse campo.

## Capabilities

### New Capabilities
- `circuito-nome-exibicao`: nome de exibição do circuito é resolvido a partir de `circuitos.properties` (não do XML), editar no editor grava e recarrega o properties imediatamente, e nenhum nome de circuito passa mais por distorção de vogais.

### Modified Capabilities
- `circuito-metadados-arquivo`: `nome` sai da lista de campos persistidos em `<nome>_mro_meta.xml`.
- `modo-homenagem-toggle`: o requirement/scenario que hoje documenta "nomes de circuito continuam usando substVogais, fora do escopo daquele flag" deixa de fazer sentido — `substVogais` é removido por completo, não só mantido fora do escopo do modo homenagem.

## Impact

- Código afetado: `Circuito.java` (getter/setter de `nome`, `copiaParaArquivoMetadados`), `CarregadorRecursos.java`, `ControleRecursos.java`, `MainPanelEditor.java` (fluxo de salvar), `Util.java` (remoção de `substVogais`), `MainFrame.java` (remoção do parse do argumento `"real"`), e todos os call sites de `Util.substVogais` listados acima.
- Dados: XMLs de metadados existentes mantêm um campo `<nome>` órfão (ignorado); nenhuma migração de arquivo é necessária, mas pode ser feita como limpeza opcional futura.
- Compatibilidade: qualquer circuito hoje sem linha em `circuitos.properties` (novo, ainda não cadastrado) precisa de um caminho para ganhar nome de exibição — ver Open Questions em design.md.

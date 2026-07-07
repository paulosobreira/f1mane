## ADDED Requirements

### Requirement: Rótulos do launcher traduzidos via Lang
Os rótulos de ação do launcher Swing ("Copy link", "Open in browser", "Java solo game", "Java multiplayer game") SHALL ser obtidos via `Lang.msg(chave)` em vez de strings hardcoded, com chaves presentes em todos os bundles `idiomas/mensagens_XX.properties` existentes.

#### Scenario: Rótulos vêm do bundle do idioma padrão
- **WHEN** o launcher é exibido numa JVM cujo `Locale.getDefault()` corresponde a um bundle existente (ex.: `pt`)
- **THEN** os quatro rótulos de ação aparecem no idioma desse bundle

#### Scenario: Chaves presentes em todos os bundles
- **WHEN** qualquer bundle `idiomas/mensagens_XX.properties` empacotado é carregado
- **THEN** as chaves novas dos rótulos do launcher existem nesse bundle (nenhum rótulo cai no fallback de chave crua)

### Requirement: i18n do launcher isolada da i18n do servidor
Como o backend roda em JVM separada, o estado estático do `Lang` na JVM do launcher (bundle/sufixo do `Locale.getDefault()`) NÃO SHALL ser alterado por traduções de requisições REST do servidor, garantindo que launcher, `MainFrame` e `AppletPaddock` exibam mensagens consistentes no idioma da máquina do usuário.

#### Scenario: Requisições web não afetam o idioma do Swing
- **WHEN** clientes web fazem requisições ao servidor com header `idioma` diferente do idioma da máquina
- **THEN** os textos do launcher e das aplicações Swing na JVM do launcher permanecem no idioma do `Locale.getDefault()` local

## MODIFIED Requirements

### Requirement: Navegação Anterior/Próximo e combobox para selecionar circuito existente
O editor de circuitos SHALL permitir navegar pelos circuitos existentes com controles "Anterior"/"Próximo" alimentados pela lista de `circuitos.properties` (com fallback de varredura de `src/main/resources/circuitos/*.xml` caso a properties não possa ser lida), replicando o padrão de navegação por temporada já usado em `EditorCoresCarros`, em vez de um `JFileChooser` bruto. Adicionalmente, o editor SHALL exibir essa mesma lista em um combobox posicionado onde hoje aparece o rótulo do circuito atual, mostrando o nome amigável de cada circuito (parseado de `circuitos.properties`) como texto visível, com o nome do arquivo XML como valor interno de seleção; selecionar um item no combobox SHALL carregar esse circuito exatamente como Anterior/Próximo carregam o circuito adjacente, e Anterior/Próximo SHALL manter o combobox sincronizado com o índice atual sem disparar recarregamento duplicado.

#### Scenario: Navegar para o próximo circuito
- **WHEN** o usuário clica em "Próximo" estando em um circuito que não é o último da lista
- **THEN** o editor carrega e exibe o próximo circuito da lista ordenada, o controle "Próximo" fica desabilitado ao alcançar o último circuito, e o combobox passa a mostrar o nome amigável do novo circuito selecionado

#### Scenario: Navegar para o circuito anterior
- **WHEN** o usuário clica em "Anterior" estando em um circuito que não é o primeiro da lista
- **THEN** o editor carrega e exibe o circuito anterior da lista ordenada, o controle "Anterior" fica desabilitado ao alcançar o primeiro circuito, e o combobox passa a mostrar o nome amigável do novo circuito selecionado

#### Scenario: Fallback quando circuitos.properties não pode ser lido
- **WHEN** `properties/circuitos.properties` não pode ser lido
- **THEN** o editor monta a lista navegável (e as opções do combobox) varrendo diretamente os arquivos `src/main/resources/circuitos/*.xml`, usando o nome do arquivo como nome amigável nesse caso

#### Scenario: Combobox mostra nome amigável, não o arquivo XML
- **WHEN** o editor de circuitos exibe a lista de circuitos disponíveis no combobox
- **THEN** cada item mostra o nome amigável do circuito (ex.: "Albert Park"), não o nome do arquivo XML (ex.: `albert_park_mro.xml`)

#### Scenario: Selecionar um circuito no combobox carrega esse circuito
- **WHEN** o usuário seleciona um item diferente do atual no combobox de circuitos
- **THEN** o editor carrega o circuito correspondente ao arquivo XML associado a esse item, do mesmo jeito que Anterior/Próximo carregam um circuito adjacente

## ADDED Requirements

### Requirement: Editor de circuitos não exibe botão de créditos
O editor de circuitos (`EditorCircuitos`/`MainPanelEditor`) SHALL NOT exibir um botão "Créditos" nem oferecer um fluxo de edição de posição de créditos na imagem do circuito.

#### Scenario: Botão de créditos removido
- **WHEN** a janela do editor de circuitos é aberta após esta mudança
- **THEN** nenhum botão "Créditos" está presente entre os controles de topo, e clicar em qualquer ponto do canvas não aciona mais um fluxo de definição de posição de créditos

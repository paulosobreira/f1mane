# editor-autosave-undo

## Purpose

Evitar perda de trabalho no editor de circuitos salvando automaticamente o circuito em disco a cada objeto incluído (quando o circuito já tem arquivo associado), preservando sempre um backup do estado imediatamente anterior, e permitindo desfazer a última inclusão com Ctrl+Z restaurando esse backup.

## Requirements

### Requirement: Editor salva automaticamente o circuito ao incluir um objeto
Sempre que um objeto for efetivamente incluído no circuito pelo editor (`ObjetoTransparencia`, `ObjetoLivre`, `ObjetoGuardRails`, `ObjetoArquibancada`, `ObjetoEscapada`, qualquer objeto posicionado por um único clique como `ObjetoConstrucao`/`ObjetoPneus`, ou uma cópia de objeto existente feita via Alt+C), e o circuito já tiver um arquivo associado (`file != null`, ou seja, já foi salvo manualmente ao menos uma vez), o editor SHALL gravar automaticamente o circuito em disco no mesmo par de arquivos usado por "Salvar Pista Atual" (`<nome>_mro.xml` e `<nome>_mro_meta.xml`), sem abrir `JFileChooser` nem diálogo de confirmação de sucesso.

#### Scenario: Incluir um objeto de posicionamento único aciona o salvamento automático
- **WHEN** o usuário clica no canvas pra posicionar um novo `ObjetoConstrucao` (ou outro objeto de clique único) num circuito que já tem arquivo associado
- **THEN** os arquivos `<nome>_mro.xml` e `<nome>_mro_meta.xml` em disco são regravados refletindo o novo objeto, sem nenhum diálogo aparecer

#### Scenario: Incluir um objeto multi-ponto aciona o salvamento automático ao finalizar
- **WHEN** o usuário finaliza a criação de um `ObjetoGuardRails`, `ObjetoArquibancada`, `ObjetoLivre`, `ObjetoTransparencia` ou `ObjetoEscapada` (clique que fecha o objeto e o adiciona à lista correspondente do circuito)
- **THEN** o salvamento automático ocorre nesse momento, com o objeto já incluído nos arquivos gravados

#### Scenario: Copiar um objeto existente via Alt+C aciona o salvamento automático
- **WHEN** o usuário seleciona um objeto existente e pressiona Alt+C, criando uma cópia desse objeto na mesma lista do original (`circuito.getObjetos()` ou `circuito.getObjetosCenario()`, conforme a origem)
- **THEN** o salvamento automático ocorre nesse momento, com a cópia já incluída nos arquivos gravados, do mesmo jeito que uma inclusão por clique no canvas

#### Scenario: Circuito sem arquivo associado não aciona salvamento automático
- **WHEN** o usuário inclui um objeto num circuito recém-criado que ainda nunca foi salvo manualmente (`file == null`)
- **THEN** o objeto é incluído normalmente na lista e no desenho do circuito em memória, mas nenhum arquivo é gravado em disco por esse salvamento automático

#### Scenario: Salvamento automático não bloqueia por validações do salvamento manual
- **WHEN** o circuito tem uma zona de escapada incompleta de uma sessão anterior, ou o campo de distância (km) não está preenchido, e o usuário inclui um novo objeto
- **THEN** o salvamento automático ainda ocorre normalmente para esse objeto (sem abrir os alertas de escapada incompleta ou distância não informada que "Salvar Pista Atual" exibiria)

### Requirement: Backup do estado anterior é preservado antes de cada salvamento automático
Antes de sobrescrever `<nome>_mro.xml`/`<nome>_mro_meta.xml` num salvamento automático, o editor SHALL copiar o conteúdo atual desses arquivos (o estado de antes da inclusão do objeto) para um par de arquivos de backup no mesmo diretório, substituindo qualquer backup anterior.

#### Scenario: Backup é gravado antes da nova versão
- **WHEN** um salvamento automático ocorre e os arquivos principais já existem em disco
- **THEN** o conteúdo que estava nos arquivos principais imediatamente antes é copiado para os arquivos de backup, e só depois os arquivos principais são regravados com o novo estado

#### Scenario: Backup é substituído a cada novo salvamento automático
- **WHEN** dois objetos são incluídos em sequência, cada um disparando um salvamento automático
- **THEN** o arquivo de backup após o segundo salvamento automático reflete o estado de antes do segundo objeto (não mais o estado de antes do primeiro)

#### Scenario: Sem arquivos principais em disco, nada é copiado para backup
- **WHEN** o salvamento automático dispara pela primeira vez para um circuito cujo arquivo já está associado (`file != null`) mas cujos arquivos principais ainda não existem fisicamente em disco
- **THEN** nenhuma cópia de backup é feita antes de gravar os arquivos principais pela primeira vez

### Requirement: Ctrl+Z desfaz a última inclusão salva automaticamente
Com o foco no editor de circuitos, pressionar Ctrl+Z SHALL restaurar os arquivos principais do circuito (`<nome>_mro.xml`/`<nome>_mro_meta.xml`) a partir dos arquivos de backup mais recentes, e SHALL recarregar o circuito exibido no editor a partir desse estado restaurado (lista de objetos, painel de filtro por tipo e desenho do canvas todos refletindo o circuito restaurado).

#### Scenario: Ctrl+Z restaura o circuito ao estado anterior à última inclusão
- **WHEN** o usuário inclui um objeto (disparando o salvamento automático e o backup correspondente) e em seguida pressiona Ctrl+Z
- **THEN** os arquivos principais do circuito voltam a ter o conteúdo de antes dessa inclusão, e o editor recarrega e exibe o circuito sem esse objeto

#### Scenario: Ctrl+Z sem backup disponível não tem efeito
- **WHEN** o usuário pressiona Ctrl+Z antes de qualquer salvamento automático ter ocorrido nesta sessão de edição (nenhum arquivo de backup existe)
- **THEN** nada acontece — nenhum arquivo é alterado, nenhum diálogo de erro aparece, e o circuito exibido permanece o mesmo

#### Scenario: Pressionar Z sozinho continua alterando o ângulo do objeto selecionado
- **WHEN** o usuário pressiona a tecla Z sem o modificador Ctrl
- **THEN** o comportamento existente de "menos ângulo" no objeto selecionado continua ocorrendo normalmente, sem acionar o desfazer

#### Scenario: Ctrl+Z repetido sem nova inclusão no meio é idempotente
- **WHEN** o usuário pressiona Ctrl+Z duas vezes seguidas, sem incluir nenhum objeto novo entre as duas pressões
- **THEN** a segunda pressão restaura o mesmo estado já restaurado pela primeira, sem erro

### Requirement: Atalho de desfazer aparece no painel de atalhos do canvas
O painel de atalhos já exibido no canto superior direito do canvas do editor (`desenhaControles`) SHALL incluir uma linha indicando o atalho Ctrl+Z para desfazer, junto das demais linhas de atalho já existentes (Inserir, Apagar, Sobe/Desce Nível etc.).

#### Scenario: Linha do atalho de desfazer aparece no painel
- **WHEN** o editor de circuitos é aberto e o canvas é desenhado
- **THEN** o painel de atalhos no canto superior direito exibe uma linha para "Ctrl+Z" com o texto de desfazer, visível junto das demais linhas de atalho

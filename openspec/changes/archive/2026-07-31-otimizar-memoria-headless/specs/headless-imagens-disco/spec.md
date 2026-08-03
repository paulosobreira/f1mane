## ADDED Requirements

### Requirement: Estruturas auxiliares da pré-geração liberadas antes de servir requisições
Ao concluir a pré-geração de imagens em modo headless (ou ao pular a pré-geração por marcador já presente), `MainLauncher` SHALL liberar as estruturas carregadas apenas para essa etapa — listas de pilotos e carros por temporada, circuitos desserializados e quaisquer caches auxiliares populados durante a geração — antes de bindar a porta, de modo que o heap em regime não carregue o pico da pré-geração.

#### Scenario: Heap em regime após pré-geração completa
- **WHEN** o servidor headless conclui a pré-geração de todas as imagens e passa a aceitar requisições
- **THEN** as estruturas usadas só pela pré-geração já não são alcançáveis, e o heap usado após coleta é comparável ao de um boot que pulou a pré-geração por marcador

#### Scenario: Boot com imagens já assadas
- **WHEN** o servidor sobe sobre um diretório com marcador de pré-geração concluída
- **THEN** nenhuma lista de pilotos/carros nem circuito é desserializado por conta da pré-geração, e a subida vai direto ao bind da porta

#### Scenario: Assets continuam disponíveis após a liberação
- **WHEN** um cliente requisita, após a subida, a imagem de fundo de um circuito, um carro ou um capacete
- **THEN** os bytes são servidos a partir do arquivo em disco, sem depender de nenhuma estrutura liberada

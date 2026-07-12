## ADDED Requirements

### Requirement: Traçado do box é desenhado com borda branca
Tanto `PainelCircuito` (usado para gerar a imagem de fundo de corrida/multiplayer) quanto `DesenhoProceduralCircuito` (usado pela tela ao vivo do editor, via `MainPanelEditor.paintComponent()`) SHALL desenhar uma borda branca ao longo do traçado do box (a sequência de nós de `circuito.getBoxKey()` usada por `desenhaPistaBox()`), pintada antes da linha central cinza do box, no mesmo espírito de `desenhaTintaPistaZebra()`/`desenhaTintaPistaEZebra()` pintar a borda branca da pista antes de `desenhaPista()`. As duas classes têm implementações próprias (sem código compartilhado entre elas, já era assim antes desta mudança para pista/box/zebra).

#### Scenario: Box sem sobreposição com a pista tem borda branca visível
- **WHEN** o circuito é desenhado (no editor ou na geração da imagem de fundo) e um trecho do traçado do box não se sobrepõe geometricamente ao traçado da pista
- **THEN** esse trecho do box é desenhado com uma borda branca visível ao redor da linha central cinza

### Requirement: Borda branca do box é suprimida onde intersecciona a pista
Nos trechos em que a área pintada da borda branca do box (calculada a partir do traçado do box) intersecciona geometricamente a área já pintada pelo traçado da pista (a mesma área usada por `desenhaPista()`/`desenhaTintaPistaZebra()`), o desenho SHALL NOT pintar a linha branca do box nesses trechos. A área efetivamente pintada de branco para o box SHALL ser a área da borda do box menos (`Area.subtract`) a área da pista.

#### Scenario: Box sobreposto à pista não desenha linha branca por cima
- **WHEN** o circuito é desenhado e um trecho do traçado do box se sobrepõe geometricamente à área já pintada pelo traçado da pista
- **THEN** nenhuma linha branca do box é desenhada nesse trecho sobreposto, mesmo que a linha central cinza do box continue sendo desenhada normalmente ali

#### Scenario: Box parcialmente sobreposto tem borda branca só na parte livre
- **WHEN** um mesmo segmento contínuo do traçado do box tem uma parte que intersecciona a pista e outra parte que não intersecciona
- **THEN** a borda branca aparece apenas na parte que não intersecciona a área da pista, sem descontinuar a linha central cinza do box em nenhum ponto

### Requirement: Caminho do box usado para a borda não é fechado de volta ao primeiro nó
Diferente do traçado da pista (um loop fechado), o traçado do box vai do primeiro nó (entrada) até o último (saída) sem voltar ao início — SHALL NOT ser tratado como um caminho fechado ao calcular a forma da borda branca. Fechar o caminho de volta ao primeiro nó (mesma lógica usada para a pista) produz uma faixa branca larga e espúria ligando o primeiro e o último nó do box, que SHALL NOT aparecer.

#### Scenario: Nenhuma linha é desenhada entre o primeiro e o último nó do box
- **WHEN** o circuito é desenhado e o primeiro e o último nó do traçado do box não são adjacentes no percurso real (ex.: entrada longe da saída)
- **THEN** nenhuma linha ou borda branca é desenhada ligando diretamente o primeiro nó ao último nó do box, fora do próprio percurso real do traçado

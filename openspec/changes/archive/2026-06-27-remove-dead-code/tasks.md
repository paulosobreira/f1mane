## 1. Classes de teste desacopladas

- [x] 1.1 Verificar com grep que `PainelTeste` não é referenciada em nenhum arquivo Java ou de build
- [x] 1.2 Remover `src/main/java/br/f1mane/visao/PainelTeste.java`
- [x] 1.3 Remover método `main` de `src/main/java/br/f1mane/editor/FormularioListaObjetos.java`

## 2. Blocos de código comentado

- [x] 2.1 Remover bloco comentado (linhas 90-109) em `src/main/java/br/f1mane/editor/FormularioObjetos.java`
- [x] 2.2 Remover bloco comentado (linhas 80-99) em `src/main/java/br/f1mane/servidor/applet/PainelCampeonato.java`
- [x] 2.3 Remover linha comentada `//campeonato.getNomePiloto()` em `src/main/java/br/f1mane/visao/PainelCampeonato.java`
- [x] 2.4 Remover linha comentada `//desenhaFPS(...)` em `src/main/java/br/f1mane/visao/PainelMenuLocal.java`
- [x] 2.5 Remover linha comentada `//affineTransform = ...` em `src/main/java/br/f1mane/entidades/ObjetoConstrucao.java`

## 3. Stubs TODO em ControleJogoLocal e entidades

- [x] 3.1 Remover ou limpar métodos `iniciaJanela()`, `getVantagem()`, `setVantagem(String)` em `src/main/java/br/f1mane/controles/ControleJogoLocal.java` (verificar antes se são chamados via grep)
- [x] 3.2 Remover constructor stub vazio com TODO em `src/main/java/br/f1mane/entidades/ConstrutoresPontosCampeonato.java`

## 4. Stubs TODO em JogoCliente

- [x] 4.1 Fazer grep de todos os `// TODO Auto-generated method stub` em `src/main/java/br/f1mane/servidor/applet/JogoCliente.java`
- [x] 4.2 Para cada método com corpo vazio (retorna null/0/false sem TODO), remover apenas o comentário TODO
- [x] 4.3 Para métodos que só têm o comentário TODO e nenhum retorno funcional, avaliar se podem ser removidos sem quebrar a compilação; se não, substituir o comentário por nenhum comentário (deixar corpo vazio limpo)

## 5. Métodos sem chamadores

- [x] 5.1 Verificar com grep que `isClimaAleatorio` não é chamado em nenhum arquivo; remover de `src/main/java/br/f1mane/controles/ControleClima.java`
- [x] 5.2 Verificar com grep que `getTipoJson` não é chamado; remover de `src/main/java/br/f1mane/entidades/No.java`
- [x] 5.3 Verificar com grep que `getCor2Hex` não é chamado; remover de `src/main/java/br/f1mane/entidades/Carro.java`
- [x] 5.4 Verificar com grep que `exibirResiltadoFinal` não é chamado (só há referência comentada em JogoCliente:238); remover de `src/main/java/br/f1mane/MainFrameEditor.java`
- [x] 5.5 Verificar com grep que `verificaCarroLentoOuDanificado` e `acharPilotoDaFrente` não são chamados; remover de `src/main/java/br/f1mane/controles/ControleCorrida.java`
- [x] 5.6 Verificar com grep que `setMapaIdsNos` e `setMapaNosIds` não são chamados; remover de `src/main/java/br/f1mane/controles/ControleRecursos.java`

## 6. Validação

- [x] 6.1 Executar `mvn clean package -Ph2 -DskipTests` e confirmar que o build compila sem erros
- [x] 6.2 Executar `./simulacao.sh` (ou equivalente) para confirmar que a simulação headless ainda funciona

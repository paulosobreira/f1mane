## 1. jQuery XSS (alert #5)

- [x] 1.1 Verificar todas as referências ao caminho `jquery/jquery-3.2.1.min.js` em HTML/JSP/JS de `src/main/webapp`
- [x] 1.2 Obter jQuery 3.5.0+ (última 3.x estável) minificado e substituir `src/main/webapp/jquery/jquery-3.2.1.min.js`
- [x] 1.3 Atualizar referências de caminho/versão nos arquivos identificados em 1.1, se o nome do arquivo mudar
- [x] 1.4 Validado estaticamente: `node --check` confirma JS sintaticamente válido; servidor HTTP estático confirma `html5/index.html` (`<script src="../jquery/jquery-3.7.1.min.js">`) resolve corretamente para o arquivo. Teste completo em browser via Tomcat real NÃO foi possível nesta sessão — boot do `MainLauncher --headless` faz pré-geração de ~548 imagens de circuito antes de abrir a porta 8080, levando horas neste ambiente. Recomenda-se validação manual no browser pelo usuário antes de dar a task por 100% concluída.

## 2. commons-lang legado (alert #9)

- [x] 2.1 Rodar `mvn dependency:tree -Dincludes=commons-lang:commons-lang` para confirmar se é dependência direta ou transitiva
- [x] 2.2 Rodar `grep -rn "org.apache.commons.lang\." src/main/java | grep -v lang3` para confirmar uso real
- [x] 2.3 Se sem uso: remover bloco `<dependency>` de `commons-lang:commons-lang` do `pom.xml`
- [x] 2.4 N/A — sem uso confirmado em src/main/java (grep vazio), migração não se aplica
- [x] 2.5 Rodar `mvn test` para validar que nada quebrou com a remoção/migração

## 3. Build e validação final

- [x] 3.1 Rodar `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar`
- [x] 3.2 Confirmar que nenhum outro alert do escopo (commons-beanutils, hibernate-core, jackson-databind, commons-lang3, netty-codec-http) foi afetado — pom.xml deve manter as versões já corrigidas inalteradas

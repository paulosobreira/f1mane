## REMOVED Requirements

### Requirement: Tomcat embutido atualizado para 11.x
**Reason**: Tomcat é removido do projeto por completo — o servidor web embutido passa a ser Netty (ver capability `netty-http-server`), eliminando a necessidade de acompanhar versões do Tomcat/Jasper em lockstep com o resto da pilha Jakarta EE.
**Migration**: Nenhuma ação do usuário final necessária. O contexto `/flmane` e a porta 8080 continuam os mesmos; agora servidos por Netty em vez de Tomcat.

### Requirement: Servlet API e JAX-RS migrados para Jakarta EE 11
**Reason**: A Jakarta Servlet API e o JAX-RS/Jersey são removidos por completo, não apenas atualizados — os endpoints REST passam a ser roteados diretamente sobre Netty (ver capability `netty-http-server`), sem depender de `jakarta.servlet.*` nem `jakarta.ws.rs.*`.
**Migration**: Nenhuma mudança de contrato HTTP para os clientes existentes (HTML5 e `AppletPaddock`); os mesmos paths, métodos e status codes de `/flmane/rest/letsRace/*` continuam válidos.

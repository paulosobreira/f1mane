## Context

O projeto F1 Mane é um simulador de corridas F1 em Java, com histórico de desenvolvimento iterativo que acumulou stubs auto-gerados de IDE, blocos comentados, classes de teste desacopladas e métodos nunca completados. Antes de produzir o SDD (Software Design Document) do sistema, é necessário que o código reflita apenas o que está efetivamente em uso, eliminando ruído que prejudica a leitura e pode gerar confusão na documentação.

Não há mudança de comportamento em runtime — trata-se exclusivamente de limpeza estrutural.

## Goals / Non-Goals

**Goals:**
- Remover classes de teste desacopladas de produção (`PainelTeste.java`)
- Remover métodos `main` de debug que não são entry points do sistema
- Remover blocos de código comentado (dead code comentado não é documentação)
- Remover stubs TODO gerados por IDE sem corpo útil
- Remover linhas isoladas comentadas que não explicam nada

**Non-Goals:**
- Refatorar ou reescrever lógica existente
- Resolver os stubs incompletos de `JogoCliente.java` (isso é trabalho de implementação, não de limpeza)
- Modificar comportamento em runtime
- Tocar em comentários que são documentação legítima (Javadoc, explicações de algoritmo)

## Decisions

**D1 — Remover `PainelTeste.java` inteiramente**
A classe não é referenciada por nenhum outro arquivo de produção. Mantê-la como "teste manual" sem vínculo ao sistema não agrega valor e pode confundir futuros leitores do SDD.

**D2 — Remover apenas o método `main` de `FormularioListaObjetos.java`, não a classe**
A classe tem função legítima (UI de lista de objetos no editor de circuito). O `main` era um atalho de teste de desenvolvimento.

**D3 — Remover stubs TODO em `ControleJogoLocal.java`**
`iniciaJanela()`, `getVantagem()` e `setVantagem()` foram gerados pela IDE ao implementar `InterfaceJogo` e nunca preenchidos. Como a interface já os declara e o método não é chamado, a implementação vazia pode ser removida sem quebrar contrato.

**D4 — Manter stubs de `JogoCliente.java` como `throw new UnsupportedOperationException()` ou vazio controlado**
Os ~40 stubs indicam que `JogoCliente` implementa `InterfaceJogo` parcialmente. Removê-los quebraria a compilação (interface contract). A decisão aqui é apenas documentar que estão incompletos — a limpeza desses métodos pertence a uma task de implementação futura. Para este change, apenas removemos os comentários `// TODO Auto-generated` das implementações vazias que já têm corpo correto (retornam null, 0, ou false por contrato).

**D5 — Blocos comentados: remover sem substituição**
Código comentado que não tem comentário explicando por que está ali não é documentação — é código morto. Git preserva o histórico se precisar recuperar.

## Risks / Trade-offs

- **[Risco] Remoção acidental de método que é chamado via reflexão** → Mitigation: verificar cada método removido com grep antes da exclusão.
- **[Risco] `PainelTeste.java` referenciada por build scripts externos** → Mitigation: checar `pom.xml` e scripts `.sh` antes de remover.
- **[Trade-off] Manter stubs de `JogoCliente` sem corpo** → Aceito; o comportamento de "não faz nada" é o comportamento atual de produção — alterar isso vai além do escopo de limpeza.

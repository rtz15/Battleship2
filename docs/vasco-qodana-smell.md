# 111331 - Vasco Rodrigues

## Smell escolhido

- **CallToPrintStackTrace**
- **Ficheiro alvo:** `src/main/java/battleship/GameHistory.java`
- **Contexto do requisito:** H2 / histórico de jogos (ficha 2)

## Justificação

`GameHistory` contém chamadas diretas a `e.printStackTrace()` nos blocos `catch (SQLException e)`.
Este padrão é um smell porque não há tratamento estruturado do erro e o output fica acoplado a `stderr`, dificultando controlo operacional e testes.

## Quality gate configurado

- Workflow: `.github/workflows/qodana-vasco.yml`
- Config: `qodana-vasco.yaml`
- Inspeção ativa: `CallToPrintStackTrace`
- Escopo limitado ao ficheiro `GameHistory.java`
- Gate: `--fail-threshold 0`

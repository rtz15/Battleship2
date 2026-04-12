# 111331 - Vasco Reis Teixeira

## Smell escolhido
- **CallToPrintStackTrace**
- **Ficheiro alvo:** `src/main/java/battleship/GameHistory.java`
- **Contexto do requisito:** H2 / historico de jogos (ficha 2)

## Justificacao
`GameHistory` contem chamadas diretas a `e.printStackTrace()` nos blocos `catch (SQLException e)`.
Este padrao e um smell porque nao ha tratamento estruturado do erro e o output fica
acoplado a `stderr`, dificultando controlo operacional e testes.

## Quality gate configurado
- Workflow: `.github/workflows/qodana-vasco.yml`
- Config: `qodana-vasco.yaml`
- Linter: `qodana-jvm-community`
- Inspecao ativa: `CallToPrintStackTrace`
- Escopo limitado ao ficheiro `GameHistory.java`
- Gate: `--fail-threshold 0`

## Nota de validacao
O ficheiro alvo contem chamadas a `printStackTrace()` em blocos `catch`, pelo que a configuracao deve
ser validada diretamente pelo run do GitHub Actions e pelo respetivo comentario do Qodana na PR.

Se o run nao reportar findings, deve confirmar-se primeiro se a inspecao esta realmente a ser aplicada
com a configuracao pretendida antes de fechar a analise como concluida.

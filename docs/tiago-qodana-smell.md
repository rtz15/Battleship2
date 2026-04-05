# Parte 2A - Qodana

## Smell escolhido

- Smell: `MethodWithMultipleReturnPoints`
- Metodo alvo: `AppLanguage.fromCode(String)`
- Ficheiro: `src/main/java/battleship/AppLanguage.java`

## Justificacao

O requisito de i18n introduziu classes de suporte leves (`AppLanguage`, `LanguageSupport`, `Messages`), e o smell escolhido foi isolado diretamente numa dessas classes.

Em `AppLanguage.fromCode(String)` existe logica de selecao da lingua com varios pontos de retorno:

- `return PORTUGUESE` para entrada nula ou vazia
- `return ENGLISH` se o codigo comecar por `en`
- `return PORTUGUESE` se o codigo comecar por `pt`
- `return PORTUGUESE` como fallback final

Isto torna `AppLanguage.fromCode(String)` um candidato plausivel a `MethodWithMultipleReturnPoints`. A documentacao oficial do Inspectopedia indica que metodos com demasiados pontos de retorno podem ser confusos e mais dificeis de refatorar.

## Configuracao adicionada

- `qodana.yaml`
- `.github/workflows/qodana.yml`

## O que a configuracao faz

1. Usa `qodana-jvm-community`, para nao depender de licenca paga nem de `QODANA_TOKEN`.
2. Usa o perfil `empty`.
3. Inclui apenas a inspecao `MethodWithMultipleReturnPoints`.
4. Limita essa inspecao ao ficheiro `src/main/java/battleship/AppLanguage.java`.
5. Define um quality gate no workflow com `fail-threshold: 0`.

Na pratica, o workflow considera falha sempre que o Qodana reportar pelo menos um problema dessa inspecao no ficheiro alvo.

## Resultado obtido

O workflow do Qodana foi executado com sucesso em GitHub Actions com a inspecao `MethodWithMultipleReturnPoints` aplicada ao ficheiro `src/main/java/battleship/AppLanguage.java`.

No run mais recente, o sumario apresentado pelo GitHub UI foi:

`No new problems were found according to the checks applied`

Assim, a execucao do workflow e do quality gate ficou validada, mas sem findings reportaveis nas annotations apresentadas pelo GitHub.

## Conclusao

Foi configurado um workflow de GitHub Actions com Qodana especificamente para avaliar o smell `MethodWithMultipleReturnPoints` no metodo `AppLanguage.fromCode(String)`.

O workflow executou com sucesso e o quality gate ficou operacional, mas a execucao analisada nao produziu findings reportaveis para a inspecao configurada.

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

Isto torna `AppLanguage.fromCode(String)` um candidato claro a `MethodWithMultipleReturnPoints`, e o Qodana consegue detetar esse problema por essa inspecao. A documentacao oficial do Inspectopedia indica que metodos com demasiados pontos de retorno podem ser confusos e mais dificeis de refatorar.

## Configuracao adicionada

- `qodana.yaml`
- `.github/workflows/qodana.yml`

## O que a configuracao faz

1. Usa `qodana-jvm-community`, para nao depender de licenca paga nem de `QODANA_TOKEN`.
2. Usa o perfil `empty`.
3. Inclui apenas a inspecao `MethodWithMultipleReturnPoints`.
4. Limita essa inspecao ao ficheiro `src/main/java/battleship/AppLanguage.java`.
5. Define um quality gate no workflow com `fail-threshold: 0`.

Na pratica, o workflow falha se o Qodana detetar pelo menos um problema dessa inspecao no ficheiro alvo.

## Resultado obtido

O workflow do Qodana foi executado com sucesso em GitHub Actions com a inspecao `MethodWithMultipleReturnPoints` aplicada ao ficheiro `src/main/java/battleship/AppLanguage.java`.

No run mais recente, o sumario apresentado pelo GitHub UI foi:

`No new problems were found according to the checks applied`

Assim, a execucao do workflow e do quality gate ficou validada, mas sem findings reportaveis nas annotations apresentadas pelo GitHub.

## Passos manuais finais

1. Manter a issue e a pull request alinhadas com o smell final escolhido:
   - `MethodWithMultipleReturnPoints`
   - `AppLanguage.fromCode(String)`
2. Registar na issue e na PR que o workflow correu com sucesso mas sem findings reportaveis.
3. Aguardar revisao e merge da pull request.
4. Depois do merge, confirmar o fecho automatico da issue associada.

## Opcional

Se quiseres integrar com Qodana Cloud mais tarde:

1. criar um projeto em Qodana Cloud
2. guardar o token em `QODANA_TOKEN`
3. adicionar esse secret ao step `Qodana Scan`

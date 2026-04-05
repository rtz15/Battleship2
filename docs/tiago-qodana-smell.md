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

## Resultado esperado

O resultado esperado e que o workflow sinalize `AppLanguage.fromCode(String)` como metodo com multiplos pontos de retorno.

Se o relatorio nao assinalar problemas, isso significa uma destas coisas:

- o metodo foi entretanto refatorado e deixou de ultrapassar o limiar da inspecao
- a configuracao do projeto no runner nao permitiu ao Qodana importar o projeto corretamente

## Passos manuais finais

1. Fazer `git add`, `commit` e `push` da branch.
2. No GitHub, abrir uma pull request para `main` ou correr manualmente o workflow em `Actions`.
3. Abrir o workflow `Qodana Tiago i18n smell gate`.
4. Confirmar que o job falha ou gera anotacoes no `Tasks.java`.
5. Abrir o artefacto/SARIF se precisares de mostrar a localizacao exata do smell.

## Opcional

Se quiseres integrar com Qodana Cloud mais tarde:

1. criar um projeto em Qodana Cloud
2. guardar o token em `QODANA_TOKEN`
3. adicionar esse secret ao step `Qodana Scan`

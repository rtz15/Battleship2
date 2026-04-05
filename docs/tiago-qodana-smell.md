# Parte 2A - Qodana

## Smell escolhido

- Smell: `OverlyComplexMethod`
- Metodo alvo: `Tasks.menu()`
- Ficheiro: `src/main/java/battleship/Tasks.java`

## Justificacao

O requisito de i18n introduziu classes de suporte leves (`AppLanguage`, `LanguageSupport`, `Messages`), mas a integracao do comportamento visivel ao utilizador ficou concentrada em classes centrais.

Nas metricas exportadas pelo MetricsTree, `Tasks.menu()` aparece como um hotspot defensavel para a parte 2A:

- `CC = 22`
- `LOC = 83`
- `CCM = 12`

Isto torna `Tasks.menu()` um candidato claro a `Long Method` / metodo excessivamente complexo, e o Qodana consegue detetar esse problema pela inspecao `OverlyComplexMethod`.

## Configuracao adicionada

- `qodana.yaml`
- `.github/workflows/qodana.yml`

## O que a configuracao faz

1. Usa `qodana-jvm-community`, para nao depender de licenca paga nem de `QODANA_TOKEN`.
2. Usa o perfil `empty`.
3. Inclui apenas a inspecao `OverlyComplexMethod`.
4. Limita essa inspecao ao ficheiro `src/main/java/battleship/Tasks.java`.
5. Define um quality gate no workflow com `fail-threshold: 0`.

Na pratica, o workflow falha se o Qodana detetar pelo menos um problema dessa inspecao no ficheiro alvo.

## Resultado esperado

O resultado esperado e que o workflow sinalize `Tasks.menu()` como metodo excessivamente complexo.

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

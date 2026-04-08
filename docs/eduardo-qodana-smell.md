# Parte 2A - Qodana

## Smell escolhido

- Smell: `UnusedDeclaration`
- Metodo alvo: `PdfExporter.export(GameSummary)`
- Ficheiro: `src/main/java/battleship/PdfExporter.java`

## Justificacao

O requisito do PDF introduziu a classe `PdfExporter` e uma sobrecarga de metodo para exportacao:

- `export(GameSummary)`
- `export(GameSummary, Path)`

A analise local com `Inspect Code` sinalizou que `export(GameSummary)` nao esta a ser usado no estado atual do projeto, enquanto o fluxo real do jogo usa a versao com `Path`.

Isto torna o metodo um bom candidato a `UnusedDeclaration`, um smell de codigo morto ou API redundante.

## Configuracao adicionada

- `qodana-eduardo.yaml`
- `.github/workflows/qodana-eduardo.yml`

## O que a configuracao faz

1. Usa `qodana-jvm-community`, para nao depender de licenca paga nem de `QODANA_TOKEN`.
2. Usa o perfil `empty`.
3. Inclui apenas a inspecao `UnusedDeclaration`.
4. Limita essa inspecao ao ficheiro `src/main/java/battleship/PdfExporter.java`.
5. Define um quality gate no workflow com `fail-threshold: 0`.

Na pratica, o workflow deve falhar sempre que o Qodana reportar pelo menos um finding dessa inspecao no ficheiro alvo.

## Relacao com a analise da ficha 3

Este smell esta alinhado com:

- a analise local feita sobre o requisito PDF
- o issue aberto para o overload nao usado
- o objetivo da ficha de cada membro escolher um cheiro diferente e construir um workflow Qodana para o detetar

## Conclusao

Foi configurado um workflow de GitHub Actions com Qodana especificamente para avaliar o smell `UnusedDeclaration` no ficheiro `src/main/java/battleship/PdfExporter.java`, com foco no metodo `PdfExporter.export(GameSummary)`.

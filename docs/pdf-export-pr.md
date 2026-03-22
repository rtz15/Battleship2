## Resumo
- adiciona exportacao de resumo do jogo para PDF com OpenPDF
- gera `./output/summary.pdf` no fim da simulacao
- inclui teste deterministico para validar o conteudo principal do PDF

## Issue
Closes: https://github.com/rtz15/Battleship2/issues/<ISSUE_NUMBER>

## Como testar
```bash
mvn test
mvn package
```

Depois, correr `battleship.Main`, executar `gerafrota` seguido de `simula` e confirmar a existencia de `output/summary.pdf`.

## Checklist
- [ ] mantem a simulacao existente a funcionar
- [ ] gera PDF com resumo final
- [ ] inclui teste deterministico
- [ ] documentacao local atualizada

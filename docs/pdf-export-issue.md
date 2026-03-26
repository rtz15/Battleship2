## Titulo
Exportar os resultados da simulacao para PDF

## Label
`type: ENHANCEMENT`

## User Story
Como utilizador da simulacao Battleship2, quero exportar o resultado final do jogo para um ficheiro PDF, para poder arquivar, partilhar e rever as jogadas e o estado final da frota.

## Acceptance Criteria
- Ao terminar uma simulacao completa, o sistema gera automaticamente `./output/summary.pdf`.
- O PDF inclui resultado final, contadores globais, estado final da frota e detalhe de cada jogada.
- Se a pasta `output/` nao existir, o sistema cria-a automaticamente.
- Se a geracao do PDF falhar, o jogo termina na mesma e apresenta uma mensagem de erro clara na consola.
- A funcionalidade fica coberta por um teste deterministico com conteudo verificavel.

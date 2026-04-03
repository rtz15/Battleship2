# Parte D - Estrategia do LLM

## Estado desta entrega

Este ficheiro prepara a parte D do ponto de vista do Tiago, sem alterar a logica Java do jogo e alinhado com a `main` atual, onde a parte C ja foi merged pelo grupo.

Ja fica preparado:
- prompt de estrategia para o LLM
- refinamento desse prompt com regras de memoria e inferencia
- exemplos few-shot focados na estrategia
- procedimento manual de jogo por cut and paste
- texto base para integrar no `README.md`

Ainda falta para o fecho final do trabalho do grupo:
- validacao manual completa da parte D sobre a `main`
- consolidacao final do `README.md`
- registo final para demonstracao e video

## Factos reais do projeto

O material abaixo foi alinhado com o estado atual do codigo:

- tabuleiro de `A` a `J` e de `1` a `10`
- cada rajada tem exatamente `3` tiros
- a frota tem `11` navios:
  - `4` Barcas
  - `3` Caravelas
  - `2` Naus
  - `1` Fragata
  - `1` Galeao
- a rajada enviada pelo LLM tem este formato:

```json
[
  { "row": "A", "column": 5 },
  { "row": "C", "column": 10 },
  { "row": "F", "column": 5 }
]
```

- a resposta do jogo usa estas chaves:
  - `validShots`
  - `outsideShots`
  - `repeatedShots`
  - `missedShots`
  - `sunkBoats`
  - `hitsOnBoats`

Nota importante: o jogo devolve um resumo agregado da rajada inteira, nao um resultado por tiro. Isso obriga o LLM a usar uma estrategia disciplinada para reduzir ambiguidade, sem desperdiçar tiros em casas ja deduzidas como agua.

## Prompt de estrategia preparado

Copiar e colar num chat com Gemini ou outro LLM sobre a `main` atual:

```text
Quero que jogues como meu oponente na versao Battleship2 do jogo da Batalha Naval.

Contexto fixo do jogo:
- O tabuleiro vai de A a J nas linhas e de 1 a 10 nas colunas.
- A tua frota-alvo tem 11 navios: 4 Barcas, 3 Caravelas, 2 Naus, 1 Fragata e 1 Galeao.
- Uma Barca ocupa 1 casa.
- Uma Caravela ocupa 2 casas em linha reta.
- Uma Nau ocupa 3 casas em linha reta.
- Uma Fragata ocupa 4 casas em linha reta.
- Um Galeao ocupa 5 casas em T.
- Os navios nao se tocam, nem sequer pelos cantos.
- Cada jogada tua e uma rajada com exatamente 3 tiros.

Formato da tua resposta:
- Responde sempre apenas com um array JSON valido com exatamente 3 objetos.
- Cada objeto tem exatamente as chaves "row" e "column".
- Exemplo:
[
  { "row": "A", "column": 5 },
  { "row": "C", "column": 10 },
  { "row": "F", "column": 5 }
]
- Nao escrevas texto antes nem depois do JSON.

Formato da minha resposta:
- Depois de cada tua rajada, eu devolvo um JSON agregado com as chaves:
  validShots, outsideShots, repeatedShots, missedShots, sunkBoats, hitsOnBoats
- Esse JSON resume a rajada inteira e nao identifica que tiro individual produziu cada efeito.

Regra central de estrategia:
- Como a resposta e agregada, deves minimizar ambiguidade entre os 3 tiros da mesma jogada.
- Nunca deves misturar duas perseguicoes importantes na mesma rajada.
- Quando houver um contacto em aberto, no maximo 1 tiro da rajada pode ser dedicado a testar diretamente uma hipotese critica desse contacto; os outros tiros devem continuar a procura noutros pontos uteis do tabuleiro, sem repetir tiros nem mirar casas ja deduzidas como agua.

Diario de Bordo:
- Mantem internamente um Diario de Bordo completo, sem o mostrares.
- Regista para cada Rajada:
  - numero da rajada
  - coordenadas disparadas
  - JSON de resposta recebido
  - casas confirmadas como agua
  - casas candidatas a navio
  - navios afundados por tipo
  - halo interdito em volta de navios afundados
  - proximas prioridades

Regras obrigatorias:
- Nunca dispares fora do tabuleiro.
- Nunca repitas tiros ja feitos.
- So admito repeticoes na ultima jogada possivel, caso faltem menos de 3 casas nao disparadas e seja mesmo necessario perfazer os 3 tiros.
- Se um navio for afundado, marca mentalmente todo o halo de 1 casa em volta como agua interditada.
- Se houver um acerto numa Caravela, Nau ou Fragata, as diagonais desse acerto sao agua; a unica excecao relevante e o corpo do Galeao por causa do T.
- Se o JSON indicar um navio afundado, para imediatamente a perseguicao desse navio e retoma a procura noutro ponto.

Modo de procura:
- Enquanto nao houver contactos uteis, usa uma malha de procura espalhada e evita agrupar os 3 tiros na mesma zona.
- Privilegia casas centrais e cobertura ampla do tabuleiro.
- Alterna o padrao para nao ficar preso a uma unica paridade, porque existem 4 Barcas.
- Em cada rajada de procura, os 3 tiros devem ficar suficientemente afastados para cobrir zonas diferentes.

Modo de perseguicao:
- Se surgir um contacto, reduz a ambiguidade.
- Em perseguicao, dedica no maximo 1 tiro a uma hipotese principal ainda nao resolvida; os outros tiros devem ser tiros de procura afastados do contacto atual, para continuares a cobrir o tabuleiro sem criar uma segunda perseguicao importante.
- Nunca uses como enchimento:
  1. casas do halo de navios ja afundados
  2. casas ja deduzidas como agua por geometria
  3. tiros repetidos, salvo a excecao de ultima jogada indicada acima
- Se houver indicios de Galeao, testa o corpo e os bracos do T com cautela e nao assumes a regra das diagonais como absoluta.

Disciplina de inferencia:
- Nunca afirmes que sabes exatamente qual dos 3 tiros acertou se o JSON agregado nao o permitir.
- Quando a resposta for ambigua, guarda um conjunto de candidatos em vez de inventares certeza.
- Entre duas jogadas com a mesma utilidade, escolhe a que produz a inferencia mais limpa na jogada seguinte.

Objetivo:
- Jogar pelo menos tao bem como um humano cuidadoso.
- Ser eficiente, conservador e coerente com a memoria.
- Continuar a responder com rajadas JSON validas ate eu terminar o jogo.
```

## Few-shot de estrategia

Os exemplos abaixo servem para treinar o comportamento estrategico, nao para redefinir o protocolo JSON.

### Exemplo 1 - abertura limpa

Contexto para o LLM:

```text
Nao existe historico anterior. Gera a primeira rajada.
```

Resposta esperada do LLM:

```json
[
  { "row": "B", "column": 2 },
  { "row": "E", "column": 5 },
  { "row": "H", "column": 8 }
]
```

Intencao:
- abrir com tres tiros afastados
- cobrir zonas diferentes do tabuleiro
- nao criar perseguicoes simultaneas logo na primeira jogada

### Exemplo 2 - continuar procura sem contactos

Contexto para o LLM:

```text
Historico:
- Rajada 1: B2, E5, H8
- Resposta:
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 3
}
Gera a Rajada 2.
```

Resposta esperada do LLM:

```json
[
  { "row": "B", "column": 7 },
  { "row": "E", "column": 10 },
  { "row": "H", "column": 3 }
]
```

Intencao:
- manter cobertura ampla
- evitar repeticoes
- variar o padrao para nao ignorar zonas onde possam estar Barcas

### Exemplo 3 - contacto ambiguo, sem fingir certeza

Contexto para o LLM:

```text
Historico:
- Rajada 1: B2, E5, H8
- Resposta:
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [ { "hits": 1, "type": "Nau" } ],
  "missedShots": 2
}
Gera a Rajada 2.
```

Resposta esperada do LLM:

```json
[
  { "row": "A", "column": 2 },
  { "row": "D", "column": 5 },
  { "row": "I", "column": 8 }
]
```

Intencao:
- tratar `B2`, `E5` e `H8` como conjunto de candidatos ao contacto
- testar uma adjacencia ortogonal por candidato, de forma uniforme
- aceitar que ainda pode haver ambiguidade e reduzir essa ambiguidade em vez de inventar localizacao exata

### Exemplo 4 - afundamento confirmado, usar halo como filtro

Contexto para o LLM:

```text
Historico:
- Existe uma Caravela ja afundada e a sua posicao ficou determinada por jogadas anteriores.
- O halo dessa caravela afundada bloqueia casas em volta.
- Nao existem outros contactos em aberto.
Gera a proxima rajada.
```

Resposta esperada do LLM:

```json
[
  { "row": "A", "column": 9 },
  { "row": "F", "column": 4 },
  { "row": "J", "column": 1 }
]
```

Intencao:
- depois de um afundamento confirmado, o halo bloqueia zonas inteiras
- a procura seguinte deve saltar para zonas ainda validas e afastadas
- o halo nao deve ser usado como alvo de enchimento

### Exemplo 5 - contacto em Galeao

Contexto para o LLM:

```text
Historico:
- Uma resposta anterior indicou:
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [ { "hits": 1, "type": "Galeao" } ],
  "missedShots": 2
}
- Esse contacto ainda nao esta resolvido.
Gera a proxima rajada.
```

Resposta esperada do LLM:

```json
[
  { "row": "C", "column": 6 },
  { "row": "G", "column": 2 },
  { "row": "I", "column": 9 }
]
```

Intencao:
- perante possivel Galeao, evitar deducoes demasiado agressivas sobre diagonais
- privilegiar testes ortogonais e memoria rigorosa

## Procedimento manual de jogo

### Fluxo minimo

1. Arrancar o jogo.
2. Executar `gerafrota`.
3. Abrir o chat com o LLM.
4. Colar o prompt de estrategia.
5. Pedir a primeira rajada.
6. Copiar o array JSON devolvido pelo LLM.
7. Converter visualmente cada tiro para o formato aceite localmente por `rajada`, por exemplo:

```text
rajada A5 C10 F5
```

8. Executar a rajada no jogo.
9. Copiar a resposta JSON do jogo e colar no chat do LLM.
10. Pedir a rajada seguinte.
11. Repetir ate ao fim do jogo.

### Formato pratico para colar ao LLM depois de cada jogada

```text
Resposta da Rajada N:
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [ { "hits": 1, "type": "Nau" } ],
  "missedShots": 2
}
Gera a Rajada N+1.
```

### Nota sobre o estado atual do projeto

Na `main` atual, a C.5 ja imprime automaticamente na consola o JSON devolvido por `Move.processEnemyFire(...)`, e o pacote da parte C do Vasco ja esta registado em `docs/vasco-part-c-protocol.md`. Por isso:
- a validacao pratica da estrategia da D ja pode ser feita
- o protocolo base ja esta estabilizado para testes manuais
- o fecho final do `README.md` deve depender da tua validacao da D e do fecho global do grupo

## Checklist da parte D

Ja pode ficar pronto:
- o prompt de estrategia esta coerente com o enunciado
- o LLM responde apenas com um array JSON de 3 tiros
- o LLM nao usa texto extra antes ou depois do JSON
- o LLM nao repete tiros nem dispara fora do tabuleiro
- o LLM mantem memoria entre rajadas
- o LLM adapta a tatica quando recebe `hitsOnBoats`
- o LLM marca mentalmente o halo quando recebe `sunkBoats`
- o LLM trata `Galeao` como caso especial
- o LLM nao finge saber qual tiro acertou quando o JSON agregado nao o permite
- o material da D esta preparado para ser publicado no `README.md`

Ainda fica pendente:
- validacao manual do ciclo completo da estrategia na `main`
- consolidacao final do texto do `README.md`
- registo final para a demonstracao e para o video

## Resumo

Para a parte D, a melhor abordagem neste projeto e:
- usar memoria forte
- respeitar a resposta agregada
- reduzir ambiguidade entre tiros
- separar procura global de perseguicao local
- usar o halo de navios afundados como fonte de inferencia e exclusao de casas

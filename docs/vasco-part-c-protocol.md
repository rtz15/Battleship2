# Parte C — Pacote Final de Prompts do Protocolo (Vasco)

## 1) Prompt inicial (jogo, tabuleiro e frota)

Usa este prompt inicial para contextualizar o LLM antes de qualquer jogada:

```text
Estás a jogar Battleship2.

Regras operacionais:
- O tabuleiro é 10x10.
- As linhas são letras A..J.
- As colunas são números 1..10.
- Cada jogada (rajada) tem exatamente 3 tiros.
- O comando lógico de tiro é "rajada" com 3 posições.

Objetivo:
- Enviar rajadas válidas em JSON.
- Interpretar corretamente a resposta JSON do jogo para decidir a próxima rajada.

Formato esperado da rajada JSON:
- Lista com 3 objetos.
- Cada objeto tem:
  - "row": letra (A..J)
  - "column": número (1..10)

Responde apenas com JSON válido quando te for pedido para disparar.
```

## 2) Prompts de validação do Galeão (Norte, Sul, Este, Oeste)

### Galeão a Norte
```text
Valida o cenário: Galeão orientado a Norte.
Gera uma rajada JSON com 3 tiros adjacentes ao eixo provável do navio (sem repetir coordenadas).
```

### Galeão a Sul
```text
Valida o cenário: Galeão orientado a Sul.
Gera uma rajada JSON com 3 tiros que teste extensão vertical no sentido Sul (sem repetir coordenadas).
```

### Galeão a Este
```text
Valida o cenário: Galeão orientado a Este.
Gera uma rajada JSON com 3 tiros que teste extensão horizontal no sentido Este (sem repetir coordenadas).
```

### Galeão a Oeste
```text
Valida o cenário: Galeão orientado a Oeste.
Gera uma rajada JSON com 3 tiros que teste extensão horizontal no sentido Oeste (sem repetir coordenadas).
```

## 3) Few-shot para o formato da rajada JSON

### Exemplo 1
```json
[
  { "row": "A", "column": 1 },
  { "row": "B", "column": 2 },
  { "row": "C", "column": 3 }
]
```

### Exemplo 2
```json
[
  { "row": "J", "column": 10 },
  { "row": "J", "column": 9 },
  { "row": "I", "column": 10 }
]
```

### Exemplo 3
```json
[
  { "row": "D", "column": 4 },
  { "row": "D", "column": 5 },
  { "row": "D", "column": 6 }
]
```

## 4) Few-shot para o formato da resposta JSON

As respostas do jogo usam as chaves atuais:
- `validShots`
- `outsideShots`
- `repeatedShots`
- `missedShots`
- `sunkBoats` (lista de objetos com `type` e `count`)
- `hitsOnBoats` (lista de objetos com `type` e `hits`)

### Exemplo 1 (só água)
```json
{
  "validShots": 3,
  "outsideShots": 0,
  "repeatedShots": 0,
  "missedShots": 3,
  "sunkBoats": [],
  "hitsOnBoats": []
}
```

### Exemplo 2 (com acertos e afundamento)
```json
{
  "validShots": 3,
  "outsideShots": 0,
  "repeatedShots": 0,
  "missedShots": 1,
  "sunkBoats": [
    { "type": "Galeao", "count": 1 }
  ],
  "hitsOnBoats": [
    { "type": "Fragata", "hits": 1 }
  ]
}
```

### Exemplo 3 (repetidos e fora)
```json
{
  "validShots": 1,
  "outsideShots": 1,
  "repeatedShots": 1,
  "missedShots": 1,
  "sunkBoats": [],
  "hitsOnBoats": []
}
```

## 5) Procedimento manual passo a passo

1. Atualizar e garantir base em `main`.
2. Correr a aplicação.
3. Executar `gerafrota`.
4. Executar `rajada A1 B2 C3`.
5. Confirmar saída textual da jogada (resumo humano).
6. Confirmar JSON de resposta imediatamente a seguir.
7. Repetir com novas rajadas para observar casos com água, acerto, repetição e tiro exterior.
8. Confirmar que o LLM usa o JSON de resposta para ajustar a próxima rajada.

## 6) Checklist curta da Parte C

- [ ] Prompt inicial do jogo/tabuleiro/frota definido.
- [ ] Prompts de validação do Galeão (N/S/E/O) definidos.
- [ ] Few-shot de rajada JSON incluído.
- [ ] Few-shot de resposta JSON incluído.
- [ ] Procedimento manual documentado.
- [ ] Validação manual feita na `main`.

## 7) Nota de alinhamento com C.5

Validação da Parte C realizada com a C.5 já merged na `main` (issue #8 fechada), incluindo impressão do resumo textual e JSON de resposta após cada `rajada`.

# Battleship2

Battleship2 is a Java 21 command-line implementation of Battleship developed for the Software Engineering coursework. The current `main` branch already includes the Ficha 2 gameplay extensions, the Ficha 3 quality-analysis artifacts, and the Ficha 4 JUnit 6 testing phase.

> Ficha 2 demo video: [Watch on YouTube](https://youtu.be/S00AQphHRkE)

## Team
- `110894` Eduardo Sousa
- `111331` Vasco Rodrigues
- `123026` Tiago Pereira

## Implemented features
- random fleet generation with `gerafrota`
- manual fleet creation with `lefrota`
- burst fire with exactly three shots per move
- fleet status and board rendering
- move result summaries in JSON
- PDF export of the final simulation summary
- H2 persistence of finished games
- Portuguese and English message bundles

## Technology stack
- Java 21
- Maven
- JUnit 6
- JaCoCo
- Jackson
- H2
- OpenPDF
- ICU4J
- GitHub Actions

## Run locally

Prerequisites:
- JDK 21
- Maven 3.9+

Build the executable jar:

```bash
mvn clean package
```

Run the game:

```bash
java -jar target/BattleshipGamePlayer-2.0.jar
```

Run the game in English:

```bash
java -jar target/BattleshipGamePlayer-2.0.jar --lang en
```

Available commands in the CLI:
- `ajuda`
- `gerafrota`
- `lefrota`
- `estado`
- `mapa`
- `rajada`
- `simula`
- `tiros`
- `historico`
- `desisto`

## Testing and coverage

Run the full test suite:

```bash
mvn test
```

Run the full verification pipeline with coverage and Javadoc:

```bash
mvn clean verify javadoc:javadoc
```

JaCoCo generates the local report at:

```text
target/site/jacoco/index.html
```

Versioned Ficha 4 reports committed to the repository:
- `reports/eduardosousa13`
- `reports/IGE-111331`
- `reports/rtz15`

## Continuous integration

The workflow in [`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on pushes to `main` and on every `pull_request` targeting `main`. It provisions JDK 21 and executes:

```bash
mvn -B clean verify javadoc:javadoc --file pom.xml
```

## Ficha 5 acceptance testing

Target web application:
- https://papergames.io/en/battleship

### Product Backlog

1. Como visitante, quero ver as opcoes principais de entrada no jogo para escolher entre jogar com um amigo, jogar contra o robot, criar um torneio ou jogar online.
2. Como visitante, quero consultar as regras do jogo antes de iniciar uma partida para perceber o objetivo, os turnos, os acertos e as falhas.
3. Como visitante, quero consultar a explicacao das armas disponiveis para perceber as diferencas entre misseis simples, grandes, chuva de misseis e missil nuclear.
4. Como jogador, quero criar um nickname antes de entrar numa partida para ser identificado no jogo.
5. Como jogador, quero iniciar uma partida contra um robot para poder jogar sem depender de outro jogador.
6. Como jogador, quero ver o estado inicial da partida depois de entrar no jogo para confirmar que tenho os meus barcos e que posso atacar o adversario.
7. Como jogador, quero criar ou usar um link de convite para jogar com outro colega.
8. Como jogador, quero consultar rankings ou leaderboards para comparar resultados com outros jogadores.
9. Como jogador, quero aceder a torneios para organizar ou participar em competicoes.
10. Como jogador, quero poder abandonar uma partida iniciada quando nao pretendo continuar.

Ficha 5 suites:
- `onboarding-robot-game`: Eduardo `110894`
- `shared-link-game`: Vasco `111331`
- `session-controls-and-rankings`: Tiago `123026`

A suite do Eduardo cobre quatro cenarios de onboarding do backlog: entrada no jogo, consulta de regras/armas, nickname e inicio de partida contra robot. Existem duas implementacoes:
- testes Selenium WebDriver diretos em `ficha5.eduardo.selenium`
- testes Selenide + Allure em `ficha5.eduardo`

A suite do Tiago cobre quatro cenarios focados no estado inicial da partida e nas areas de sessao visiveis na landing page: rankings, torneios, estado inicial contra robot e abandono de partida. Existem duas implementacoes:
- testes Selenium WebDriver diretos em `ficha5.tiago.selenium`
- testes Selenide + Allure em `ficha5.tiago`

## Project artifacts

- Code metrics: [CodeMetrics/README.md](CodeMetrics/README.md)
- SonarQube artifacts: [SonarQube/README.md](SonarQube/README.md)
- API documentation: [GitHub Pages Javadoc](https://rtz15.github.io/Battleship2/)
- LLM strategy notes: [docs/tiago-part-d-strategy.md](docs/tiago-part-d-strategy.md)

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

The workflow in [`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on every `push` and on every `pull_request` targeting `main`. It provisions JDK 21 and executes:

```bash
mvn -B clean verify javadoc:javadoc --file pom.xml
```

## Project artifacts

- Code metrics: [CodeMetrics/README.md](CodeMetrics/README.md)
- SonarQube artifacts: [SonarQube/README.md](SonarQube/README.md)
- API documentation: [GitHub Pages Javadoc](https://rtz15.github.io/Battleship2/)
- LLM strategy notes: [docs/tiago-part-d-strategy.md](docs/tiago-part-d-strategy.md)

# Ficha 6 - Docker Compose Runtime Notes (111331)

## Scope

This document records the local Docker Compose runtime work for member `111331`.

The implementation is limited to:

- `docker-compose.yml`
- `docs/ficha6-vasco-runtime-notes.md`

It depends on the Docker image definition already prepared in `Dockerfile` by the `123026` task.

## Runtime model

The Compose service builds and runs the Battleship2 Java CLI application from the project `Dockerfile`.

The service uses:

- image tag: `battleship-game:latest`
- service name: `battleship`
- working directory inside the container: `/app/runtime`
- bind mount: `./data:/app/runtime`
- interactive flags: `stdin_open: true` and `tty: true`

The container still executes the JAR from `/app/battleship-game.jar`, because the `Dockerfile` uses an absolute `ENTRYPOINT`.

## Persistence

The application stores runtime artifacts relative to the Java process working directory.

Relevant application paths:

- H2 database URL: `jdbc:h2:./battleship_history`
- PDF output path: `output/summary.pdf`

Because Compose sets `working_dir: /app/runtime` and mounts `./data` there:

- H2 files are persisted as `data/battleship_history.*`
- the generated PDF is persisted as `data/output/summary.pdf`

This keeps runtime data outside the immutable image and avoids changes to the `Dockerfile`.

## Local commands

Build the Maven artifact first. The Docker image expects the shaded JAR at `target/BattleshipGamePlayer-2.0.jar`.

```powershell
mvn clean package
```

Build the Docker image through Compose:

```powershell
docker compose build
```

Run the application interactively in Portuguese, the default language:

```powershell
docker compose run --rm battleship
```

Run the application interactively in English:

```powershell
docker compose run --rm battleship --lang=en
```

Stop and remove Compose resources after use:

```powershell
docker compose down
```

## Validation notes

Static validation performed:

- confirmed that `Dockerfile` keeps the JAR at `/app/battleship-game.jar`
- confirmed that `GameHistory` uses `jdbc:h2:./battleship_history`
- confirmed that `PdfExporter` writes to `output/summary.pdf`
- confirmed that the professor WordPress/MySQL Compose file was not copied into the project solution

Local runtime validation is still pending in this workspace because:

- WSL package `Microsoft.WSL` is installed, but WSL2 cannot start yet
- Windows reports firmware virtualization as disabled
- enabling Windows optional features requires an elevated administrator shell
- the `docker` command is not available in PowerShell

Required validation after Docker Desktop is installed:

```powershell
wsl --version
wsl --status
docker --version
docker compose version
docker info
docker run hello-world
mvn clean package
docker compose build
docker compose run --rm battleship
docker compose run --rm battleship --lang=en
docker compose down
```

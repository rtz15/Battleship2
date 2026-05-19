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

Local runtime validation performed on 2026-05-19 after reboot:

```powershell
Get-CimInstance Win32_Processor | Select-Object VirtualizationFirmwareEnabled
```

Result: `VirtualizationFirmwareEnabled` still reported `False`.

```powershell
wsl --version
wsl --status
```

Result:

- WSL version: `2.7.3.0`
- kernel version: `6.6.114.1-1`
- default WSL version: `2`
- Windows version: `10.0.26200.8457`

```powershell
docker --version
docker compose version
docker info
docker run hello-world
```

Result:

- Docker client version: `29.4.3`
- Docker Compose version: `v5.1.3`
- Docker server version: `29.4.3`
- Docker context: `desktop-linux`
- Docker Desktop backend: `docker-desktop`
- Docker kernel: `6.6.114.1-microsoft-standard-WSL2`
- `docker run hello-world` completed successfully

```powershell
mvn clean package
```

Result: build success with `216` tests, `0` failures, `0` errors, and `0` skipped tests.

```powershell
docker compose build
```

Result: build success for image `battleship-game:latest`.

The application is interactive. In this automated validation shell, running without stdin displayed the Portuguese menu and then ended with `NoSuchElementException` at the prompt. To validate the runtime deterministically, the exit command was piped with TTY disabled:

```powershell
"desisto" | docker compose run --rm -T battleship
"desisto" | docker compose run --rm -T battleship --lang=en
```

Result:

- Portuguese runtime displayed the menu and exited with `Bons ventos!`
- English runtime displayed the menu and exited with `Fair winds!`

```powershell
docker compose down
```

Result: Compose network `battleship2_default` was removed successfully.

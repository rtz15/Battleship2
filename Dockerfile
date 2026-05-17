FROM eclipse-temurin:21-jre

LABEL org.opencontainers.image.title="Battleship2"
LABEL org.opencontainers.image.description="Container image for the Battleship2 Java CLI application"
LABEL org.opencontainers.image.source="https://github.com/rtz15/Battleship2"

WORKDIR /app

RUN useradd --create-home --uid 10001 --shell /usr/sbin/nologin battleship \
    && mkdir -p /app/output \
    && chown -R battleship:battleship /app

COPY target/BattleshipGamePlayer-2.0.jar /app/battleship-game.jar

USER battleship

ENTRYPOINT ["java", "-jar", "/app/battleship-game.jar"]

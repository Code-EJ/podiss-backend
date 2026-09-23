# Créditos: oEnzoRibas
# H2 contract tests and compilation run under Java 21 during the image build.
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -Dapp.cors.allowed-origins=http://localhost:5173 package

FROM eclipse-temurin:21-jre-jammy AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 podiss && useradd --uid 10001 --gid 10001 --no-create-home podiss
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/*.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
    CMD curl --fail --silent --output /dev/null 'http://127.0.0.1:8080/episodes?size=1' || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

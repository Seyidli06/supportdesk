FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn \
    --batch-mode \
    --no-transfer-progress \
    -Dmaven.test.skip=true \
    -Dmaven.wagon.http.retryHandler.count=5 \
    clean package

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install \
        --yes \
        --no-install-recommends \
        curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system supportdesk \
    && useradd \
        --system \
        --gid supportdesk \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        supportdesk

WORKDIR /app

COPY \
    --from=build \
    /workspace/target/supportdesk-1.0.0.jar \
    /app/app.jar

RUN chown --recursive supportdesk:supportdesk /app

USER supportdesk:supportdesk

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
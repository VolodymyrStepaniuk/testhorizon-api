# syntax=docker/dockerfile:experimental
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/testhorizon

COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY src src
COPY libs libs
COPY lombok.config lombok.config

RUN chmod +x gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew clean build -x test

# Prepare the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG APP=/workspace/testhorizon
ARG EXTRACTED=${APP}/build/extracted

COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
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
RUN ls -R /workspace/testhorizon/build/libs/

# Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /workspace/testhorizon/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
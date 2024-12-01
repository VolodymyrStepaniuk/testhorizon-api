# syntax=docker/dockerfile:experimental
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/all

COPY ./gradle /workspace/all/gradle
COPY ./gradlew /workspace/all/gradlew
COPY ./settings.gradle /workspace/all/settings.gradle
COPY ./projects /workspace/all/projects
COPY ./libs /workspace/all/libs
COPY ./lombok.config /workspace/all/lombok.config
RUN --mount=type=cache,target=/root/.gradle ./gradlew :testhorizon:clean :testhorizon:build -x :testhorizon:test

WORKDIR /workspace/all/testhorizon
RUN mkdir -p target/extracted
#RUN java -Djarmode=layertools -jar build/libs/*-all.jar extract --destination target/extracted
#RUN apk add openssl
#WORKDIR /workspace/all/projects/workshop/target/extracted/application/BOOT-INF/classes/certs
#RUN openssl genrsa -out keypair.pem 2048
#RUN openssl rsa -in keypair.pem -pubout -out public.pem
#RUN openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG APP=/workspace/all/testhorizon
ARG EXTRACTED=${APP}/target/extracted
COPY --from=build ${APP}/start-app.sh ./
COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./
RUN chmod +x ./start-app.sh
ENTRYPOINT ["./start-app.sh"]
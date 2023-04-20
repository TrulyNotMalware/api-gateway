#Dockerfile to builds loggers.
FROM eclipse-temurin:11.0.15_10-jdk as builder
ARG JAR_FILE_NAME=api-gateway-0.0.1-SNAPSHOT
ARG CONFIG_FILE_NAME=application.yaml
ARG GATEWAY_SERVER_PORT=80

RUN apt update && apt install curl -y
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew build
#RUN ./gradlew jibDockerBuild

FROM eclipse-temurin:11.0.15_10-jdk
COPY --from=builder /build/libs/${JAR_FILE_NAME}.jar /app.jar
#COPY build/libs/*.jar app.jar
ENV JAVA_OPTS=""
EXPOSE ${GATEWAY_SERVER_PORT}
ENTRYPOINT exec java -jar -Dspring.config.location=classpath:/${CONFIG_FILE_NAME} /app.jar
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY build/libs/* ./Bot.jar
COPY run/* ./

ENTRYPOINT [ "java", "-jar", "Bot.jar" ]
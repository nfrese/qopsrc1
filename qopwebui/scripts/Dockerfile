FROM openjdk:8-jdk-alpine

WORKDIR /app
COPY app.jar app.jar
COPY lib lib/

ENTRYPOINT ["java", "-cp", "app.jar:lib/*", "at.qop.qopwebui.QopApplication"]

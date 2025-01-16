FROM alpine/git
WORKDIR /build
RUN git clone -b vaadin8 https://github.com/nfrese/qopsrc1.git 

FROM maven:3.5-jdk-8-alpine AS build
WORKDIR /build
COPY --from=0 /build/qopsrc1 /build 
RUN mvn install -DskipTests

FROM openjdk:8-jdk-alpine
ARG JAR_FILE=qopwebui/target/*.jar
COPY --from=build /build/${JAR_FILE} app.jar
# COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

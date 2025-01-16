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

#USER root
#
#RUN yum -y install epel-release
#RUN yum -y install https://download.postgresql.org/pub/repos/yum/11/redhat/rhel-7-x86_64/pgdg-centos11-11-2.noarch.rpm
#RUN yum -y install postgis25_11
#RUN yum -y install postgis25_11-client
#RUN yum -y install postgis25_11-utils
#RUN yum -y install zip
#RUN yum -y install gdal
#RUN psql -V
#
#ENV PATH="/usr/pgsql-11/bin:${PATH}"
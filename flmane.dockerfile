FROM tomcat:9.0.82-jdk11
MAINTAINER Paulo Sobreira
WORKDIR /usr/local/tomcat/webapps
RUN  rm -rf *
ADD target/flmane.war /usr/local/tomcat/webapps/flmane.war
EXPOSE 8080
FROM tomcat:9.0.82-jdk11
MAINTAINER Paulo Sobreira
WORKDIR /usr/local/tomcat/webapps
RUN  rm -rf *
ADD target/f1mane-full.war /usr/local/tomcat/webapps/f1mane.war
EXPOSE 8080
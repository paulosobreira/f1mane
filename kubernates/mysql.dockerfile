FROM mysql:5.5
MAINTAINER Paulo Sobreira
ENV MYSQL_ALLOW_EMPTY_PASSWORD=yes
EXPOSE 3306
ADD databases.sql /docker-entrypoint-initdb.d/databases.sql
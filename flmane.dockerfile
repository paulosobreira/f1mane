FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY target/flmane.jar app.jar

ENV FLMANE_IMAGENS_HEADLESS_DIR=/app/imagens-headless
RUN java -Djava.awt.headless=true -jar app.jar --pre-gerar-imagens

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar","--headless"]

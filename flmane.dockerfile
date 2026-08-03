FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY target/flmane.jar app.jar

ENV FLMANE_IMAGENS_HEADLESS_DIR=/app/imagens-headless
RUN java -Djava.awt.headless=true -jar app.jar --pre-gerar-imagens

EXPOSE 8080

# -Djava.awt.headless=true: o servidor so usa Java2D para gerar imagens em
#   BufferedImage; sem a flag a JVM ainda inicializa o toolkit grafico nativo
#   (fontconfig/X11) num processo que nunca abre janela.
# -XX:MaxRAMPercentage=75: dimensiona o heap pelo limite de memoria do container
#   (ver mem_limit no docker-compose.yaml) em vez do default de 1/4 da RAM do
#   host, que num host grande faz o G1 reservar/commitar heap muito alem do que
#   a corrida usa.
ENTRYPOINT ["java","-Djava.awt.headless=true","-XX:MaxRAMPercentage=75","-jar","app.jar","--headless"]

ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-history-service/ /app/ws-history-service/

WORKDIR /app/ws-history-service/
RUN mvn clean compile
CMD mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80
EXPOSE 80



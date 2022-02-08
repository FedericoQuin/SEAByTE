ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-recommendation-service/ /app/ws-recommendation-service/

WORKDIR /app/ws-recommendation-service/
RUN mvn clean compile
CMD mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80
EXPOSE 80



ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-inventory-service/ /app/ws-inventory-service/

WORKDIR /app/ws-inventory-service/
RUN mvn clean compile
CMD mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80
EXPOSE 80



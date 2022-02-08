ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-basket-service/ /app/ws-basket-service/

WORKDIR /app/ws-basket-service/
RUN mvn clean compile
CMD mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80
EXPOSE 80



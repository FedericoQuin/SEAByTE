ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-user-service/ /app/ws-user-service/

WORKDIR /app/ws-user-service/
RUN mvn clean compile
CMD mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80
EXPOSE 80



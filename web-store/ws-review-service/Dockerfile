ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-review-service/ /app/ws-review-service/

WORKDIR /app/ws-review-service/
RUN mvn clean compile
CMD mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80
EXPOSE 80



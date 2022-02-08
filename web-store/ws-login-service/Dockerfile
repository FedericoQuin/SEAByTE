ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-login-service/ /app/ws-login-service/

WORKDIR /app/ws-login-service/
RUN mvn clean compile
CMD mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80
EXPOSE 80



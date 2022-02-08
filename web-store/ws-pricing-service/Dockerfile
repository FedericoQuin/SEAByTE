ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-pricing-service/ /app/ws-pricing-service/

WORKDIR /app/ws-pricing-service/
RUN mvn clean compile
CMD mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80
EXPOSE 80



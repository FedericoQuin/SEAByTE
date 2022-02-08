ARG VERSION=latest
FROM main-dependency-image:${VERSION}

ADD ./ws-registration-service/ /app/ws-registration-service/

WORKDIR /app/ws-registration-service/
RUN mvn clean compile
CMD mvn spring-boot:run



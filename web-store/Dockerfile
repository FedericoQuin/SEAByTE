# Create main image to avoid downloading maven dependencies separately for all submodules
FROM maven:3.8.6-openjdk-18
ADD ./pom.xml /app/pom.xml
WORKDIR /app/
RUN mvn dependency:go-offline dependency:resolve-plugins dependency:resolve
# Somehow the above does not download everything necessary yet, 
#  so some dependencies are still downloaded individually in each container image
#  --> fixed by explicitly downloading the resources and compiler plugins in the main POM


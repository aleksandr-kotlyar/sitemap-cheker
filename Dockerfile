FROM maven:latest
COPY pom.xml pom.xml
RUN mvn clean
RUN mvn install
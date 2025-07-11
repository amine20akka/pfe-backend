# 1. Build stage
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY drawing-module drawing-module

RUN ./mvnw -f pom.xml dependency:go-offline -B

RUN ./mvnw -f pom.xml clean package -pl drawing-module -am -DskipTests

# 2. Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/drawing-module/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]

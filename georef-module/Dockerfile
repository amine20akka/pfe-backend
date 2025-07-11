# 1. Build stage
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY georef-module georef-module

RUN chmod +x ./mvnw

# Commented out to avoid downloading dependencies every time
# RUN ./mvnw -f pom.xml dependency:go-offline -B

RUN ./mvnw -f pom.xml clean package -pl georef-module -am -DskipTests

# 2. Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/georef-module/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

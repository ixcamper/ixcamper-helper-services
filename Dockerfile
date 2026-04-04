# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy POMs to cache dependencies
COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY gateway-service/pom.xml gateway-service/

RUN mvn dependency:go-offline -B

# Copy source and build
COPY . .
# Build common first, then auth-service
RUN mvn clean install -pl common -DskipTests && \
    mvn clean package -pl auth-service -am -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
# Clean is vital here to remove any half-baked 'thin' jars
RUN mvn clean install -pl common -DskipTests
RUN mvn clean package -pl gateway-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Grab the fat jar (usually the largest one in target)
COPY --from=build /app/gateway-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
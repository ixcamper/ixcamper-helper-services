FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy everything from root (Build Context must be project root)
COPY . .

# 1. Install common so gateway can see shared security/dtos
RUN mvn clean install -pl common -DskipTests

# 2. Build gateway
RUN mvn clean package -pl gateway-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# 3. Ensure we grab the jar from the correct sub-module target
COPY --from=build /app/gateway-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
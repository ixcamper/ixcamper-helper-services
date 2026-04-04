# --- Stage 1: Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copy the parent POM and all module POMs first to cache dependencies
COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY gateway-service/pom.xml gateway-service/
# Add any other modules here (e.g., config-server/pom.xml)

# 2. Download dependencies (this layer is cached unless a POM changes)
RUN mvn dependency:go-offline -B

# 3. Copy the actual source code for all modules
COPY . .

# 4. Build the specific service and its dependencies
# -pl (project list) specifies the target module
# -am (also make) builds the dependencies like 'common' automatically
RUN mvn clean package -pl auth-service -am -DskipTests

# --- Stage 2: Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 5. Copy the JAR from the build stage
# (Adjust path if your target module name differs)
COPY --from=build /app/auth-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
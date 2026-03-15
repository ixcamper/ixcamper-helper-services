# Microservices Security & Identity System

A professional-grade microservices architecture featuring a centralized security backbone, shared library logic, and standardized communication protocols. Built with **Spring Boot 3**, **Spring Cloud Gateway**, and **Angular 21**.

## 🏗 System Architecture

The project follows a **Shared-Kernel** and **API Gateway** pattern. All requests are routed through the Gateway, which acts as the single point of entry and identity validator.

* **gateway-service**: A reactive (WebFlux) entry point that validates JWTs and routes requests.
* **auth-service**: Handles user registration, login, and secure token generation.
* **common module**: A shared Maven dependency containing DTOs, Exceptions, and JWT utilities used by all services.
* **frontend**: An Angular 21 application utilizing standalone components and Jest for testing.

---

## 🚀 Key Features

* **Centralized Configuration**: Shared properties like `jwt.secret` are managed in a single `application-common.yml` located in the common module.
* **Unified Error Handling**: Every service returns a consistent `ErrorResponse` JSON for 400, 401, 403, and 500 status codes.
* **Request Mutation**: The Gateway injects a `loggedInUser` header into downstream requests upon successful authentication.
* **Security Integration**: Implements JJWT for token management and utilizes HashiCorp Vault for secret handling.

---

## 🛠 Setup & Installation

### 1. Environment Variables
To keep secrets out of the source code, you must set the following environment variable in your IDE Run Configuration (IntelliJ/VS Code) or your shell:

```
export JWT_SECRET="your-base64-encoded-64-character-secret-key"
```

### 2. Build the Project
Since the services depend on the common module, you must install it to your local Maven repository first:

```
# Run from the root directory
mvn clean install -DskipTests
```

### 3. Running Services
Start the services in the following order:
1. Common Module (Build only)
2. Auth-Service (Default Port: 8081)
3. Gateway-Service (Default Port: 8080)

---

## 📁 Project Structure
```
├── auth-service/       # Auth logic & Neon PostgreSQL integration
├── gateway-service/    # Spring Cloud Gateway & Netty
├── common/             # Shared kernel (DTOs, Utils, Exceptions)
│   └── src/main/resources/application-common.yml
├── frontend/           # Angular 21 (Vercel deployment)
└── pom.xml             # Parent Multi-module POM
```

## ⚠️ Troubleshooting
MacOS Native Resolver (Netty)
If you see `UnsatisfiedLinkError` regarding `MacOSDnsServerAddressStreamProvider` on Apple Silicon or Intel Macs:
* Ensure the `netty-resolver-dns-native-macos` dependency is present in the Gateway `pom.xml`.
* Use the classifier `<classifier>osx-aarch_64</classifier>` for M1/M2/M3 chips.

Missing Placeholders
If the app fails with `Could not resolve placeholder 'JWT_SECRET'`:
* Verify the variable is set in the Run Configuration of the specific service you are starting.
* If using terminal, ensure you `export` the variable in the same session you use to run the Maven command.

Dependency Issues
If the Auth Service cannot find a class or property recently added to the Common module:
* Run `mvn clean install` from the root to refresh the local `.m2` repository and update the shared JAR file.

## Testing
To test your microservices through the Gateway (Port 8080), you can use these curl commands. They are designed to verify that your AuthenticationFilter and JwtUtil are working correctly.

### 1. Register a New User
First, create a user in your database via the Auth Service (routed through the Gateway).
```
curl --location 'http://localhost:8080/auth/register' \
--header 'Content-Type: application/json' \
--data '{
    "username": "suhas_dev",
    "password": "securePassword123",
    "email": "suhas@example.com"
}'
```

### 2. Login (Generate JWT)
This will return the token you need for all other requests.

```Bash
curl --location 'http://localhost:8080/auth/login' \
--header 'Content-Type: application/json' \
--data '{
    "username": "suhas_dev",
    "password": "securePassword123"
}'
```

### 3. Test Gateway Security (Missing Token)
Try to access a secured endpoint without a header. You should see your standardized ErrorResponse from the onError method.

```Bash
curl --location 'http://localhost:8080/api/secure-endpoint'
```

Expected JSON Response:
```JSON
{
    "timestamp": "2026-03-15T...",
    "status": 401,
    "error": "Unauthorized",
    "message": "Missing Authorization Header",
    "path": "/api/secure-endpoint"
}
```

### 4. Test Access with Token
Replace YOUR_TOKEN_HERE with the token from Step 2.

```Bash
curl --location 'http://localhost:8080/api/secure-endpoint' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

### 5. Verify the loggedInUser Header
If you want to verify that the Gateway is correctly injecting the header for downstream services, you can create a simple "test" endpoint in any microservice:

In your Controller:
```Java
@GetMapping("/test-header")
public ResponseEntity<String> testHeader(@RequestHeader("loggedInUser") String user) {
    return ResponseEntity.ok("Gateway injected user: " + user);
}
```
The Test Command:
```Bash
curl --location 'http://localhost:8080/any-service/test-header' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# Multi-stage Dockerfile for Secure REST API (Spring Boot 3.3.5 / Java 21)

# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy Maven wrapper and POM first to leverage Docker layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn/
RUN chmod +x ./mvnw

# Copy source code and build runnable JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Hardened Runtime Container
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Security: Run as dedicated non-root user (OWASP Security Misconfiguration protection)
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

COPY --from=builder --chown=appuser:appgroup /app/target/lab1-1.0.0.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

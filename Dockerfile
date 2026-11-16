# Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn/
RUN chmod +x ./mvnw

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

COPY --from=builder --chown=appuser:appgroup /app/target/lab1-1.0.0.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# ==============================================================================
# Stage 1: Build Spring Boot JAR with Eclipse Temurin JDK 25
# ==============================================================================
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

# Copy Maven wrapper and POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure wrapper is executable
RUN chmod +x ./mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy application source
COPY src/ src/

# Build production executable JAR
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# Stage 2: Production JRE 25 Runtime Image
# ==============================================================================
FROM eclipse-temurin:25-jre-alpine AS runtime

# Create unprivileged application user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy executable jar from build stage
COPY --from=build /workspace/target/*.jar app.jar
RUN chown -R spring:spring /app

# Run as non-root user
USER spring:spring

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck probe using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

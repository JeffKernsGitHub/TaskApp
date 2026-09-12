# ==============================================================================
# Stage 1: Build Spring Boot JAR with Eclipse Temurin JDK 25
# ==============================================================================
FROM docker.io/library/eclipse-temurin:25-jdk-alpine AS build

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
# Stage 2: Generate Custom Minimal Server JRE with jlink
# ==============================================================================
FROM docker.io/library/eclipse-temurin:25-jdk-alpine AS jlink

WORKDIR /workspace

# Copy executable jar from build stage
COPY --from=build /workspace/target/*.jar app.jar

# Unpack JAR, run jdeps to analyze dependencies, append dynamic/runtime modules,
# and build a stripped custom Server JRE.
RUN jar xf app.jar && \
    DETECTED_MODULES=$(jdeps \
      --ignore-missing-deps \
      --multi-release 25 \
      --print-module-deps \
      --class-path 'BOOT-INF/lib/*' \
      app.jar) && \
    EXTRA_MODULES="java.desktop,java.instrument,java.management,java.naming,java.security.jgss,java.security.sasl,java.sql,java.sql.rowset,jdk.crypto.ec,jdk.jfr,jdk.unsupported" && \
    MODULES="${DETECTED_MODULES},${EXTRA_MODULES}" && \
    jlink \
      --add-modules "${MODULES}" \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=zip-6 \
      --output /custom-jre

# ==============================================================================
# Stage 3: Minimal Production Runtime Image
# ==============================================================================
FROM docker.io/library/alpine:3.21 AS runtime

# Install essential runtime dependencies:
# - tzdata: timezone data
# - ca-certificates: TLS certificate validation
# - libstdc++: C++ runtime needed by HotSpot JVM
RUN apk add --no-cache tzdata ca-certificates libstdc++ && \
    addgroup -S spring && adduser -S spring -G spring

ENV JAVA_HOME=/opt/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /app

# Copy custom JRE from jlink stage and executable jar from build stage
COPY --from=jlink /custom-jre ${JAVA_HOME}
COPY --from=build /workspace/target/*.jar app.jar

RUN chown -R spring:spring /app

# Run as unprivileged user
USER spring:spring

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck probe using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]


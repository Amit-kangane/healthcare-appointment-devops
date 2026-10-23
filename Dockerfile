# ==============================================================================
# MULTI-STAGE DOCKERFILE FOR HEALTHCARE APPOINTMENT MANAGEMENT SYSTEM
# Base: Eclipse Temurin OpenJDK 21 (Long-Term Support)
# Security: Runs under an unprivileged non-root user (appuser:appgroup)
# ==============================================================================

# ------------------------------------------------------------------------------
# STAGE 1: Build & Package Stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copy Maven Wrapper & configuration files first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Pre-fetch project dependencies offline
RUN ./mvnw dependency:go-offline -B

# Copy application source code
COPY src ./src

# Compile, run automated tests, and package executable fat JAR
RUN ./mvnw clean package -B

# ------------------------------------------------------------------------------
# STAGE 2: Lightweight, Secure Production Runtime Stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Metadata labels
LABEL maintainer="DevOps Engineering Team" \
      project="Healthcare Appointment Management System" \
      version="1.0.0"

# Install curl/wget for container health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Create a dedicated, unprivileged system group and user
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/bash -m appuser

WORKDIR /app

# Copy the compiled JAR artifact from the builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/healthcare-appointment-devops-1.0.0.jar app.jar

# Enforce non-root user ownership and execution permissions
RUN chown -R appuser:appgroup /app && \
    chmod 755 /app/app.jar

# Switch to the non-root user for security (Least Privilege principle)
USER appuser:appgroup

# Expose Spring Boot web port
EXPOSE 8080

# Environment variables with container defaults
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Container Healthcheck using Spring Boot Actuator readiness probe
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/readiness || exit 1

# Launch application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

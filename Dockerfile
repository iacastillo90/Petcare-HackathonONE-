# ===================================================================
# Petcare Application - Dockerfile
# ===================================================================
# Multi-stage build for optimized production image
# ===================================================================

# ===================================================================
# Stage 1: Build
# ===================================================================
FROM gradle:8.14.3-jdk21 AS builder

WORKDIR /app

# Create cache and reports directories with proper permissions BEFORE copying files
RUN mkdir -p /app/.gradle /app/build/reports && chmod -R 777 /app

# Copy build configuration first for dependency caching
COPY --chown=gradle:gradle build.gradle settings.gradle gradlew ./
COPY --chown=gradle:gradle gradle gradle

# Download dependencies (cached layer)
USER gradle
RUN gradle dependencies --no-daemon

# Copy source code
COPY --chown=gradle:gradle src src

# Build the application
RUN gradle bootJar --no-daemon

# ===================================================================
# Stage 2: Production Runtime
# ===================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the JAR from builder stage
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# Set ownership
RUN chown -R spring:spring /app

USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8088/actuator/health || exit 1

# Expose port
EXPOSE 8088

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

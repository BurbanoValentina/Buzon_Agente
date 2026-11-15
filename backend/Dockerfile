# Multi-stage Dockerfile optimized for Render & Cloud Deployment (Java 21)
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package application (skipping unit tests for fast image build)
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage with minimal Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

# Limit heap memory to 384MB to fit comfortably in Render's 512MB free tier
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseG1GC"

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8081} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

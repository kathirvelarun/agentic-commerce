# ── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY agent-backend/pom.xml agent-backend/
# Download deps first (layer cache)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl agent-backend -am -q 2>/dev/null || true
COPY agent-backend/src agent-backend/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -pl agent-backend -am -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=build /build/agent-backend/target/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.threads.virtual.enabled=true", \
  "-jar", "/app/app.jar"]

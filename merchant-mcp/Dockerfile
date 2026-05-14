FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY merchant-mcp/pom.xml merchant-mcp/
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl merchant-mcp -am -q 2>/dev/null || true
COPY merchant-mcp/src merchant-mcp/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -pl merchant-mcp -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/merchant-mcp/target/*.jar app.jar
EXPOSE 8002
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.threads.virtual.enabled=true", \
  "-jar", "/app/app.jar"]

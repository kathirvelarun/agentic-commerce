FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY merchant-backend/pom.xml merchant-backend/
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl merchant-backend -am -q 2>/dev/null || true
COPY merchant-backend/src merchant-backend/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -pl merchant-backend -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=build /build/merchant-backend/target/*.jar app.jar
EXPOSE 8001
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.threads.virtual.enabled=true", \
  "-jar", "/app/app.jar"]

# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build (frontend-maven-plugin will install Node + npm inside)
COPY . .
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S certx && adduser -S certx -G certx
USER certx

COPY --from=build /app/target/certx-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

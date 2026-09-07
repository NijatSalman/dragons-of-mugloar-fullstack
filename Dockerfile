# syntax=docker/dockerfile:1

# Stage 1: build the React frontend into static files.
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# Stage 2: build the Spring Boot jar, with the frontend served from its static resources.
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /app
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon --version > /dev/null
COPY src ./src
COPY --from=frontend /app/frontend/dist ./src/main/resources/static
RUN ./gradlew --no-daemon bootJar -x test

# Stage 3: a small runtime image with only the jar, running as a non-root user.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home --shell /usr/sbin/nologin dragons
COPY --from=backend /app/build/libs/*.jar app.jar
USER dragons
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

# Stage 1: Build
FROM openjdk:21-jdk-bullseye AS builder
WORKDIR /home/gradle/project
COPY . .
RUN chmod +x ./gradlew && \
    ./gradlew shadowJar -x test --stacktrace

# Stage 2: Runtime
FROM openjdk:21-jdk-bullseye
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/be-was-neon-1.0-SNAPSHOT-all.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

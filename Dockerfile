# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Grant execution rights to the maven wrapper
RUN chmod +x ./mvnw

# Copy the project source
COPY src src

# Build the application
RUN ./mvnw -B clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /workspace/app/target/*.jar app.jar

# Expose the port Spring Boot runs on (usually 8080)
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
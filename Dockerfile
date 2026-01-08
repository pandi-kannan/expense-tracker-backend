# Stage 1: build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy project files from demo folder
COPY demo/pom.xml .
COPY demo/src ./src
COPY demo/mvnw .
COPY demo/.mvn ./.mvn

# Build jar
RUN ./mvnw clean package -DskipTests

# Stage 2: run
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/expense-tracker-1.0.jar ./expense-tracker-1.0.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","expense-tracker-1.0.jar"]

FROM maven:3-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -q

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p data logs
EXPOSE 8080
ENTRYPOINT ["java", "-XX:ErrorFile=logs/hs_err_%p.log", "-jar", "app.jar"]

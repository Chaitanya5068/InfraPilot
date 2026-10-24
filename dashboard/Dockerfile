FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY . .
RUN if [ -f "./mvnw" ]; then ./mvnw -B -q package -DskipTests; else mvn -B -q package -DskipTests; fi

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
USER 1000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

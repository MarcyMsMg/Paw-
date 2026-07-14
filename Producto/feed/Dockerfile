# Dockerfile para microservicio Spring Boot / Java 21
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia el proyecto completo del microservicio
COPY . .

# Normaliza el wrapper por si viene desde Windows y compila sin ejecutar tests
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia el jar generado por Maven
COPY --from=build /app/target/*.jar app.jar

# Puerto documentativo; el puerto real lo define SERVER_PORT en docker-compose
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

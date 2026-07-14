# PAW+ - Guia base para microservicios backend

Este documento define una base comun para crear los microservicios backend de PAW+. El objetivo es que todos los servicios mantengan una estructura similar, usen las mismas versiones principales y sigan las mismas convenciones de codigo.

## 1. Creacion del proyecto

En Visual Studio Code:

1. Presionar `Ctrl + Shift + P`.
2. Buscar `Spring Initializr: Create a Maven Project`.
3. Seleccionar las siguientes opciones:

```txt
Project: Maven
Spring Boot Version: 4.1.0
Language: Java
Group Id: com.paw
Artifact Id: nombre-del-servicio
Packaging: Jar
Java Version: 21
```

Ejemplos de `Artifact Id`:

```txt
adoptions
users
donations
crowdfunding
notifications
feed
```

## 2. Dependencias base

Todos los microservicios deberian partir con estas dependencias:

```txt
Spring Web
Spring Data JPA
PostgreSQL Driver
Spring Security
Validation
```

En el `pom.xml` deberian verse de forma similar a esto:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## 3. Dependencias recomendadas

Para mantener el codigo mas limpio y preparar los servicios para autenticacion con JWT:

```txt
Lombok
JJWT API
JJWT Impl
JJWT Jackson
```

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

## 4. Estructura base de carpetas

Cada microservicio deberia seguir una estructura parecida a esta:

```txt
src/main/java/com/paw/nombre_servicio
├── common
├── config
├── controller
├── domain
├── dto
├── exception
├── repository
├── security
└── service
```

Descripcion de cada carpeta:

```txt
common: clases reutilizables, como ApiResponse.
config: configuraciones del servicio, seguridad, CORS y beans.
controller: endpoints REST.
domain: entidades JPA, enums y conceptos principales del negocio.
dto: objetos de entrada y salida. Evitan exponer entidades de la base de datos directamente.
exception: manejo centralizado de errores.
repository: interfaces JPA para acceso a datos.
security: JWT, usuario autenticado y roles.
service: logica de negocio.
```

## 5. Convenciones de codigo

El codigo del backend debe escribirse en ingles:

```txt
Animal
User
Donation
Campaign
createAnimal()
listApplications()
AnimalResponse
CreateDonationRequest
```

No se deben exponer entidades directamente desde los controladores.

Correcto:

```java
public ApiResponse<AnimalResponse> getAnimal(...)
```

Evitar:

```java
public Animal getAnimal(...)
```

Usar DTOs para requests y responses:

```txt
CreateRequest
UpdateRequest
Response
```

Ejemplos:

```txt
AnimalCreateRequest
AnimalUpdateRequest
AnimalResponse
```

## 6. Respuesta estandar de API

Todos los servicios deberian responder con la misma estructura:

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

Clase sugerida:

```java
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

Esto ayuda a que el frontend consuma todos los microservicios de la misma manera.

## 7. Base de datos

Usaremos Supabase PostgreSQL como base de datos central.

Cada microservicio deberia trabajar en su propio schema:

```txt
adoptions
users
donations
crowdfunding
notifications
feed
```

Ejemplo SQL:

```sql
create schema if not exists adoptions;
```

Ejemplo de `application.properties`:

```properties
spring.application.name=adoptions
server.port=${PORT:${SERVER_PORT:8084}}

spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.default_schema=adoptions
```

Importante: no subir contrasenas al repositorio. Usar variables de entorno.

## 8. Seguridad y futuro API Gateway

Cada microservicio debe estar preparado para trabajar con JWT.

Por ahora, los servicios pueden validar el token directamente. En el futuro, si usamos un API Gateway, el gateway podra validar el JWT y reenviar datos del usuario a cada microservicio mediante headers internos:

```txt
X-User-Id
X-User-Role
X-User-Email
```

Roles estandar:

```txt
ADMIN
NGO
NATURAL_PERSON
```

## 9. Puertos sugeridos

```txt
api-gateway: 8080
users: 8081
adoptions: 8082
crowdfunding: 8083
donations: 8084
feed: 8085
notifications: 8086
```

## 10. Recomendaciones finales

- Mantener el codigo en ingles.
- Usar DTOs para entrada y salida de datos.
- Usar entidades solo dentro del backend.
- No mezclar logica de negocio en los controllers.
- Manejar errores con `GlobalExceptionHandler`.
- Usar schemas separados por microservicio.
- Usar variables de entorno para credenciales.
- Mantener respuestas API consistentes con `ApiResponse`.
- Pensar los servicios para que puedan funcionar detras de un API Gateway en el futuro.


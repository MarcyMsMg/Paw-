package com.paw.campaigns.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Configuración de CORS para permitir que el frontend (Vite) consuma este servicio.
// Este microservicio no maneja autenticación, así que no usamos Spring Security:
// basta con habilitar CORS a nivel de MVC.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:8080",
                        "http://localhost:5173",
                        "http://127.0.0.1:8080",
                        "http://127.0.0.1:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

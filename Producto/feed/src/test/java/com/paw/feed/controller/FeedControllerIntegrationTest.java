package com.paw.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.paw.feed.domain.FeedPostStatus;
import com.paw.feed.domain.FeedPostType;
import com.paw.feed.dto.FeedPostResponse;
import com.paw.feed.exception.ApiException;
import com.paw.feed.security.JwtService;
import com.paw.feed.service.FeedPostService;

@WebMvcTest({FeedPostController.class, NgoFeedPostController.class, AdminFeedPostController.class})
@AutoConfigureMockMvc(addFilters = false)
class FeedControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedPostService feedPostService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void listarFeedPublico_conFiltroTipo_devuelvePublicaciones() throws Exception {
        // Arrange
        when(feedPostService.listPublic(null, FeedPostType.RESCUE, null)).thenReturn(List.of(feedPostResponse()));

        // Act + Assert
        mockMvc.perform(get("/api/feed/posts").param("type", "RESCUE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Rescate de Benito"));
    }

    @Test
    void obtenerPostInexistente_devuelve404() throws Exception {
        // Arrange
        when(feedPostService.getPublic(any(UUID.class))).thenThrow(ApiException.notFound("Feed post not found"));

        // Act + Assert
        mockMvc.perform(get("/api/feed/posts/{postId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void crearPostOng_conDatosInvalidos_devuelve400YNoLlamaServicio() throws Exception {
        // Arrange
        String body = """
                {
                  "summary": "Sin titulo ni contenido"
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/feed/ngo/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(feedPostService, never()).create(any());
    }

    @Test
    void crearPostOng_conDatosValidos_devuelveCreated() throws Exception {
        // Arrange
        when(feedPostService.create(any())).thenReturn(feedPostResponse());
        String body = """
                {
                  "title": "Rescate de Benito",
                  "summary": "Actualizacion breve",
                  "content": "Contenido de la actualizacion de rescate con detalles suficientes.",
                  "type": "RESCUE",
                  "imageUrls": ["https://img.test/rescate.jpg"],
                  "publishNow": true
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/feed/ngo/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        verify(feedPostService).create(any());
    }

    private FeedPostResponse feedPostResponse() {
        Instant now = Instant.now();
        return new FeedPostResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Fundacion Test",
                "https://img.test/logo.png",
                "Rescate de Benito",
                "Actualizacion breve",
                "Contenido de la actualizacion de rescate con detalles suficientes.",
                FeedPostType.RESCUE,
                List.of("https://img.test/rescate.jpg"),
                null,
                null,
                null,
                FeedPostStatus.PUBLISHED,
                now,
                now,
                now
        );
    }
}

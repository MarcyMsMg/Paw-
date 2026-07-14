package com.paw.campaigns.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paw.campaigns.client.NgoClient;
import com.paw.campaigns.client.NotificationsClient;
import com.paw.campaigns.client.dto.NgoData;
import com.paw.campaigns.dto.request.CreateCampaignRequest;
import com.paw.campaigns.dto.request.UpdateCampaignRequest;
import com.paw.campaigns.dto.response.CampaignResponse;
import com.paw.campaigns.enums.CampaignStatus;
import com.paw.campaigns.exception.NgoNotEligibleException;
import com.paw.campaigns.exception.ResourceNotFoundException;
import com.paw.campaigns.model.Campaign;
import com.paw.campaigns.repository.CampaignRepository;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private NgoClient ngoClient;

    @Mock
    private NotificationsClient notificationsClient;

    private CampaignServiceImpl campaignService;
    private UUID ngoId;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignServiceImpl(campaignRepository, ngoClient, notificationsClient);
        ngoId = UUID.randomUUID();
    }

    private CreateCampaignRequest validRequest(LocalDate start, LocalDate end) {
        return new CreateCampaignRequest(
                ngoId, "  Ayuda a Firulais  ", "  Descripcion de la campaÃƒÆ’Ã‚Â±a  ",
                null, null, null, new BigDecimal("50000"), start, end
        );
    }

    @Test
    void create_debeCrearCampana_cuandoOngActivaYFechasValidas() {
        // Arrange
        when(ngoClient.getNgo(ngoId)).thenReturn(new NgoData(ngoId, "Patitas Felices", "ACTIVE"));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreateCampaignRequest request = validRequest(LocalDate.now(), LocalDate.now().plusDays(30));

        // Act
        CampaignResponse response = campaignService.create(request);

        // Assert
        assertEquals("Ayuda a Firulais", response.title());
        assertEquals(BigDecimal.ZERO, response.raisedAmount());
        assertEquals(CampaignStatus.ACTIVE, response.status());
    }

    @Test
    void create_debeLanzarExcepcion_cuandoLaOngNoEstaActiva() {
        // Arrange
        when(ngoClient.getNgo(ngoId)).thenReturn(new NgoData(ngoId, "Patitas Felices", "PENDING"));
        CreateCampaignRequest request = validRequest(LocalDate.now(), LocalDate.now().plusDays(30));

        // Act + Assert
        assertThrows(NgoNotEligibleException.class, () -> campaignService.create(request));
    }

    @Test
    void create_debeLanzarExcepcion_cuandoFechaFinEsIgualAFechaInicio() {
        // Arrange
        when(ngoClient.getNgo(ngoId)).thenReturn(new NgoData(ngoId, "Patitas Felices", "ACTIVE"));
        LocalDate hoy = LocalDate.now();
        CreateCampaignRequest request = validRequest(hoy, hoy);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> campaignService.create(request));
    }

    @Test
    void create_debeLanzarExcepcion_cuandoFechaFinEsAnteriorAFechaInicio() {
        // Arrange
        when(ngoClient.getNgo(ngoId)).thenReturn(new NgoData(ngoId, "Patitas Felices", "ACTIVE"));
        LocalDate hoy = LocalDate.now();
        CreateCampaignRequest request = validRequest(hoy, hoy.minusDays(1));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> campaignService.create(request));
    }

    private Campaign existingCampaign() {
        return Campaign.builder()
                .id(UUID.randomUUID())
                .ngoId(ngoId)
                .title("Titulo original")
                .description("Descripcion original")
                .goalAmount(new BigDecimal("100000"))
                .raisedAmount(BigDecimal.ZERO)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(CampaignStatus.ACTIVE)
                .build();
    }

    @Test
    void update_debeActualizarSoloLosCamposNoNulos() {
        // Arrange
        Campaign campaign = existingCampaign();
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        UpdateCampaignRequest request = new UpdateCampaignRequest("  Nuevo titulo  ", null, null, null, null, null, null);

        // Act
        CampaignResponse response = campaignService.update(campaign.getId(), request);

        // Assert
        assertEquals("Nuevo titulo", response.title());
        assertEquals("Descripcion original", response.description());
    }

    @Test
    void update_noDebePermitirModificarLaMeta_porqueElDtoNiSiquieraLaExpone() {
        // Arrange: goalAmount es inmutable por diseÃƒÆ’Ã‚Â±o; UpdateCampaignRequest no tiene ese campo.
        Campaign campaign = existingCampaign();
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        UpdateCampaignRequest request = new UpdateCampaignRequest("Otro titulo", null, null, null, null, null, null);

        // Act
        CampaignResponse response = campaignService.update(campaign.getId(), request);

        // Assert
        assertEquals(new BigDecimal("100000"), response.goalAmount());
    }

    @Test
    void update_debeLanzarExcepcion_cuandoLaNuevaFechaFinQuedaAntesDeLaFechaInicioExistente() {
        // Arrange
        Campaign campaign = existingCampaign();
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        UpdateCampaignRequest request = new UpdateCampaignRequest(
                null, null, null, null, null, null, campaign.getStartDate().minusDays(1)
        );

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> campaignService.update(campaign.getId(), request));
    }

    @Test
    void update_debeLanzarResourceNotFound_cuandoLaCampanaNoExiste() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(campaignRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> campaignService.update(id, new UpdateCampaignRequest(null, null, null, null, null, null, null))
        );
    }

    @Test
    void addRaisedAmount_debeMarcarCompletada_cuandoElRecaudadoAlcanzaLaMeta() {
        // Arrange
        Campaign campaign = existingCampaign();
        campaign.setGoalAmount(new BigDecimal("1000"));
        campaign.setRaisedAmount(new BigDecimal("800"));
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        // Act
        CampaignResponse response = campaignService.addRaisedAmount(campaign.getId(), new BigDecimal("200"));

        // Assert
        assertEquals(new BigDecimal("1000"), response.raisedAmount());
        assertEquals(CampaignStatus.COMPLETED, response.status());
    }

    @Test
    void addRaisedAmount_noDebeMarcarCompletada_cuandoAunNoAlcanzaLaMeta() {
        // Arrange
        Campaign campaign = existingCampaign();
        campaign.setGoalAmount(new BigDecimal("1000"));
        campaign.setRaisedAmount(new BigDecimal("100"));
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        // Act
        CampaignResponse response = campaignService.addRaisedAmount(campaign.getId(), new BigDecimal("200"));

        // Assert
        assertEquals(new BigDecimal("300"), response.raisedAmount());
        assertEquals(CampaignStatus.ACTIVE, response.status());
    }

    @Test
    void subtractRaisedAmount_noDebeQuedarNegativo_cuandoElMontoSuperaLoRecaudado() {
        // Arrange
        Campaign campaign = existingCampaign();
        campaign.setRaisedAmount(new BigDecimal("100"));
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        // Act
        CampaignResponse response = campaignService.subtractRaisedAmount(campaign.getId(), new BigDecimal("500"));

        // Assert
        assertEquals(BigDecimal.ZERO, response.raisedAmount());
    }

    @Test
    void subtractRaisedAmount_debeDescontarNormalmente_cuandoHaySuficienteRecaudado() {
        // Arrange
        Campaign campaign = existingCampaign();
        campaign.setRaisedAmount(new BigDecimal("500"));
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        // Act
        CampaignResponse response = campaignService.subtractRaisedAmount(campaign.getId(), new BigDecimal("200"));

        // Assert
        assertEquals(new BigDecimal("300"), response.raisedAmount());
    }
    @Test
    void findAllActive_debeListarCampanasActivasDentroDeVentanaPublica() {
        // Arrange
        Campaign campaign = existingCampaign();
        LocalDate today = LocalDate.now();
        when(campaignRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                CampaignStatus.ACTIVE, today, today
        )).thenReturn(List.of(campaign));

        // Act
        List<CampaignResponse> response = campaignService.findAllActive();

        // Assert
        assertEquals(1, response.size());
        assertEquals(campaign.getId(), response.getFirst().id());
        assertEquals(CampaignStatus.ACTIVE, response.getFirst().status());
    }

    @Test
    void finish_debeMarcarCompletadaYNotificar_cuandoNoEstabaFinalizada() {
        // Arrange
        Campaign campaign = existingCampaign();
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        // Act
        CampaignResponse response = campaignService.finish(campaign.getId());

        // Assert
        assertEquals(CampaignStatus.COMPLETED, response.status());
        var captor = org.mockito.ArgumentCaptor.forClass(com.paw.campaigns.client.dto.NotificationEventRequest.class);
        org.mockito.Mockito.verify(notificationsClient).send(captor.capture());
        assertEquals("CAMPAIGN_FINISHED", captor.getValue().type());
        assertEquals(campaign.getNgoId(), captor.getValue().recipientUserId());
    }

    @Test
    void finish_noDebeDuplicarNotificacion_cuandoYaEstabaCompletada() {
        // Arrange
        Campaign campaign = existingCampaign();
        campaign.setStatus(CampaignStatus.COMPLETED);
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        // Act
        CampaignResponse response = campaignService.finish(campaign.getId());

        // Assert
        assertEquals(CampaignStatus.COMPLETED, response.status());
        org.mockito.Mockito.verify(notificationsClient, org.mockito.Mockito.never()).send(any());
    }

    @Test
    void statsForNgo_debeCalcularTotalesYProgresoPromedio() {
        // Arrange
        Campaign active = existingCampaign();
        active.setGoalAmount(new BigDecimal("1000"));
        active.setRaisedAmount(new BigDecimal("500"));
        Campaign completed = existingCampaign();
        completed.setId(UUID.randomUUID());
        completed.setGoalAmount(new BigDecimal("2000"));
        completed.setRaisedAmount(new BigDecimal("2000"));
        completed.setStatus(CampaignStatus.COMPLETED);
        when(campaignRepository.findByNgoId(ngoId)).thenReturn(List.of(active, completed));

        // Act
        var stats = campaignService.statsForNgo(ngoId);

        // Assert
        assertEquals(1, stats.activeCampaigns());
        assertEquals(1, stats.finishedCampaigns());
        assertEquals(new BigDecimal("3000"), stats.totalGoalAmount());
        assertEquals(new BigDecimal("2500"), stats.totalRaisedAmount());
        assertEquals(new BigDecimal("75.00"), stats.averageProgress());
        assertEquals(2, stats.campaignProgress().size());
    }
}

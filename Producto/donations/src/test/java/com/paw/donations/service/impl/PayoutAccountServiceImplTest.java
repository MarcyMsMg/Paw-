package com.paw.donations.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paw.donations.dto.request.UpsertPayoutAccountRequest;
import com.paw.donations.dto.response.PayoutAccountResponse;
import com.paw.donations.model.PayoutAccount;
import com.paw.donations.repository.PayoutAccountRepository;

@ExtendWith(MockitoExtension.class)
class PayoutAccountServiceImplTest {

    @Mock
    private PayoutAccountRepository payoutAccountRepository;

    private PayoutAccountServiceImpl payoutAccountService;
    private UUID ngoId;

    @BeforeEach
    void setUp() {
        payoutAccountService = new PayoutAccountServiceImpl(payoutAccountRepository);
        ngoId = UUID.randomUUID();
    }

    @Test
    void upsert_debeCrearCuentaNueva_cuandoLaOngNoTeniaDatosCargados() {
        // Arrange
        when(payoutAccountRepository.findByNgoId(ngoId)).thenReturn(Optional.empty());
        when(payoutAccountRepository.save(any(PayoutAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UpsertPayoutAccountRequest request = new UpsertPayoutAccountRequest(
                "Fundacion Patitas", "12.345.678-5", "Banco Estado", "Cuenta Corriente", "1234567890", null
        );

        // Act
        PayoutAccountResponse response = payoutAccountService.upsert(ngoId, request);

        // Assert
        assertEquals("Fundacion Patitas", response.holderName());
        assertEquals("12.345.678-5", response.rut());
        assertEquals(ngoId, response.ngoId());
    }

    @Test
    void upsert_debeActualizarCuentaExistente_enVezDeCrearUnaNueva() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        PayoutAccount existente = PayoutAccount.builder()
                .id(accountId)
                .ngoId(ngoId)
                .holderName("Nombre viejo")
                .rut("11.111.111-1")
                .bankName("Banco viejo")
                .accountType("Cuenta Vista")
                .accountNumber("000")
                .build();
        when(payoutAccountRepository.findByNgoId(ngoId)).thenReturn(Optional.of(existente));
        when(payoutAccountRepository.save(any(PayoutAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UpsertPayoutAccountRequest request = new UpsertPayoutAccountRequest(
                "Nombre actualizado", "12.345.678-5", "Banco nuevo", "Cuenta Corriente", "9999", "contacto@ong.cl"
        );

        // Act
        PayoutAccountResponse response = payoutAccountService.upsert(ngoId, request);

        // Assert: se actualizo la misma cuenta (mismo id), no se creo una nueva
        assertEquals(accountId, response.id());
        assertEquals("Nombre actualizado", response.holderName());
        assertEquals("Banco nuevo", response.bankName());
    }
}

package com.paw.adoptions.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChilePhoneValidatorTest {

    @Test
    void shouldAcceptBareMobileNumber() {
        // Arrange
        String phone = "912345678";

        // Act
        boolean result = ChilePhoneValidator.isValid(phone);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldAcceptNumberWithPlusFiftySixPrefix() {
        // Arrange
        String phone = "+56912345678";

        // Act
        boolean result = ChilePhoneValidator.isValid(phone);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldAcceptNumberWithFiftySixPrefixAndSpaces() {
        // Arrange
        String phone = "56 9 1234 5678";

        // Act
        boolean result = ChilePhoneValidator.isValid(phone);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldRejectNumberNotStartingWithNine() {
        // Arrange
        String phone = "812345678";

        // Act
        boolean result = ChilePhoneValidator.isValid(phone);

        // Assert
        assertFalse(result);
    }

    @Test
    void shouldRejectTooFewDigits() {
        // Arrange
        String phone = "91234";

        // Act
        boolean result = ChilePhoneValidator.isValid(phone);

        // Assert
        assertFalse(result);
    }

    @Test
    void shouldRejectNullInput() {
        // Act
        boolean result = ChilePhoneValidator.isValid(null);

        // Assert
        assertFalse(result);
    }
}

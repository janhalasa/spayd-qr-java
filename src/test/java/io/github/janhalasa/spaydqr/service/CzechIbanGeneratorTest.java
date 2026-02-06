package io.github.janhalasa.spaydqr.service;

import org.apache.commons.validator.routines.IBANValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class CzechIbanGeneratorTest {

    private static final String VALID_ACCOUNT_NUMBER = "1234567899";

    @ParameterizedTest
    @DisplayName("Should correctly convert known Czech account numbers to IBAN")
    @CsvSource({
            "0800, 1234567899, '', CZ55 0800 0000 0012 3456 7899"
    })
    void testKnownIbanConversions(String bankCode, String account, String prefix, String expectedIban) {
        String iban = CzechIbanGenerator.composeCzechIban(bankCode, account, prefix);
        String formatted = CzechIbanGenerator.formatIban(iban);
        assertEquals(expectedIban, formatted);
        assertTrue(IBANValidator.getInstance().isValid(iban));
    }

    @Test
    @DisplayName("Should handle null prefix as empty string")
    void testNullPrefix() {
        String resultWithNull = CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, null);
        String resultWithEmpty = CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, "");
        assertEquals(resultWithEmpty, resultWithNull);
    }

    @Test
    @DisplayName("Should pad account number with leading zeros")
    void testAccountPadding() {
        String result = CzechIbanGenerator.formatIban(CzechIbanGenerator.composeCzechIban("0800", "51", ""));
        assertTrue(result.contains("0000 0051"));
    }

    @Test
    @DisplayName("Should throw exception when bank code is null")
    void testNullBankCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban(null, VALID_ACCOUNT_NUMBER, "");
        });
        assertTrue(exception.getMessage().contains("Bank code cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when bank code is empty")
    void testEmptyBankCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("", "123456789", "");
        });
        assertTrue(exception.getMessage().contains("Bank code cannot be empty"));
    }

    @ParameterizedTest
    @DisplayName("Should throw exception when bank code has invalid length")
    @ValueSource(strings = {"1", "12", "123", "12345", "123456"})
    void testInvalidBankCodeLength(String invalidBankCode) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban(invalidBankCode, "123456789", "");
        });
        assertTrue(exception.getMessage().contains("must be exactly 4 digits"));
    }

    @ParameterizedTest
    @DisplayName("Should throw exception when bank code contains non-digits")
    @ValueSource(strings = {"08a0", "080X", "08 0", "08 0", "ABCD"})
    void testBankCodeWithNonDigits(String invalidBankCode) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban(invalidBankCode, "123456789", "");
        });
        assertTrue(exception.getMessage().contains("must contain only digits"));
    }

    // Account Number Validation Tests

    @Test
    @DisplayName("Should throw exception when account number is null")
    void testNullAccountNumber() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("0800", null, "");
        });
        assertTrue(exception.getMessage().contains("Account number cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when account number is empty")
    void testEmptyAccountNumber() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("0800", "", "");
        });
        assertTrue(exception.getMessage().contains("Account number cannot be empty"));
    }

    @Test
    @DisplayName("Should throw exception when account number exceeds 10 digits")
    void testAccountNumberTooLong() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("0800", "12345678901", "");
        });
        assertTrue(exception.getMessage().contains("cannot exceed 10 digits"));
    }

    @ParameterizedTest
    @DisplayName("Should throw exception when account number contains non-digits")
    @ValueSource(strings = {"12345678a", "1234 5678", "1234 5678", "ABCD"})
    void testAccountNumberWithNonDigits(String invalidAccount) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("0800", invalidAccount, "");
        });
        assertTrue(exception.getMessage().contains("must contain only digits"));
    }

    @Test
    @DisplayName("Should throw exception when account number is zeros")
    void testMultipleZeroAccountNumber() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("0800", "0000000", "");
        });
        assertTrue(exception.getMessage().contains("Account number cannot be zero"));
    }

    @Test
    @DisplayName("Should throw exception when account number is not modulo 11")
    void testAccountNumberNotModulo11() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("0800", "0000012", "");
        });
        assertTrue(exception.getMessage().contains("Account number must have modulo 11 checksum"));
    }

    // Prefix Validation Tests

    @Test
    @DisplayName("Should accept valid prefix")
    void testValidPrefix() {
        assertDoesNotThrow(() -> {
            CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, "51");
        });
    }

    @Test
    @DisplayName("Should throw exception when prefix exceeds 6 digits")
    void testPrefixTooLong() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, "1234567");
        });
        assertTrue(exception.getMessage().contains("cannot exceed 6 digits"), exception.getMessage());
    }

    @ParameterizedTest
    @DisplayName("Should throw exception when prefix contains non-digits")
    @ValueSource(strings = {"12a", "1 2", "1 2", "ABC"})
    void testPrefixWithNonDigits(String invalidPrefix) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, invalidPrefix);
        });
        assertTrue(exception.getMessage().contains("must contain only digits"), exception.getMessage());
    }

    // Format Tests

    @Test
    @DisplayName("Should format IBAN with spaces")
    void testIbanFormatting() {
        String result = CzechIbanGenerator.formatIban(CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, ""));
        String[] parts = result.split(" ");
        assertEquals(6, parts.length);
        assertEquals(4, parts[0].length()); // CZ##
        for (int i = 1; i < parts.length; i++) {
            assertEquals(4, parts[i].length());
        }
    }

    @Test
    @DisplayName("Should create valid IBAN with correct check digits")
    void testCheckDigitCalculation() {
        String result = CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, "19");

        // The check digits should be numeric
        String checkDigits = result.substring(2, 4);
        assertTrue(checkDigits.matches("\\d{2}"));
    }

    @Test
    @DisplayName("Should produce IBAN with correct total length")
    void testIbanLength() {
        String result = CzechIbanGenerator.formatIban(CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, "19"));
        String unformatted = CzechIbanGenerator.unformatIban(result);
        assertEquals(24, unformatted.length());
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle maximum length account number")
    void testMaxLengthAccount() {
        String result = CzechIbanGenerator.formatIban(CzechIbanGenerator.composeCzechIban("0800", "9999999999", ""));
        assertTrue(result.contains("99 9999 9999"));
    }

    @Test
    @DisplayName("Should handle maximum length prefix")
    void testMaxLengthPrefix() {
        String result = CzechIbanGenerator.formatIban(CzechIbanGenerator.composeCzechIban("0800", VALID_ACCOUNT_NUMBER, "999993"));
        assertTrue(result.contains("9999 93"));
    }

    // Unformat Test

    @Test
    @DisplayName("Should correctly unformat IBAN by removing dashes and spaces")
    void testUnformatIban() {
        String formatted = "CZ65 0800 0000 0012 3456 7890";
        String unformatted = CzechIbanGenerator.unformatIban(formatted);
        assertEquals("CZ6508000000001234567890", unformatted);
        assertEquals(24, unformatted.length());
    }
}
package io.github.janhalasa.spaydqr.service;

import io.github.janhalasa.spaydqr.model.BankAccount;
import io.github.janhalasa.spaydqr.model.Payment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpaydQrCodeGeneratorTest {

    @Test
    void givenPaymentString_whenGenerateQrCode_thenValidQrCode() throws Exception {
        String spaydString = "SPD*1.0*ACC:CZ5508000000001234567899+GIBACZPX*AM:123.45*CC:CZK*DT:20290131*MSG:ZPRAVA PRO PRIJEMCE";
        byte[] qrCode = SpaydQrCodeGenerator.generateQrCodeFromString(spaydString, 100);
        assertNotNull(qrCode);
    }

    @Test
    void givenPayment_whenGenerateQrCode_thenValidQrCode() throws Exception {
        Payment payment = Payment.builder()
                .bankAccount(new BankAccount("CZ5508000000001234567899", "GIBACZPX"))
                .amount(new java.math.BigDecimal("123.45"))
                .currencyCode("CZK")
                .paymentDueDate(java.time.LocalDate.of(2029, 1, 31))
                .paymentNote("ZPRAVA PRO PRIJEMCE")
                .build();
        byte[] qrCode = SpaydQrCodeGenerator.generateQrCode(payment, 100);
        assertNotNull(qrCode);
    }

    @Test
    void givenPayment_whenGenerateQrCodeWithChecksum_thenValidQrCode() throws Exception {
        Payment payment = Payment.builder()
                .bankAccount(new BankAccount("CZ5508000000001234567899", "GIBACZPX"))
                .amount(new java.math.BigDecimal("123.45"))
                .currencyCode("CZK")
                .paymentDueDate(java.time.LocalDate.of(2029, 1, 31))
                .paymentNote("ZPRAVA PRO PRIJEMCE")
                .build();
        byte[] qrImage = SpaydQrCodeGenerator.generateQrCode(payment, 100, true, false);
        assertNotNull(qrImage);
        java.nio.file.Files.write(java.nio.file.Path.of("spayd-qr.png"), qrImage);
    }
}
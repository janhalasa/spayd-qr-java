package io.github.janhalasa.spaydqr.service;

import io.github.janhalasa.spaydqr.model.BankAccount;
import io.github.janhalasa.spaydqr.model.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Generování řetězce SPAYD – základní parametry:
 * https://qr-platba.cz/pro-vyvojare/restful-api/#generator-string
 * https://api.paylibo.com/paylibo/generator/string
 */
public class SpaydSerializerTest {

    @Test
    void givenIbanAndBic_whenSerialize_thenValidSpaydString() {
        String expected = "SPD*1.0*ACC:CZ5508000000001234567899+GIBACZPX";
        Payment payment = Payment.builder()
                .bankAccount(new BankAccount("CZ5508000000001234567899", "GIBACZPX"))
                .build();
        String result = SpaydSerializer.serialize(payment);
        assertEquals(expected, result);
    }

    @Test
    void givenIbanNull_whenSerialize_thenException() {
        Payment payment = Payment.builder()
                .build();
        Exception exception = assertThrows(Exception.class, () -> SpaydSerializer.serialize(payment));
        assertEquals("Bank account (IBAN) is required", exception.getMessage());
    }

    @Test
    void givenAllFieldsSet_whenSerialize_thenValidSpaydString() {
        String expected = "SPD*1.0*ACC:CZ5508000000001234567899+GIBACZPX*ALT-ACC:SK3581800000510543524521+TATRSKBX,SK3112000000001987426375*AM:123.45*CC:CZK*DT:20290131*MSG:ZPRAVA PRO PRIJEMCE*X-KS:0308*X-SS:11111*X-VS:22222";

        Payment payment = Payment.builder()
                .bankAccount(new BankAccount("CZ5508000000001234567899", "GIBACZPX"))
                .alternativeBankAccounts(List.of(
                        new BankAccount("SK3581800000510543524521", "TATRSKBX"),
                        new BankAccount("SK3112000000001987426375")
                ))
                .amount(new BigDecimal("123.45"))
                .currencyCode("CZK")
                .paymentDueDate(LocalDate.of(2029, 1, 31))
                .variableSymbol("22222")
                .specificSymbol("11111")
                .constantSymbol("0308")
                .paymentNote("ZPRAVA PRO PRIJEMCE")
                .build();
        String result = SpaydSerializer.serialize(payment);
        assertEquals(expected, result);
    }

    @Test
    void givenTextsWithSpecialCharacters_whenSerialize_thenValidSpaydStringWithTextConvertedToIso88591() {
        String expected = "SPD*1.0*ACC:CZ5508000000001234567899+GIBACZPX*MSG:ZPRAVA PRO PRIJEMCE";

        Payment payment = Payment.builder()
                .bankAccount(new BankAccount("CZ5508000000001234567899", "GIBACZPX"))
                .paymentNote("   Zpráva pro příjemce   ")
                .build();
        String result = SpaydSerializer.serialize(payment);
        assertEquals(expected, result);
    }

    @Test
    void givenCrcIsRequired_whenSerialize_thenValidSpaydStringWithCrc() {
        String expected = "SPD*1.0*ACC:CZ5508000000001234567899+GIBACZPX*AM:123.45*CC:CZK*DT:20290131*MSG:ZPRAVA PRO PRIJEMCE*X-KS:0308*X-SS:11111*X-VS:22222*CRC32:56674E89";
        Payment payment = Payment.builder()
                .bankAccount(new BankAccount("CZ5508000000001234567899", "GIBACZPX"))
                .amount(new BigDecimal("123.45"))
                .currencyCode("CZK")
                .paymentDueDate(LocalDate.of(2029, 1, 31))
                .variableSymbol("22222")
                .specificSymbol("11111")
                .constantSymbol("0308")
                .paymentNote("ZPRAVA PRO PRIJEMCE")
                .build();
        String result = SpaydSerializer.serialize(payment, true, true);
        assertEquals(expected, result);
    }
}
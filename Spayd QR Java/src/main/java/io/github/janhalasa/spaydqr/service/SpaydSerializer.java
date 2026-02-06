package io.github.janhalasa.spaydqr.service;

import io.github.janhalasa.spaydqr.model.BankAccount;
import io.github.janhalasa.spaydqr.model.Payment;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

public class SpaydSerializer {

    static final DecimalFormat DECIMAL_FORMAT;

    static {
        DECIMAL_FORMAT = new DecimalFormat("0.##");
        DECIMAL_FORMAT.setMaximumFractionDigits(9);
        DECIMAL_FORMAT.setGroupingUsed(false);
    }

    private static String toISO88591(String value, boolean apply) {
        if (value == null) {
            return null;
        }
        return apply
                ? StringUtils.stripAccents(value.trim()).toUpperCase()
                : value;
    }

    private static void validate(Payment payment) {
        if (payment.getBankAccount() == null) {
            throw new IllegalArgumentException("Bank account (IBAN) is required");
        }
    }

    public static String serialize(Payment payment) {
        return serialize(payment, false, true);
    }

    private static String bankAccountValue(BankAccount bankAccount) {
        String result = bankAccount.getIban();
        if (bankAccount.getBic() != null) {
            result += "+" + bankAccount.getBic();
        }
        return result;
    }

    public static String serialize(Payment payment, boolean includeChecksum, boolean normalizeStrings) {
        validate(payment);

        final Map<String, String> fields = new HashMap<>();
        fields.put("ACC", bankAccountValue(payment.getBankAccount()));

        if (payment.getAlternativeBankAccounts() != null && !payment.getAlternativeBankAccounts().isEmpty()) {
            fields.put("ALT-ACC",payment.getAlternativeBankAccounts().stream()
                    .map(SpaydSerializer::bankAccountValue)
                    .collect(Collectors.joining(",")));
        }

        if (payment.getAmount() != null) {
            fields.put("AM", DECIMAL_FORMAT.format(payment.getAmount()));
        }
        if (payment.getCurrencyCode() != null) {
            fields.put("CC", payment.getCurrencyCode());
        }
        if (payment.getOriginatorsReference() != null) {
            fields.put("RF", payment.getOriginatorsReference());
        }
        if (payment.getBeneficiaryName() != null) {
            fields.put("RN", toISO88591(payment.getBeneficiaryName(), normalizeStrings));
        }
        if (payment.getPaymentDueDate() != null) {
            fields.put("DT", DateTimeFormatter.BASIC_ISO_DATE.format(payment.getPaymentDueDate()));
        }
        if (Boolean.TRUE.equals(payment.getInstantPayment())) {
            fields.put("PT", "IP");
        }
        if (payment.getPaymentNote() != null) {
            fields.put("MSG", toISO88591(payment.getPaymentNote(), normalizeStrings));
        }
        if (payment.getNotificationType() != null) {
            fields.put("NT", payment.getNotificationType());
        }
        if (payment.getNotificationAddress() != null) {
            fields.put("NTA", payment.getNotificationAddress());
        }
        if (payment.getConstantSymbol() != null) {
            fields.put("X-KS", payment.getConstantSymbol());
        }
        if (payment.getVariableSymbol() != null) {
            fields.put("X-VS", payment.getVariableSymbol());
        }
        if (payment.getSpecificSymbol() != null) {
            fields.put("X-SS", payment.getSpecificSymbol());
        }

        String fieldsAsString = fields.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining("*"));

        String result = "SPD*1.0*" + fieldsAsString;
        if (includeChecksum) {
            CRC32 crc32 = new CRC32();
            crc32.update(fieldsAsString.getBytes());
            String checksum = Long.toHexString(crc32.getValue()).toUpperCase();
            result += "*CRC32:" + checksum;
        }
        return result;
    }
}

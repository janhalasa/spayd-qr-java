package io.github.janhalasa.spaydqr.service;

import java.math.BigInteger;

public class CzechIbanGenerator {

    private static final String COUNTRY_CODE = "CZ";
    private static final int IBAN_LENGTH = 24;
    private static final int BANK_CODE_LENGTH = 4;
    private static final int MIN_ACCOUNT_LENGTH = 2;
    private static final int MAX_ACCOUNT_LENGTH = 10;
    private static final int PREFIX_MAX_LENGTH = 6;

    /**
     * Converts a Czech account number to IBAN format.
     *
     * @param bankCode the bank code (must be exactly 4 digits)
     * @param account the account number (up to 10 digits)
     * @param prefix the account prefix (up to 6 digits, can be null or empty)
     * @return the IBAN in format CZ##-####-####-####-####-####
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static String composeCzechIban(String bankCode, String account, String prefix) {
        // Validate inputs
        validateBankCode(bankCode);
        validateAccount(account);
        validatePrefix(prefix);

        // Normalize prefix (empty or null becomes "0")
        String normalizedPrefix = (prefix == null || prefix.isEmpty()) ? "0" : prefix;

        // Pad prefix to 6 digits and account to 10 digits with leading zeros
        String paddedPrefix = String.format("%06d", Long.parseLong(normalizedPrefix));
        String paddedAccount = String.format("%010d", Long.parseLong(account));

        // Create BBAN (Basic Bank Account Number): bankCode + paddedPrefix + paddedAccount
        String bban = bankCode + paddedPrefix + paddedAccount;

        // Calculate check digits
        String checkDigits = calculateCheckDigits(bban);

        // Construct IBAN
        return COUNTRY_CODE + checkDigits + bban;
    }

    private static void validateBankCode(String bankCode) {
        if (bankCode == null) {
            throw new IllegalArgumentException("Bank code cannot be null");
        }
        if (bankCode.isEmpty()) {
            throw new IllegalArgumentException("Bank code cannot be empty");
        }
        if (bankCode.length() != BANK_CODE_LENGTH) {
            throw new IllegalArgumentException("Bank code must be exactly 4 digits, got: " + bankCode.length());
        }
        if (!bankCode.matches("\\d+")) {
            throw new IllegalArgumentException("Bank code must contain only digits");
        }
    }

    private static boolean isModulo11(String number) {
        if (number == null || number.isEmpty()) {
            return true; // Empty prefix is allowed
        }
        if (!number.matches("\\d+")) {
            return false;
        }

        int sum = 0;
        int weight = 1;

        // Modulo 11 calculation from right to left
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            sum += digit * weight;
            weight = (weight * 2) % 11;
        }

        return (sum % 11 == 0);
    }

    private static void validateAccount(String account) {
        if (account == null) {
            throw new IllegalArgumentException("Account number cannot be null");
        }
        if (account.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }
        if (account.length() < MIN_ACCOUNT_LENGTH) {
            throw new IllegalArgumentException("Account number cannot be shorter than 2 digits, got: " + account.length());
        }
        if (account.length() > MAX_ACCOUNT_LENGTH) {
            throw new IllegalArgumentException("Account number cannot exceed 10 digits, got: " + account.length());
        }
        if (!account.matches("\\d+")) {
            throw new IllegalArgumentException("Account number must contain only digits");
        }
        // Account number should not be all zeros
        if (Long.parseLong(account) == 0) {
            throw new IllegalArgumentException("Account number cannot be zero");
        }
        if (!isModulo11(account)) {
            throw new IllegalArgumentException("Account number must have modulo 11 checksum");
        }
    }

    private static void validatePrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return; // Prefix is optional
        }
        if (prefix.length() > PREFIX_MAX_LENGTH) {
            throw new IllegalArgumentException("Prefix cannot exceed 6 digits, got: " + prefix.length());
        }
        if (!prefix.matches("\\d+")) {
            throw new IllegalArgumentException("Prefix must contain only digits");
        }
        if (!isModulo11(prefix)) {
            throw new IllegalArgumentException("Prefix must be valid modulo 11 number");
        }
    }

    /**
     * Calculates the IBAN check digits using mod-97 algorithm.
     */
    private static String calculateCheckDigits(String bban) {
        // Move country code to end and replace letters with numbers (C=12, Z=35)
        String rearranged = bban + "1235" + "00"; // CZ = 12,35 + provisional check digits 00

        // Calculate mod 97
        BigInteger ibanNumber = new BigInteger(rearranged);
        int remainder = ibanNumber.mod(BigInteger.valueOf(97)).intValue();

        // Check digit = 98 - remainder
        int checkDigit = 98 - remainder;

        return String.format("%02d", checkDigit);
    }

    /**
     * Formats IBAN with dashes for readability.
     */
    public static String formatIban(String iban) {
        if (iban.length() != IBAN_LENGTH) {
            throw new IllegalStateException("Invalid IBAN length: " + iban.length());
        }

        // Format: CZ## #### #### #### #### ####
        StringBuilder formatted = new StringBuilder();
        formatted.append(iban.substring(0, 4));  // CZ##

        for (int i = 4; i < iban.length(); i += 4) {
            formatted.append(" ");
            formatted.append(iban.substring(i, Math.min(i + 4, iban.length())));
        }

        return formatted.toString();
    }

    /**
     * Removes formatting from IBAN (spaces, dashes).
     */
    public static String unformatIban(String iban) {
        return iban.replaceAll("[\\s]", "");
    }
}

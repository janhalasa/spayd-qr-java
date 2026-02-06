package io.github.janhalasa.spaydqr.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class Payment {

    public static final String CURRENCY_CZK = "CZK";

    private BankAccount bankAccount;
    private List<BankAccount> alternativeBankAccounts;
    private BigDecimal amount;
    private String currencyCode;
    private LocalDate paymentDueDate;
    private String variableSymbol;
    private String constantSymbol;
    private String specificSymbol;
    private String originatorsReference;
    private String paymentNote;
    private String notificationType;
    private String notificationAddress;
    private Boolean instantPayment;
    private String beneficiaryName;
}

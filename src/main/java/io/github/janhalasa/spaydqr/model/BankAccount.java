package io.github.janhalasa.spaydqr.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BankAccount {

    private String iban;
    private String bic;

    public BankAccount(String iban) {
        this.iban = iban;
    }

    public BankAccount(String iban, String bic) {
        this.iban = iban;
        this.bic = bic;
    }
}

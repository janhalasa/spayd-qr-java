# SPAYD QR Java

Java library for generating QR for payments in SPAYD standard used in Czechia.
The result is either an "SPD String" or a QR code in PNG format.
The "SPD String" may be useful if you want to use a different QR code generator.

## Overview

Java implementation of the [Czech SPAYD](https://qr-platba.cz/pro-vyvojare/specifikace-formatu/) QR code standard for bank payments.
The following attributes are not supported:
* X-PER,
* X-ID,
* X-URL,
* custom attributes not listed in the standard.

Custom styling of the QR code is not supported - the result is just a bare QR code like this:

![Sample result](spayd-qr.png)

The library supports the CRC32 checksum implemented according to the specification, but I couldn't find any other
tool supporting this feature, so I couldn't verify the result.

## Installation

If you just want the SPAYD string generator, you can exclude the `com.google.zxing` dependencies.

### Maven

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.janhalasa</groupId>
        <artifactId>spayd-qr-java</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
    compile "io.github.janhalasa:spayd-qr-java:1.0.1"
}
```

## Usage

### Simple one-time payment

```java

import io.github.janhalasa.spaydqr.model.BankAccount;
import io.github.janhalasa.spaydqr.model.Payment;
import io.github.janhalasa.spaydqr.service.PayBySquareGenerator;

import java.time.LocalDate;

public class OneTimePayment {
    public static void main(String[] args) throws Exception {
        
        Payment payment = Payment.builder()
                .bankAccount(new BankAccount("CZ5508000000001234567899", "GIBACZPX"))
                .amount(new BigDecimal("123.45"))
                .currencyCode("CZK")
                .paymentDueDate(LocalDate.of(2029, 1, 31))
                .variableSymbol("22222")
                .paymentNote("ZPRAVA PRO PRIJEMCE")
                .build();
        String result = SpaydSerializer.serialize(payment);
        byte[] qrCodePng = SpaydQrCodeGenerator.generateQrCode(payment, 256);
    }
}
```

### Convert Czech bank account number to IBAN

To convert a Czech bank account number to IBAN, you can use the included `CzechIbanGenerator`:

```java
import io.github.janhalasa.spaydqr.service.CzechIbanGenerator;

public class Iban {
    public static void main(String[] args) {
        String iban = CzechIbanGenerator.composeCzechIban("0800", "1234567899");
        // CZ5508000000001234567899
        CzechIbanGenerator.formatIban(iban);
        // CZ55 0800 0000 0012 3456 7899
    }
}
```

## Tests

The library contains multiple tests. The results were compared with the output from the official [SPAYD HTTP API](https://qr-platba.cz/pro-vyvojare/restful-api/).

## License

MIT

package io.github.janhalasa.spaydqr.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.github.janhalasa.spaydqr.model.Payment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class SpaydQrCodeGenerator {

    public byte[] generateQrCode(Payment payment, int size)
            throws IOException, WriterException {
        String code = SpaydSerializer.serialize(payment);
        return generateQrCodeFromString(code, size);
    }

    public byte[] generateQrCode(Payment payment, int size, boolean includeChecksum, boolean normalizeStrings)
            throws IOException, WriterException {
        String code = SpaydSerializer.serialize(payment, includeChecksum, normalizeStrings);
        return generateQrCodeFromString(code, size);
    }

    public byte[] generateQrCodeFromString(String spaydString, int size) throws IOException, WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
        hints.put(EncodeHintType.MARGIN, 0);

        BitMatrix bitMatrix = qrCodeWriter.encode(spaydString, BarcodeFormat.QR_CODE, size, size, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}

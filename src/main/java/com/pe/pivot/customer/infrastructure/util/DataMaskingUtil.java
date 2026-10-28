package com.pe.pivot.customer.infrastructure.util;

public final class DataMaskingUtil {

    private DataMaskingUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Masks a document number leaving only the last 4 characters visible.
     *
     * @param documentNumber - plain document number
     * @return - masked string (e.g., "****4567")
     */
    public static String maskDocument(String documentNumber) {
        if(documentNumber == null || documentNumber.length() <= 4) {
            return "****";
        }

        int visibleDigits = 4;
        String tail = documentNumber.substring(documentNumber.length() - visibleDigits);
        return "*".repeat(documentNumber.length() - visibleDigits) + tail;
    }
}

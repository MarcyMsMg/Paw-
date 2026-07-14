package com.paw.adoptions.common;

import java.util.regex.Pattern;

// Acepta 912345678, +56912345678 o 56912345678 (celular chileno), con o sin espacios.
public final class ChilePhoneValidator {

    private static final Pattern PATTERN = Pattern.compile("^(\\+?56)?9\\d{8}$");

    private ChilePhoneValidator() {
    }

    public static boolean isValid(String phone) {
        if (phone == null) {
            return false;
        }
        String stripped = phone.replaceAll("\\s+", "");
        return PATTERN.matcher(stripped).matches();
    }
}

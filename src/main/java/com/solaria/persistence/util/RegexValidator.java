package com.solaria.persistence.util;

/**
 * Expressões REGEX, Usadas em @Pattern nos request DTOs.
 */
public final class RegexValidator {

    public static final String ZIP_CODE_REGEX = "^\\d{8}$";

    public static final String PHONE_REGEX = "^9\\d{8}$";

    public static final String URL_REGEX = "^[a-zA-Z][a-zA-Z0-9+\\-.]*://[^\\s/$.?#][^\\s]*$";

    public static final String CNPJ_REGEX = "^\\d{14}$";

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    public static final String CPF_REGEX = "^\\d{11}$";

    public static final String USERNAME_REGEX = "^[a-z0-9_]{3,30}$";

    private RegexValidator() {}

}

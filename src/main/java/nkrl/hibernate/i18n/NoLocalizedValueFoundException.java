package nkrl.hibernate.i18n;

import java.util.Arrays;
import java.util.Locale;

public class NoLocalizedValueFoundException extends RuntimeException {

    public NoLocalizedValueFoundException(Locale locale) {
        super("No localized value found in locale " + locale);
    }

    public NoLocalizedValueFoundException(Locale[] locales) {
        super("No localized value found in any of the locales " + Arrays.toString(locales));
    }

    public NoLocalizedValueFoundException(String value, Locale locale) {
        super("No localized value found for " + value + " in locale " + locale);
    }

    public NoLocalizedValueFoundException(String value, Locale locale, Throwable cause) {
        super("No localized value found for " + value + " in locale " + locale, cause);
    }
}

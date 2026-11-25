package com.reandroid.apkeditor.gui.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Traducciones de la interfaz. El bundle base contiene inglés y
 * messages_es.properties contiene español.
 */
public final class I18n {
    public enum Language {
        SPANISH("es", "Español"),
        ENGLISH("en", "English");

        private final String code;
        private final String label;

        Language(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final String BUNDLE = "i18n.messages";
    private static final ResourceBundle.Control CONTROL =
            ResourceBundle.Control.getNoFallbackControl(
                    ResourceBundle.Control.FORMAT_PROPERTIES);
    private static Language language = Language.SPANISH;
    private static ResourceBundle bundle = load(language);

    private I18n() {
    }

    public static Language getLanguage() {
        return language;
    }

    public static String getLanguageCode() {
        return language.getCode();
    }

    public static void setLanguage(String code) {
        setLanguage(fromCode(code));
    }

    public static void setLanguage(Language selected) {
        Language next = selected == null ? Language.SPANISH : selected;
        if (next == language) {
            return;
        }
        language = next;
        bundle = load(next);
    }

    public static Language fromCode(String code) {
        if (code != null && code.toLowerCase(Locale.ROOT).startsWith("en")) {
            return Language.ENGLISH;
        }
        return Language.SPANISH;
    }

    public static String t(String key) {
        if (key == null) {
            return "";
        }
        try {
            return bundle.getString(key);
        } catch (MissingResourceException error) {
            return key;
        }
    }

    public static String t(String key, Object... arguments) {
        String pattern = t(key);
        if (arguments == null || arguments.length == 0) {
            return pattern;
        }
        try {
            return MessageFormat.format(pattern, arguments);
        } catch (IllegalArgumentException error) {
            return pattern;
        }
    }

    private static ResourceBundle load(Language selected) {
        return ResourceBundle.getBundle(BUNDLE,
                Locale.forLanguageTag(selected.getCode()), CONTROL);
    }
}

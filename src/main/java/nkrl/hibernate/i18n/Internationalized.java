package nkrl.hibernate.i18n;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for an internationalized (translatable) Hibernate column that stores its values as JSON.
 *
 * @param <T> The type of the column content
 */
@Data
@NoArgsConstructor
@Embeddable
public class Internationalized<T> {

    //@Type(type = "json")
    //@Column(columnDefinition = "jsonb") or "hstore" for PostgreSQL plus @TypeDef("json", ...)
    // OR in Hibernate 6: @JdbcTypeCode(SqlTypes.JSON)
    /*
    @OneToMany(mappedBy = "product", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, orphanRemoval = true)
    @MapKey(name = "localizedId.locale")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Locale, T> contents = new HashMap<>();

    /**
     * The name of the attribute that stores the localizations. This is useful for overriding the column name in JPA.
     *
     * @see jakarta.persistence.AttributeOverride
     */
    public static final String I18N_ATTRIBUTE_NAME = "contents";

    /**
     * Determines which of the stored localizations best matches the locale preferences. This method respects
     * the fallback locale if a fallback was set.
     *
     * @param preferredLocales An array of locales ordered by preference from highest to lowest
     * @return The preferred locale or the fallback locale or {@literal null} if no localization matches the preferences
     */
    private Locale preferredLocalization(Locale... preferredLocales) {
        Locale preferred = Locale.lookup(Arrays.stream(preferredLocales)
                .map(locale -> new Locale.LanguageRange(locale.toLanguageTag()))
                .collect(Collectors.toList()), contents.keySet());
        return (preferred == null && contents.containsKey(Locale.ROOT)) ? Locale.ROOT : preferred;
    }

    /**
     * Gets the localized value or throws an exception.
     *
     * @param locale
     * @return The localized value
     * @throws NoLocalizedValueFoundException If no localization for the given locale exists
     */
    public T getOrFail(Locale locale) {
        if (!hasLocalizationFor(locale)) {
            throw new NoLocalizedValueFoundException(locale);
        }
        return contents.get(locale);
    }

    /**
     * Gets the localized value.
     *
     * @param locale
     * @return The localized value or {@literal null} if such a localization does not exist
     */
    public T get(Locale locale) {
        return contents.get(preferredLocalization(locale));
    }

    /**
     * Gets the localized value as an {@link Optional}.
     *
     * @param locale
     *
     * @return An Optional containing the localized value or an empty Optional if no localized value exists or the value is set to {@literal null}
     */
    public Optional<T> getOptional(Locale locale) {
        return Optional.ofNullable(contents.get(preferredLocalization(locale)));
    }

    public T getOrFallbackTo(Locale... locales) {
        Locale preferredLocale = preferredLocalization(locales);
        if (preferredLocale == null) {
            throw new NoLocalizedValueFoundException(locales);
        }
        return contents.get(preferredLocale);
    }

    /**
     * Determines which of the preferred locales has a localization.
     *
     * @param locales An array of locales, ordered by preference from highest to lowest
     * @return An Optional containing the locale if any of the localizations matches the provided locales, or an empty Optional.
     */
    public Optional<T> getLocale(Locale... locales) {
        for (Locale locale : locales) {
            if (!hasLocalizationFor(locale)) {
                continue;
            }
            return Optional.ofNullable(contents.get(locale));
        }
        // TODO: let #preferredLocaloization() return Optional<Locale> and use Optional#orDefault
        return Optional.empty();
    }

    /**
     * Sets a localized value for this attribute. Existing values will be overridden.
     *
     * @param locale The locale
     * @param value The localized value
     */
    public void set(Locale locale, T value) {
        Objects.requireNonNull(locale);
        contents.put(locale, value);
    }

    /**
     * This sets a locale-independent fallback value that is used for all locales unless
     * another, locale-specific value overrides it.
     * <p/>
     * This is useful in situations where the entity attribute is generally internationalized, but some
     * individual entities do not require localization, that is, the localized values would be the same
     * for all (or at least many) entities. Setting a fallback value eliminates this redundancy.
     *
     * @param value The fallback value
     */
    public void set(T value) {
        contents.put(Locale.ROOT, value);
    }

    /**
     * Tests if this attribute has a localization suitable for the given locale. This may be a more generic
     * locale than the given one, e.g. if Swiss German is requested, the locale German (without a country)
     * is considered a match.
     *
     * @param locale The locale to test
     * @return {@literal true} if a suitable localization was found, otherwise {@literal false}
     */
    public boolean hasLocalizationFor(Locale locale) {
        return contents.containsKey(preferredLocalization(locale));
    }

    public Set<Locale> localizations() {
        return Collections.unmodifiableSet(contents.keySet());
    }

    // TODO needs access to repository to find records in other locales?

    // TODO static: empty(); of(Locale, value);
}


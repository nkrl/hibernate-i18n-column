package nkrl.hibernate.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

public class LocalizedContentTest {

    private Internationalized<String> column;

    @BeforeEach
    public void createColumn() {
        column = new Internationalized<>();
    }

    @Test
    public void setShouldStoreLocalization() {
        assertThat(column.getContents()).isEmpty();

        column.set(Locale.US, "Pew pew pew");
        assertThat(column.getContents()).containsOnly(entry(Locale.US, "Pew pew pew"));

        column.set(Locale.US, "Pew pew pew pew pew pew");
        assertThat(column.getContents()).containsOnly(entry(Locale.US, "Pew pew pew pew pew pew"));

        column.set(Locale.GERMANY, "Ratatatatat");
        assertThat(column.getContents()).containsExactly(entry(Locale.US, "Pew pew pew pew pew pew"), entry(Locale.GERMANY, "Ratatatatat"));
    }

    @Test
    public void setFallbackShouldStoreLocalization() {
        column.set("123");
        assertThat(column.getContents()).containsValue("123");
        assertThat(column.get(Locale.GERMANY)).isEqualTo("123");
        assertThat(column.get(Locale.ENGLISH)).isEqualTo("123");
    }

    @Test
    public void fallbackShouldGetOverridden() {
        column.set("123");
        column.set(Locale.GERMANY, "eins zwei drei");
        assertThat(column.get(Locale.ENGLISH)).isEqualTo("123");
        assertThat(column.get(Locale.GERMANY)).isEqualTo("eins zwei drei");
    }

    @Test
    public void hasShouldFindDirectLocalization() {
        assertThat(column.hasLocalizationFor(Locale.US)).isFalse();

        column.set(Locale.US, "Pew");
        assertThat(column.hasLocalizationFor(Locale.US)).isTrue();
        assertThat(column.hasLocalizationFor(Locale.UK)).isFalse();
    }

    @Test
    public void hasShouldFindIndirectLocalization() {
        column.set(new Locale("de"), "Hallo!");
        //column.set(new Locale("de", "AT"), "Servus!");

        assertThat(column.hasLocalizationFor(Locale.GERMANY)).isTrue();
    }

    @Test
    public void getShouldFailIfEmpty() {
        assertThat(column.get(Locale.US)).isNull();

        assertThatExceptionOfType(NoLocalizedValueFoundException.class).isThrownBy(() -> {
            column.getOrFallbackTo(Locale.US);
        });

        assertThat(column.getOptional(Locale.US)).isEmpty();
    }

    @Test
    public void getShouldFindDirectLocalization() {
        column.set(Locale.US, "Pew");
        assertThat(column.get(Locale.US)).isEqualTo("Pew");

        column.set(Locale.GERMANY, "Peng");
        assertThat(column.get(Locale.GERMANY)).isEqualTo("Peng");
        assertThat(column.get(Locale.US)).isEqualTo("Pew");
    }

    @Test
    public void getShouldFindIndirectLocalizations() {
        column.set(new Locale("de"), "Hallo!");
        column.set(new Locale("de", "AT"), "Servus!");
        column.set(new Locale("de", "CH"), "Grüezi!");

        assertThat(column.get(Locale.GERMANY)).isEqualTo("Hallo!");
    }

    @Test
    public void fallbacksShouldRespectOrder() {
        column.set(Locale.US, "Howdy");
        assertThat(column.getOrFallbackTo(Locale.US)).isEqualTo("Howdy");
        assertThat(column.getOrFallbackTo(Locale.UK, Locale.US)).isEqualTo("Howdy");

        column.set(Locale.UK, "Oi");
        assertThat(column.getOrFallbackTo(Locale.UK, Locale.US)).isEqualTo("Oi");
        assertThat(column.getOrFallbackTo(Locale.US, Locale.UK)).isEqualTo("Howdy");
    }
}

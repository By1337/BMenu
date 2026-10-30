package dev.by1337.bmenu.compat;

import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.edsl.Themes;
import dev.by1337.yaml.YamlMap;
import net.kyori.adventure.text.Component;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MenuTitleTest {
    @Test
    void localizedTitleUsesPlayerLanguageAndConfiguredFallback() {
        var previous = Themes.defaultLocale;
        try {
            Themes.defaultLocale = Locale.US;
            var config = decode("""
                    title:
                      ru_RU: '&aРусский {value}'
                      en_US: '&aEnglish {value}'
                    """);
            assertEquals("&aРусский {value}", config.title(Locale.forLanguageTag("ru-RU")));
            assertEquals("&aРусский {value}", config.title(Locale.forLanguageTag("ru-BY")));
            assertEquals("&aEnglish {value}", config.title(Locale.US));
            assertEquals("&aEnglish {value}", config.title(Locale.FRANCE));
            assertEquals("&aEnglish {value}", config.title());
            Themes.defaultLocale = Locale.GERMANY;
            var onlyRussian = decode("title: {ru_RU: Русский}");
            assertEquals("Русский", onlyRussian.title(Locale.FRANCE));
        } finally {
            Themes.defaultLocale = previous;
        }
    }

    @Test
    void scalarAndAbsentTitlesKeepOldApi() {
        var config = decode("title: Plain {value}");
        assertEquals("Plain {value}", config.title());
        assertEquals(config.title(), config.title(Locale.US));
        assertNull(decode("size: 27").title(Locale.US));
        var custom = new MenuConfig() {
            @Override
            public String title() {
                return "Custom";
            }
        };
        assertEquals("Custom", custom.title(Locale.US));
    }

    @Test
    void rawTitleResolvesThemeBeforeLiveMenuPlaceholders() {
        var theme = new AtomicReference<>("first");
        var value = new AtomicReference<>("42");
        Themes.setSelector(player -> theme.get());
        Themes.setThemeReplacer(id -> text -> text.replace("{theme}", id + " {value}"));
        try {
            var config = decode("""
                    title:
                      ru_RU: '{theme} RU'
                      en_US: '{theme} EN'
                    """);
            var text = config.titleText(Locale.US);
            assertSame(text, config.titleText(Locale.US));
            assertEquals(Component.text("first 42 EN"), text.asComponent(null,
                    input -> input.replace("{value}", value.get())));
            value.set("43");
            assertEquals(Component.text("first 43 EN"), text.asComponent(null,
                    input -> input.replace("{value}", value.get())));
            theme.set("second");
            assertEquals(Component.text("second 43 EN"), text.asComponent(null,
                    input -> input.replace("{value}", value.get())));
            assertEquals("Plain", decode("title: Plain").titleText(Locale.US).source());
            assertNull(decode("size: 27").titleText(Locale.US));
        } finally {
            Themes.setSelector(null);
            Themes.setThemeReplacer(null);
        }
    }

    @Test
    void invalidLocaleMapsAreRejected() {
        for (var yaml : new String[]{ "title: {}", "title: {'ru_???': bad}",
                "title: {ru_RU: first, ru-RU: second}" }) {
            assertTrue(MenuConfig.DECODER.decode(YamlMap.loadFromString(yaml).get()).hasError(), yaml);
        }
    }

    private MenuConfig decode(String yaml) {
        return MenuConfig.DECODER.decode(YamlMap.loadFromString(yaml).get()).getOrThrow();
    }
}

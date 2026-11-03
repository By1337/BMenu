package dev.by1337.bmenu.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class LocalizedConfigsTest {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$?\\{[^{}\\r\\n]+}|%[\\w:]+%");
    private final Path generated = Path.of(System.getProperty("bmenu.generatedConfigs", "target/generated-configs"));

    @Test
    void bothLocalesPreserveConfigStructureAndPlaceholders() throws Exception {
        List<Path> russian = relativeFiles(generated.resolve("ru"));
        assertFalse(russian.isEmpty());
        assertEquals(russian, relativeFiles(generated.resolve("en")));
        for (Path relative : russian) {
            String ru = Files.readString(generated.resolve("ru").resolve(relative));
            String en = Files.readString(generated.resolve("en").resolve(relative));
            assertFalse(ru.contains("@locale:"), relative.toString());
            assertFalse(en.contains("@locale:"), relative.toString());
            assertSameStructure(documents(ru), documents(en), relative.toString());
        }
    }

    @Test
    void localizationDemoKeepsBothLanguagesAndDebugRemainsShared() throws Exception {
        for (String locale : List.of("ru", "en")) {
            String demo = Files.readString(generated.resolve(locale).resolve("menu/localization.yml"));
            assertTrue(demo.contains("ru_RU: '&8Пример локализации'"));
            assertTrue(demo.contains("en_US: '&8Localization demo'"));
            assertFalse(Files.exists(generated.resolve(locale).resolve("menu/debug.yml")));
        }
    }

    @Test
    void templatesPairLocaleLinesAndAvoidCommentsAfterAnchors() throws Exception {
        Path templates = Path.of("src/main/config-templates");
        for (Path relative : relativeFiles(templates)) {
            List<String> lines = Files.readAllLines(templates.resolve(relative));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String location = relative + ":" + (i + 1);
                assertFalse(!line.stripLeading().startsWith("#") && line.matches(".*:\\s+&[\\w-]+\\s+#.*"), location);
                if (line.contains("@locale:")) {
                    assertTrue(line.endsWith(" # @locale:ru") || line.endsWith(" # @locale:en"), location);
                    if (line.endsWith(" # @locale:ru")) {
                        assertTrue(i + 1 < lines.size() && lines.get(i + 1).endsWith(" # @locale:en"), location);
                    } else {
                        assertTrue(i > 0 && lines.get(i - 1).endsWith(" # @locale:ru"), location);
                    }
                }
                if (!line.stripLeading().startsWith("#") && line.matches(".*:\\s+&[\\w-]+\\s*")) {
                    int next = i + 1;
                    while (next < lines.size() && lines.get(next).isBlank()) next++;
                    assertFalse(next < lines.size() && lines.get(next).stripLeading().startsWith("#"), location);
                }
            }
        }
    }

    private List<Path> relativeFiles(Path root) throws Exception {
        try (var files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).map(root::relativize).sorted().toList();
        }
    }

    private List<Object> documents(String input) {
        List<Object> documents = new ArrayList<>();
        new Yaml(new LoaderOptions()).loadAll(input).forEach(documents::add);
        return documents;
    }

    private void assertSameStructure(Object ru, Object en, String path) {
        assertSameStructure(ru, en, path, new IdentityHashMap<>());
    }

    private void assertSameStructure(Object ru, Object en, String path, Map<Object, Object> visited) {
        if (ru instanceof Map<?, ?> || ru instanceof List<?>) {
            if (visited.containsKey(ru)) {
                assertSame(visited.get(ru), en, path);
                return;
            }
            visited.put(ru, en);
        }
        if (ru instanceof Map<?, ?> map) {
            assertTrue(en instanceof Map<?, ?>, path);
            Map<?, ?> translated = (Map<?, ?>) en;
            assertEquals(map.keySet(), translated.keySet(), path);
            map.forEach((key, value) -> assertSameStructure(value, translated.get(key), path + "." + key, visited));
        } else if (ru instanceof List<?> list) {
            assertTrue(en instanceof List<?>, path);
            List<?> translated = (List<?>) en;
            assertEquals(list.size(), translated.size(), path);
            for (int i = 0; i < list.size(); i++) {
                assertSameStructure(list.get(i), translated.get(i), path + "[" + i + "]", visited);
            }
        } else if (ru instanceof String text) {
            assertTrue(en instanceof String, path);
            assertEquals(placeholders(text), placeholders((String) en), path);
        } else {
            assertEquals(ru, en, path);
        }
    }

    private Map<String, Integer> placeholders(String text) {
        Map<String, Integer> counts = new TreeMap<>();
        PLACEHOLDER.matcher(text).results().forEach(match -> counts.merge(match.group(), 1, Integer::sum));
        return counts;
    }
}

package dev.by1337.bmenu.animation.opcode;

import dev.by1337.yaml.YamlMap;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ProductionAnimationValidationTest {
    @Test
    void parsesActualMaterialOpcodesWhenConfigDirectoryIsProvided() throws Exception {
        String directory = System.getProperty("bmenu.animationRoot");
        assumeTrue(directory != null);
        AtomicInteger checked = new AtomicInteger();
        try (var paths = Files.walk(Path.of(directory))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml")).toList()) {
                YamlMap yaml = YamlMap.load(path.toFile());
                inspect(yaml.get("animation").getRaw(), path, checked);
                inspect(yaml.get("animations").getRaw(), path, checked);
            }
        }
        assertTrue(checked.get() > 0);
        System.out.println("Validated production material opcodes: " + checked.get());
    }

    private void inspect(Object value, Path file, AtomicInteger checked) {
        if (value instanceof Map<?, ?> map) {
            map.forEach((key, entry) -> {
                switch (String.valueOf(key)) {
                    case "set", "st" -> { assertDoesNotThrow(() -> new SetAnimOpcode(String.valueOf(entry)), file.toString()); checked.incrementAndGet(); }
                    case "set-if-empty", "sie" -> { assertDoesNotThrow(() -> new SetIfEmptyAnimOpcode(String.valueOf(entry)), file.toString()); checked.incrementAndGet(); }
                    case "fill", "fl" -> { assertDoesNotThrow(() -> new FillAnimOpcode(String.valueOf(entry)), file.toString()); checked.incrementAndGet(); }
                    default -> inspect(entry, file, checked);
                }
            });
        } else if (value instanceof Iterable<?> values) {
            values.forEach(entry -> inspect(entry, file, checked));
        }
    }
}

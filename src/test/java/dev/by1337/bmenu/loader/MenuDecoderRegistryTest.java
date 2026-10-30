package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.loader.v2.MenuDecoderImpl;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.DecodeContext;
import dev.by1337.yaml.decoder.PipelineYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.helpers.NOPLogger;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuDecoderRegistryTest {
    @TempDir
    Path directory;

    static class CustomConfig extends MenuConfig {
        static final YamlDecoder<CustomConfig> DECODER = PipelineYamlDecoder.of(CustomConfig::new)
                .and(MenuConfig.RAW_DECODER)
                .field(YamlDecoder.INT, "custom", (m, value) -> m.custom = value)
                .build();
        int custom;
    }

    @Test
    void decoderRegistrationPreservesContextAndSupportsLegacyView() {
        var registry = new MenuCodecRegistry();
        var expected = new MenuConfig();
        YamlDecoder<MenuConfig> decoder = (ctx, yaml) -> ctx.get(MenuConfig.class);
        var key = NamespacedKey.fromString("test:menu");
        registry.register(key, decoder);
        assertSame(decoder, registry.getDecoder(key));
        assertSame(decoder, registry.getDecoder("test:menu"));
        assertSame(decoder, registry.getDecoder("menu"));
        assertSame(expected, registry.getDecoder(key).decode(DecodeContext.of(expected), YamlMap.loadFromString("{}").get()).getOrThrow());
        var codec = registry.get(key);
        assertSame(codec, registry.get("menu"));
        assertSame(decoder, codec.asDecoder());
        registry.unregister(codec);
        assertNull(registry.getDecoder(key));
        assertEquals(0, registry.size());
    }

    @Test
    void legacyRegistrationKeepsCodecIdentityAndUnregistersThroughDecoder() {
        var registry = new MenuCodecRegistry();
        var codec = YamlCodec.of(CustomConfig.DECODER);
        registry.register("test:custom", codec);
        assertSame(codec, registry.get("test:custom"));
        assertSame(CustomConfig.DECODER, registry.getDecoder("custom"));
        registry.unregister(CustomConfig.DECODER);
        assertNull(registry.get("custom"));
        assertEquals(0, registry.size());
    }

    @Test
    void replacementAndAmbiguousShortNamesStayConsistent() {
        var registry = new MenuCodecRegistry();
        registry.register("first:custom", MenuConfig.DECODER);
        registry.register("first:custom", CustomConfig.DECODER);
        assertEquals(1, registry.size());
        assertSame(CustomConfig.DECODER, registry.getDecoder("custom"));
        registry.register("second:custom", MenuConfig.DECODER);
        assertNull(registry.getDecoder("custom"));
        assertNull(registry.get("custom"));
        registry.unregister(MenuConfig.DECODER);
        assertSame(CustomConfig.DECODER, registry.getDecoder("custom"));
    }

    @Test
    void loaderDecodesNativeAndLegacyProvidersIncludingSubLoaders() {
        var plugin = mock(Plugin.class);
        when(plugin.getSLF4JLogger()).thenReturn(NOPLogger.NOP_LOGGER);
        var loader = new MenuLoader(directory.toFile(), plugin);
        loader.codecRegistry().register("test:native", CustomConfig.DECODER);
        var legacy = YamlCodec.of(CustomConfig.DECODER);
        var subLoader = new MenuSubLoader(directory.toFile(), plugin, loader);
        subLoader.menuCodecRegistry().register("test:legacy", legacy);
        loader.registerSubLoader(plugin, subLoader);
        assertSame(CustomConfig.DECODER, loader.findMenuDecoder("test:native"));
        assertSame(CustomConfig.DECODER, loader.findMenuDecoder("legacy"));
        assertSame(legacy, loader.findMenuCodec("test:legacy"));
        for (var provider : new String[]{ "test:native", "legacy" }) {
            var result = new MenuDecoderImpl(loader).decode(YamlMap.loadFromString(
                    "provider: " + provider + "\ntitle: Custom\ncustom: 42\n"));
            var config = (CustomConfig) result.getOrThrow();
            assertEquals(42, config.custom);
            assertEquals("Custom", config.title());
        }
        assertTrue(new MenuDecoderImpl(loader).decode(YamlMap.loadFromString("provider: missing")).hasError());
    }
}

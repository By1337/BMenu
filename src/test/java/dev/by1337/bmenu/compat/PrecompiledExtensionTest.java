package dev.by1337.bmenu.compat;

import dev.by1337.bmenu.loader.MenuConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Pass -Dbmenu.compat.previousJar=/path/to/previous/BMenu.jar to check real old bytecode. */
class PrecompiledExtensionTest {
    @TempDir
    Path temp;

    @Test
    void extensionCompiledAgainstPreviousJarLoadsWithoutRecompilation() throws Exception {
        String previous = System.getProperty("bmenu.compat.previousJar");
        assumeTrue(previous != null, "A previous BMenu jar was not supplied");
        assertTrue(Files.isRegularFile(Path.of(previous)));
        var source = temp.resolve("OldExtension.java");
        Files.writeString(source, """
                import dev.by1337.bmenu.loader.MenuConfig;
                import dev.by1337.bmenu.loader.MenuCodecRegistry;
                import dev.by1337.bmenu.factory.MenuCodecs;
                import dev.by1337.bmenu.command.Commands;
                import dev.by1337.yaml.YamlMap;
                import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
                import dev.by1337.yaml.codec.YamlCodec;

                public class OldExtension extends MenuConfig {
                    public static final YamlCodec<OldExtension> CODEC = PipelineYamlCodecBuilder.of(OldExtension::new)
                        .and(MenuConfig.RAW_CODEC)
                        .field(MenuCodecs.SLOTS_CODEC, "slots", v -> v.slots, (v, slots) -> v.slots = slots)
                        .build();
                    public int[] slots;
                    public static MenuConfig load() {
                        MenuCodecRegistry registry = new MenuCodecRegistry();
                        registry.register("old:extension", CODEC);
                        return registry.get("old:extension").decode(YamlMap.loadFromString(
                            "title: Old binary\\nslots: 1-3\\nmenu-events:\\n  on_open: '[message] old'\\n").get()).getOrThrow();
                    }
                }
                """);
        // The old jar is first: javac resolves BMenu symbols against the previous release.
        String classpath = previous + java.io.File.pathSeparator + System.getProperty("java.class.path");
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Run with a JDK");
        assertEquals(0, compiler.run(null, null, null,
                "-classpath", classpath, "-d", temp.toString(), source.toString()));
        // Only the extension bytecode is loaded here; its parent supplies the new BMenu.
        try (var loader = new URLClassLoader(new java.net.URL[]{ temp.toUri().toURL() }, getClass().getClassLoader())) {
            var extension = loader.loadClass("OldExtension");
            var config = (MenuConfig) extension.getMethod("load").invoke(null);
            assertEquals("Old binary", config.title());
            assertArrayEquals(new int[]{ 1, 2, 3 }, (int[]) extension.getField("slots").get(config));
            assertNotNull(config.eventHandlers().get("on_open"));
        }
    }
}

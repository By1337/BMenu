package dev.by1337.bmenu.legacy;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BMenuBridgeTest {
    private static final String RESOURCE = "bridges/BMenu-1.7+legacy.jar";
    @TempDir
    Path directory;

    private Plugin plugin() {
        var plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        when(plugin.getResource(RESOURCE)).thenAnswer(call -> getClass().getClassLoader().getResourceAsStream(RESOURCE));
        return plugin;
    }

    @Test
    void extractsBundledLegacyJarWithoutADownload() throws Exception {
        Path jar = BMenuBridge.unpack(plugin());
        try (var resource = getClass().getClassLoader().getResourceAsStream(RESOURCE)) {
            assertNotNull(resource, "The build must embed the legacy jar");
            assertArrayEquals(resource.readAllBytes(), Files.readAllBytes(jar));
        }
        try (var archive = new JarFile(jar.toFile())) {
            assertNotNull(archive.getEntry("org/by1337/bmenu/BMenu.class"));
            assertNotNull(archive.getEntry("org/by1337/bmenu/Menu.class"));
        }
        assertEquals(directory.resolve(".bridges/BMenu-1.7+legacy.jar"), jar);
    }

    @Test
    void reusesExistingBridgeWithoutOpeningTheResource() throws Exception {
        var first = BMenuBridge.unpack(plugin());
        byte[] bytes = Files.readAllBytes(first);
        var plugin = plugin();
        when(plugin.getResource(RESOURCE)).thenThrow(new AssertionError("The bridge is already extracted"));
        assertEquals(first, BMenuBridge.unpack(plugin));
        assertArrayEquals(bytes, Files.readAllBytes(first));
    }

    @Test
    void missingResourceDoesNotCreateAnEmptyJar() {
        var plugin = plugin();
        when(plugin.getResource(RESOURCE)).thenReturn(null);
        var error = assertThrows(IOException.class, () -> BMenuBridge.unpack(plugin));
        assertTrue(error.getMessage().contains(RESOURCE));
        assertFalse(Files.exists(directory.resolve(".bridges/BMenu-1.7+legacy.jar")));
    }

    @Test
    void failedExtractionLeavesNoPartialJarAndCanBeRetried() throws Exception {
        var plugin = plugin();
        when(plugin.getResource(RESOURCE)).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Failed resource read");
            }
        });
        assertThrows(IOException.class, () -> BMenuBridge.unpack(plugin));
        try (var files = Files.list(directory.resolve(".bridges"))) {
            assertEquals(0, files.count());
        }
        assertTrue(Files.isRegularFile(BMenuBridge.unpack(plugin())));
    }
}

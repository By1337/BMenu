package dev.by1337.bmenu.compat;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.loader.MenuCodecRegistry;
import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuConfigExtensionTest {
    static class BuyerConfig extends MenuConfig {
        static final YamlCodec<BuyerConfig> CODEC = PipelineYamlCodecBuilder.of(BuyerConfig::new)
                .and(MenuConfig.RAW_CODEC)
                .field(MenuCodecs.SLOTS_CODEC, "putable_slots", v -> v.putableSlots, (v, slots) -> v.putableSlots = slots)
                .build();
        int[] putableSlots;
    }

    static class LotsConfig extends MenuConfig {
        static final PipelineYamlCodecBuilder<LotsConfig> RAW_CODEC = PipelineYamlCodecBuilder.of(LotsConfig::new)
                .and(MenuConfig.RAW_CODEC)
                .integer("count", v -> v.count, (v, count) -> v.count = count);
        int count;
    }

    static class VaultConfig extends LotsConfig {
        static final YamlCodec<VaultConfig> CODEC = PipelineYamlCodecBuilder.of(VaultConfig::new)
                .and(LotsConfig.RAW_CODEC)
                .string("economy", v -> v.economy, (v, economy) -> v.economy = economy)
                .build();
        String economy;
    }

    @Test
    void buyerStyleCodecCanExtendAndRegisterBaseFieldsWithoutChanges() {
        var registry = new MenuCodecRegistry();
        registry.register("buyer:sell", BuyerConfig.CODEC);
        var config = (BuyerConfig) registry.get("buyer:sell").decode(YamlMap.loadFromString("""
                id: buyer:sell
                title: Shop
                size: 27
                putable_slots: 0-2
                menu-events:
                  on_open:
                    requirements:
                      - check: 'true'
                    commands: '[message] open'
                """).get()).getOrThrow();
        assertEquals("Shop", config.title());
        assertEquals(27, config.size());
        assertArrayEquals(new int[]{ 0, 1, 2 }, config.putableSlots);
        assertNotNull(config.eventHandlers().get("on_open"));
        assertEquals("buyer:sell", config.id().toString());
    }

    @Test
    void menuItemsUseDecoderForOldSlotsVisibilityAndTicking() {
        try (var bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            var server = mock(org.bukkit.Server.class);
            bukkit.when(org.bukkit.Bukkit::getServer).thenReturn(server);
            var decoded = MenuConfig.DECODER.decode(YamlMap.loadFromString("""
                    title: Legacy shop
                    items:
                      button:
                        material: stone
                        display_name: Old name
                        slots: 0-2
                        view_req: 'true'
                        ticking: true
                        tick-speed: 5
                    """).get());
            assertFalse(decoded.hasError(), () -> String.valueOf(decoded.error()));
            var slot = decoded.getOrThrow().idToItems().get("button");
            assertArrayEquals(new int[]{ 0, 1, 2 }, slot.slots());
            assertNotNull(slot.build());
            assertEquals("1.16.5", dev.by1337.core.ServerVersion.CURRENT_ID);
        }
    }

    @Test
    void dumpPreservesBaseAndExtensionYamlWithoutAnEncoder(@org.junit.jupiter.api.io.TempDir java.nio.file.Path directory) throws Exception {
        var config = BuyerConfig.CODEC.decode(YamlMap.loadFromString("""
                id: buyer:sell
                title: Shop
                putable_slots: 0-2
                """).get()).getOrThrow();
        config.dump(directory, "saved");
        var reloaded = BuyerConfig.CODEC.decode(YamlMap.load(directory.resolve("saved.yml").toFile()).get()).getOrThrow();
        assertEquals("Shop", reloaded.title());
        assertArrayEquals(config.putableSlots, reloaded.putableSlots);
    }

    @Test
    void auctionStyleCodecCanBeExtendedThroughMultipleInheritanceLevels() {
        var config = VaultConfig.CODEC.decode(YamlMap.loadFromString("""
                title: Auction
                count: 9
                economy: coins
                """).get()).getOrThrow();
        assertEquals("Auction", config.title());
        assertEquals(9, config.count);
        assertEquals("coins", config.economy);
    }
}

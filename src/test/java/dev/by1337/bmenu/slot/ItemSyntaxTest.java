package dev.by1337.bmenu.slot;

import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.factory.fixer.ItemFixer;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.handler.ConditionalHandler;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.yaml.YamlMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemSyntaxTest {
    @Test
    void normalizesAndParsesLegacySlotFields() {
        var yaml = YamlMap.loadFromString("""
                slots: '10-12'
                view_req: 'true'
                tick-speed: 5
                ticking: true
                """);
        ItemFixer.fixItem(yaml);

        assertArrayEquals(new int[]{10, 11, 12}, yaml.get("slot").decode(MenuCodecs.SLOTS_CODEC).getOrThrow());
        assertEquals("true", yaml.get("on_view").asString().getOrThrow());
        assertEquals(5, yaml.get("tick_speed").asInt().getOrThrow());
        assertEquals("[rebuild]", Commands.CODEC.decode(yaml.get("on_tick")).getOrThrow()
                .encode().asCollection().getOrThrow().iterator().next());
    }

    @Test
    void parsesModernSlotFields() {
        var yaml = YamlMap.loadFromString("""
                slot: '20,22-23'
                on_view:
                  if: 'true'
                  do: '[message] visible'
                tick_speed: 4
                on_tick: '[rebuild]'
                """);
        ItemFixer.fixItem(yaml);

        assertArrayEquals(new int[]{20, 22, 23}, yaml.get("slot").decode(MenuCodecs.SLOTS_CODEC).getOrThrow());
        assertTrue(Requirement.CODEC.decode(yaml.get("on_view")).getOrThrow() instanceof ConditionalHandler);
        assertEquals(4, yaml.get("tick_speed").asInt().getOrThrow());
        assertEquals("[rebuild]", yaml.get("on_tick").asString().getOrThrow());
    }
}

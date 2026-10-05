package dev.by1337.bmenu.command;

import dev.by1337.bmenu.handler.BreakableConditionalHandler;
import dev.by1337.bmenu.handler.ConditionalHandler;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.yaml.YamlMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandsCodecTest {
    @Test
    void parsesLegacyCheckAndCommandKeys() {
        var yaml = YamlMap.loadFromString("""
                check: 'true'
                commands: '[message] allowed'
                deny_commands: '[message] denied'
                """);
        var handler = ConditionalHandler.CODEC.decode(yaml.get()).getOrThrow();

        assertTrue(handler.req().test(null, null));
        assertEquals("[message] allowed", handler.doCommands().encode().asCollection().getOrThrow().iterator().next());
        assertEquals("[message] denied", handler.elseCommands().encode().asCollection().getOrThrow().iterator().next());
    }

    @Test
    void parsesLegacyRequirementsBlock() {
        var yaml = YamlMap.loadFromString("""
                requirements:
                  - check: 'false'
                    commands: '[message] allowed'
                    deny_commands: '[message] denied'
                commands: '[message] success'
                deny_commands: '[message] failure'
                """);
        var handler = BreakableConditionalHandler.CODEC.decode(yaml.get()).getOrThrow();

        assertEquals(1, handler.handlers().size());
        assertTrue(handler.handlers().get(0) instanceof ConditionalHandler);
        assertEquals("[message] success", handler.doCmds().encode().asCollection().getOrThrow().iterator().next());
        assertEquals("[message] failure", handler.elseCmds().encode().asCollection().getOrThrow().iterator().next());
    }

    @Test
    void parsesModernConditionalAndCommandList() {
        var yaml = YamlMap.loadFromString("""
                value:
                  - if: 'true'
                    do:
                      - '[message] allowed'
                      - '[close]'
                    else: '[message] denied'
                  - console: 'say hello'
                """);
        var commands = Commands.CODEC.decode(yaml.get("value")).getOrThrow();
        var encoded = commands.encode().asCollection().getOrThrow();

        assertEquals(2, encoded.size());
        assertTrue(encoded.iterator().next() instanceof java.util.Map);
        assertEquals("[console] say hello", encoded.stream().skip(1).findFirst().orElseThrow());
    }

    @Test
    void parsesLegacyTypedRequirementAndModernBooleanGroup() {
        var legacy = YamlMap.loadFromString("""
                type: hp
                permission: bmenu.open
                commands: '[message] allowed'
                """);
        var modern = YamlMap.loadFromString("""
                if-one:
                  - 'false'
                  - 'true'
                do: '[message] allowed'
                """);

        assertTrue(Requirement.CODEC.decode(legacy.get()).getOrThrow() instanceof ConditionalHandler);
        var handler = ConditionalHandler.CODEC.decode(modern.get()).getOrThrow();
        assertTrue(handler.req().test(null, null));
    }

    @Test
    void parsesLegacyTypedRequirementVariants() {
        var cases = java.util.Map.of(
                "m", "expression: '1 == 1'",
                "se", "input: same\noutput: same",
                "sei", "input: Same\noutput: same",
                "sc", "input: abcd\noutput: bc",
                "rm", "regex: '[a-z]+'\ninput: abc",
                "hp", "permission: bmenu.open"
        );
        cases.forEach((type, fields) -> {
            var yaml = YamlMap.loadFromString("type: " + type + "\n" + fields + "\n");
            assertTrue(Requirement.CODEC.decode(yaml.get()).getOrThrow() instanceof ConditionalHandler, type);
        });
    }

    @Test
    void parsesModernFirstMatch() {
        var yaml = YamlMap.loadFromString("""
                oneOf:
                  - if: 'false'
                    do: '[message] first'
                  - if: 'true'
                    do: '[message] second'
                """);
        var commands = Commands.CODEC.decode(yaml.get()).getOrThrow();
        var encoded = commands.encode().asCollection().getOrThrow();

        assertEquals(1, encoded.size());
        assertTrue(encoded.iterator().next() instanceof java.util.List);
    }
}

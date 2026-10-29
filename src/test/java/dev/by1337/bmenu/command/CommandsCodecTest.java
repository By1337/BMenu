package dev.by1337.bmenu.command;

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
        assertNotNull(Commands.DECODER.decode(yaml.get()).getOrThrow());
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
        assertNotNull(Commands.DECODER.decode(yaml.get()).getOrThrow());
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
        assertEquals(java.util.Map.of("console", "say hello"), encoded.stream().skip(1).findFirst().orElseThrow());
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

        assertNotNull(Requirement.DECODER.decode(legacy.get()).getOrThrow());
        assertNotNull(Commands.DECODER.decode(modern.get()).getOrThrow());
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
            assertNotNull(Requirement.DECODER.decode(yaml.get()).getOrThrow(), type);
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
        assertTrue(encoded.iterator().next() instanceof java.util.Map);
        assertNotNull(Commands.DECODER.decode(commands.encode()).getOrThrow());
    }
}

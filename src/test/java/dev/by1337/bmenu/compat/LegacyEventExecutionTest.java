package dev.by1337.bmenu.compat;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.event.MenuEvents;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.cmd.Command;
import dev.by1337.cmd.argument.ArgumentStrings;
import dev.by1337.edsl.context.EventContext;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LegacyEventExecutionTest {
    record Outcome(boolean result, List<String> commands) {
    }

    @Test
    void legacyChecksRunInOrderAndSelectTheOuterBranch() {
        assertCharacterized("""
                requirements:
                  - check: 'false'
                    deny_commands: '[record] first failed'
                  - check: 'true'
                    commands: '[record] second passed'
                commands: '[record] success'
                deny_commands: '[record] failure'
                """, false, "first failed", "second passed", "failure");
    }

    @Test
    void breakOnFailureStopsChecksButStillRunsOuterFailureBranch() {
        assertCharacterized("""
                requirements:
                  - check: 'false'
                    deny_commands:
                      - '[record] failed'
                      - '[break]'
                      - '[record] unreachable command'
                  - check: 'true'
                    commands: '[record] unreachable check'
                commands: '[record] success'
                deny_commands: '[record] failure'
                """, false, "failed", "failure");
    }

    @Test
    void breakOnSuccessDoesNotEraseAnEarlierFailedRequirement() {
        assertCharacterized("""
                requirements:
                  - check: 'false'
                  - check: 'true'
                    commands:
                      - '[record] passed'
                      - '[break]'
                  - check: 'true'
                    commands: '[record] unreachable'
                commands: '[record] success'
                deny_commands: '[record] failure'
                """, false, "passed", "failure");
    }

    @Test
    void breakOnSuccessSelectsOuterSuccessWhenAllEarlierChecksPassed() {
        assertCharacterized("""
                requirements:
                  - check: 'true'
                    commands: ['[record] passed', '[break]']
                  - check: 'false'
                    deny_commands: '[record] unreachable'
                commands: '[record] success'
                deny_commands: '[record] failure'
                """, true, "passed", "success");
    }

    @Test
    void typedNamedChecksAndInversionStillWork() {
        assertEvents("""
                requirements:
                  name:
                    type: string equals ignorecase
                    input: Hello World
                    output: hello world
                    commands: '[record] equal'
                  regex:
                    type: '!regex matches'
                    regex: '[0-9]+'
                    input: abc
                    commands: '[record] regex'
                commands: '[record] success'
                """, true, "equal", "regex", "success");
    }

    @Test
    void breakOnlyStopsItsOwnBranchAndNotTheNextEvent() {
        assertEvents("""
                - if: 'true'
                  do: ['[record] branch', '[break]', '[record] unreachable']
                - '[record] after'
                """, true, "branch", "after");
    }

    @Test
    void rootBreakStopsTheCurrentListAndDoesNotLeakToAnotherInvocation() {
        var handler = MenuEvents.DECODER.decode(YamlValue.wrap(List.of("[record] before", "[break]", "[record] after"))).getOrThrow();
        var output = new ArrayList<String>();
        EventContext ctx = context(output);
        assertTrue(handler.handle(ctx));
        assertTrue(MenuEvents.DECODER.decode(YamlValue.wrap("[record] next")).getOrThrow().handle(ctx));
        assertEquals(List.of("before", "next"), output);
    }

    @Test
    void modernEventDslUsesTheSameCommandTreeAsLegacyEvents() {
        assertEvents("""
                - if-one: ['false', 'true']
                  do: '[record] any'
                - oneOf:
                    - if: 'false'
                      do: '[record] unreachable'
                    - do: '[record] fallback'
                """, true, "any", "fallback");
    }

    @Test
    void commandsFacadePreservesCustomMenuCommandsAndLivePlaceholders() {
        var menu = mock(Menu.class);
        var output = new ArrayList<String>();
        var command = new Command<ExecuteContext>("root")
                .sub(new Command<ExecuteContext>("[custom]").argument(new ArgumentStrings<>("value"))
                        .executor((ctx, args) -> output.add(ctx.placeholders().setPlaceholders((String) args.get("value")))));
        when(menu.getCommands()).thenReturn(command);
        var handler = Commands.DECODER.decode(YamlValue.wrap("[custom] {value}")).getOrThrow();
        try (var execution = ExecuteContext.of(menu)) {
            assertTrue(handler.test(execution, value -> value.replace("{value}", "first")));
            assertTrue(handler.test(execution, value -> value.replace("{value}", "second")));
        }
        assertEquals(List.of("first", "second"), output);
    }

    @Test
    void emptyLegacyAnyGroupRetainsBMenuBehavior() {
        assertEvents("if-one: []\ndo: '[record] empty'", true, "empty");
    }

    @Test
    void switchingMenuUsesTheNewMenusCommandTree() {
        var first = mock(Menu.class);
        var second = mock(Menu.class);
        var output = new ArrayList<String>();
        when(first.getCommands()).thenReturn(new Command<ExecuteContext>("root")
                .sub(new Command<ExecuteContext>("[switch]").executor((ctx, args) -> ctx.menu = second)));
        when(second.getCommands()).thenReturn(new Command<ExecuteContext>("root")
                .sub(new Command<ExecuteContext>("[custom]").executor((ctx, args) -> output.add("second"))));
        var commands = Commands.DECODER.decode(YamlValue.wrap(List.of("[switch]", "[custom]"))).getOrThrow();
        try (var ctx = ExecuteContext.of(first)) {
            assertTrue(commands.test(ctx, value -> value));
        }
        assertEquals(List.of("second"), output);
    }

    @Test
    void malformedTypedCheckReportsMissingField() {
        var result = MenuEvents.DECODER.decode(YamlValue.wrap(Map.of("requirements", Map.of("type", "hp"))));
        assertTrue(result.hasError());
        assertTrue(result.error().contains("permission"));
    }

    @Test
    void emptyAndBreakOnlyCommandListsKeepTheirPublicApiMeaning() {
        assertTrue(Commands.DECODER.decode(YamlValue.wrap(List.of())).getOrThrow().isEmpty());
        var commands = Commands.DECODER.decode(YamlValue.wrap(List.of("[break]"))).getOrThrow();
        assertTrue(commands.isEmpty());
        assertTrue(commands.isHasBreak());
    }

    @Test
    void unconditionalLegacyStepCanBreakRequirements() {
        assertCharacterized("""
                requirements:
                  - do: ['[record] unconditional', '[break]']
                  - check: 'false'
                    deny_commands: '[record] unreachable'
                commands: '[record] success'
                """, true, "unconditional", "success");
    }

    @Test
    void commandsBesideLegacyRequirementsAreNotDropped() {
        assertEvents("""
                requirements: ['true']
                commands: '[record] conditional'
                record: sibling
                """, true, "conditional", "sibling");
    }

    @Test
    void yamlBreakCommandHonorsCaseAndOnlyStopsItsOwnList() {
        assertEvents("""
                - record: before
                  BREAK: ''
                  message: unreachable
                - record: unreachable outside
                """, true, "before");
    }

    @Test
    void inheritedCommandsCanReenterTheSameContextWithoutRecursivePlaceholders() {
        var menu = mock(Menu.class);
        var output = new ArrayList<String>();
        when(menu.getCommands()).thenReturn(new Command<ExecuteContext>("root")
                .sub(new Command<ExecuteContext>("[record]").argument(new ArgumentStrings<>("value"))
                        .executor((ctx, args) -> output.add((String) args.get("value")))));
        var inherited = Commands.DECODER.decode(YamlValue.wrap("[record] inherited {value}")).getOrThrow();
        var child = new Commands(List.of("[record] own"));
        child.addHandler(inherited);
        try (var ctx = ExecuteContext.of(menu)) {
            var event = ctx.eventContext();
            child.test(ctx, text -> text.replace("{value}", "first"));
            child.test(ctx, text -> text.replace("{value}", "second"));
            assertSame(event, ctx.eventContext());
            assertSame(ctx, event.get(ExecuteContext.class));
        }
        assertEquals(List.of("own", "inherited first", "own", "inherited second"), output);
    }

    @Test
    void oldViewConstructorAndGetterProxyUseTheSameLiveContext() {
        var menu = mock(Menu.class);
        var legacy = dev.by1337.bmenu.requirement.Requirement.DECODER.decode(YamlValue.wrap("{value} == yes")).getOrThrow();
        var view = new dev.by1337.bmenu.slot.component.OnViewComponent(legacy);
        try (var ctx = ExecuteContext.of(menu)) {
            var event = ctx.eventContext();
            assertTrue(view.requirement().test(ctx, text -> text.replace("{value}", "yes")));
            assertFalse(view.requirement().test(ctx, text -> text.replace("{value}", "no")));
            assertSame(event, ctx.eventContext());
            assertSame(menu, ctx.placeholders());
        }
    }

    // Expected outcomes were recorded against the previous BMenu requirements implementation.
    private void assertCharacterized(String yaml, boolean result, String... expected) {
        assertEquals(new Outcome(result, List.of(expected)), run(YamlMap.loadFromString(yaml).get()));
    }

    private void assertEvents(String yaml, boolean result, String... expected) {
        var value = YamlMap.loadFromString("value:\n" + yaml.indent(2)).get("value");
        assertEquals(new Outcome(result, List.of(expected)), run(value));
    }

    private Outcome run(YamlValue yaml) {
        var output = new ArrayList<String>();
        var decoded = MenuEvents.DECODER.decode(yaml);
        assertFalse(decoded.hasError(), () -> String.valueOf(decoded.error()));
        boolean result = decoded.getOrThrow().handle(context(output));
        return new Outcome(result, output);
    }

    private EventContext context(List<String> output) {
        return new EventContext(false).setCommand(new Command<EventContext>("root")
                .sub(new Command<EventContext>("[record]").argument(new ArgumentStrings<>("value"))
                        .executor((ctx, args) -> output.add((String) args.get("value")))));
    }
}

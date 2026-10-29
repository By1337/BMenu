package dev.by1337.bmenu.compat;

import dev.by1337.bmenu.event.MenuEventContext;
import dev.by1337.bmenu.event.MenuEvents;
import dev.by1337.bmenu.handler.input.PlayerNumberInput;
import dev.by1337.bmenu.handler.input.PlayerStringInput;
import dev.by1337.edsl.EventHandler;
import dev.by1337.edsl.check.CheckHandler;
import dev.by1337.edsl.context.EventContext;
import dev.by1337.edsl.decoder.EventHandlerDecoder;
import dev.by1337.edsl.handle.ListHandler;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.decoder.DataResultBuilder;
import dev.by1337.yaml.decoder.DecodeContext;
import dev.by1337.yaml.decoder.YamlDecoder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** The only interpreter of historical BMenu event syntax. No global break state. */
public final class MenuEventDecoder extends EventHandlerDecoder {
    private static final YamlDecoder<CheckHandler> CHECKS = CheckHandler.decoder();

    public MenuEventDecoder() {
        super((ctx, yaml) -> new MenuEventDecoder().decodeRequirement(ctx, yaml));
    }

    @Override
    public DataResult<EventHandler> decode(DecodeContext ctx, YamlValue yaml) {
        if (yaml.isList()) {
            return YamlDecoder.YAML_VALUE.listOf().decode(ctx, yaml).flatMap(values -> {
                var errors = new DataResultBuilder();
                List<EventHandler> handlers = new ArrayList<>();
                boolean stop = false;
                for (int i = 0; i < values.size(); i++) {
                    try (var ignored = ctx.fromKey(String.valueOf(i))) {
                        var result = decode(ctx, values.get(i));
                        result.ifPresent(handlers::add, errors::append);
                        if (result.hasResult() && hasBreak(result.result())) {
                            stop = true;
                            break;
                        }
                    }
                }
                return errors.result(new Sequence(new ListHandler(handlers), stop));
            });
        }
        if (!yaml.isMap()) return super.decode(ctx, yaml);
        return YamlDecoder.mapOf(YamlDecoder.STRING, YamlDecoder.OBJECT).decode(ctx, yaml).flatMap(raw -> {
            Map<String, Object> map = new LinkedHashMap<>(raw);
            rename(map, "req", "requirements");
            rename(map, "commands", "do");
            rename(map, "deny_commands", "else");
            rename(map, "check", "if");
            map.keySet().removeIf(key -> key.startsWith("$"));
            YamlValue normalized = YamlValue.wrap(map);
            if (map.containsKey("requirements")) {
                return requirements(ctx, map).flatMap(requirement -> {
                    Map<String, Object> remaining = new LinkedHashMap<>(map);
                    List.of("requirements", "if", "if-all", "if-one", "oneOf", "do", "else").forEach(remaining::remove);
                    if (remaining.isEmpty()) return DataResult.success(requirement);
                    return decode(ctx, YamlValue.wrap(remaining)).map(tail -> new Sequence(new ListHandler(List.of(requirement, tail)), hasBreak(tail)));
                });
            }
            if (map.containsKey("type")) return typedConditional(ctx, map).map(v -> v);
            String breakKey = map.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase("break") && entry.getValue() instanceof String value && value.isBlank())
                    .map(Map.Entry::getKey).findFirst().orElse(null);
            if (breakKey != null) {
                Map<String, Object> before = new LinkedHashMap<>();
                for (var entry : map.entrySet()) {
                    if (entry.getKey().equals(breakKey)) break;
                    before.put(entry.getKey(), entry.getValue());
                }
                return super.decode(ctx, YamlValue.wrap(before)).map(handler -> new Sequence(handler, true));
            }
            return super.decode(ctx, normalized);
        });
    }

    @Override
    public DataResult<? extends EventHandler> decodeCommand(DecodeContext ctx, String command, YamlValue value) {
        if (command.equals("input_chat")) {
            return PlayerNumberInput.DECODER.decode(ctx, value).map(MenuEvents::input);
        }
        if (command.equals("input_chat_str")) {
            return PlayerStringInput.DECODER.decode(ctx, value).map(MenuEvents::input);
        }
        if (value.isMap() && (command.equals("open") || command.equals("back") || command.equals("back_or_open"))) {
            command += "_with_args";
        }
        return super.decodeCommand(ctx, command, value);
    }

    @Override
    public DataResult<? extends EventHandler> decodePrimitiveCommand(String command) {
        if (command.equalsIgnoreCase("[break]")) {
            return DataResult.success(new Sequence(new ListHandler(List.of()), true));
        }
        return super.decodePrimitiveCommand(command);
    }

    public DataResult<CheckHandler> decodeRequirement(DecodeContext ctx, YamlValue yaml) {
        if (yaml.isList()) {
            boolean any = "if-one".equals(ctx.getFromKey());
            return decodeChecks(ctx, yaml, any);
        }
        if (yaml.isMap()) {
            return YamlDecoder.mapOf(YamlDecoder.STRING, YamlDecoder.OBJECT).decode(ctx, yaml).flatMap(map -> {
                if (map.containsKey("checks")) {
                    return decodeChecks(ctx, YamlValue.wrap(map.get("checks")), Boolean.TRUE.equals(map.get("anyOf")));
                }
                if (map.containsKey("type")) {
                    Map<String, Object> normalized = new LinkedHashMap<>(map);
                    rename(normalized, "commands", "do");
                    rename(normalized, "deny_commands", "else");
                    return typedConditional(ctx, normalized).map(check -> check);
                }
                if (map.containsKey("check") || map.containsKey("if") || map.containsKey("if-all") || map.containsKey("if-one") || map.containsKey("do") || map.containsKey("commands")) {
                    Map<String, Object> normalized = new LinkedHashMap<>(map);
                    rename(normalized, "check", "if");
                    rename(normalized, "commands", "do");
                    rename(normalized, "deny_commands", "else");
                    String key = normalized.containsKey("if") ? "if" : normalized.containsKey("if-all") ? "if-all" : "if-one";
                    if (!normalized.containsKey(key)) return conditional(ctx, normalized, CheckHandler.nopTrue()).map(check -> check);
                    try (var ignored = ctx.fromKey(key)) {
                        return decodeRequirement(ctx, YamlValue.wrap(normalized.get(key)))
                                .flatMap(check -> conditional(ctx, normalized, check)).map(check -> check);
                    }
                }
                return decode(ctx, yaml).<CheckHandler>map(handler -> handler instanceof CheckHandler check ? check : handler::handle);
            });
        }
        return YamlDecoder.STRING.decode(ctx, yaml).flatMap(value -> {
            if (value.startsWith("nearby ") || value.startsWith("!nearby ")) return nearby(value);
            if (value.startsWith("regex ") || value.startsWith("!regex ")) {
                String[] parts = value.split(" ", 3);
                if (parts.length != 3) return DataResult.error("Expected regex <pattern> <input>");
                try {
                    Pattern pattern = Pattern.compile(parts[1]);
                    return DataResult.success(event -> value.startsWith("!") != pattern.matcher(event.setPlaceholders(parts[2])).find());
                } catch (IllegalArgumentException e) {
                    return DataResult.error(e.getMessage());
                }
            }
            return CHECKS.decode(ctx, yaml);
        });
    }

    private DataResult<CheckHandler> nearby(String source) {
        String[] parts = source.split("\\s+");
        if (parts.length != 6) return DataResult.error("Expected nearby <world> <x> <y> <z> <radius>");
        try {
            double x = Double.parseDouble(parts[2]), y = Double.parseDouble(parts[3]), z = Double.parseDouble(parts[4]);
            int radius = Integer.parseInt(parts[5]);
            return DataResult.success(event -> {
                var player = event.source();
                if (player == null) return false;
                boolean inside = false;
                if (player.getWorld().getName().equals(parts[1])) {
                    var location = player.getLocation();
                    double dx = location.getX() - x, dy = location.getY() - y, dz = location.getZ() - z;
                    inside = dx * dx + dy * dy + dz * dz < radius * radius;
                }
                return source.startsWith("!") != inside;
            });
        } catch (NumberFormatException e) {
            return DataResult.error(e.getMessage());
        }
    }

    private DataResult<CheckHandler> decodeChecks(DecodeContext ctx, YamlValue yaml, boolean any) {
        return ((YamlDecoder<CheckHandler>) this::decodeRequirement).listOf().decode(ctx, yaml).map(checks -> event -> {
            // BMenu considers an empty group satisfied, including its historical anyOf form.
            if (checks.isEmpty()) return true;
            for (CheckHandler check : checks) {
                boolean result = check.handle(event);
                if (any && result) return true;
                if (!any && !result) return false;
            }
            return !any;
        });
    }

    private DataResult<EventHandler> requirements(DecodeContext ctx, Map<String, Object> map) {
        Object raw = map.get("requirements");
        YamlValue value = YamlValue.wrap(raw);
        if (value.isMap() && raw instanceof Map<?, ?> checks && !checks.containsKey("type") && !checks.containsKey("check")
                && !checks.containsKey("if") && !checks.containsKey("if-all") && !checks.containsKey("if-one")
                && !checks.containsKey("do") && !checks.containsKey("commands")
                && !checks.containsKey("requirements") && !checks.containsKey("checks")) {
            value = YamlValue.wrap(new ArrayList<>(checks.values()));
        }
        var errors = new DataResultBuilder();
        List<CheckHandler> checks = new ArrayList<>();
        ((YamlDecoder<CheckHandler>) this::decodeRequirement).listOf().decode(ctx, value)
                .ifPresent(checks::addAll, errors::append);
        EventHandler success = branch(ctx, map, "do", errors);
        EventHandler failure = branch(ctx, map, "else", errors);
        return errors.result(event -> {
            boolean result = true;
            for (CheckHandler check : checks) {
                try {
                    boolean passed = check.handle(event);
                    if (!passed) result = false;
                    if (check instanceof LegacyConditional conditional && conditional.stops(passed)) break;
                } catch (Exception e) {
                    event.getLogger().error("Failed to check BMenu requirement", e);
                }
            }
            (result ? success : failure).handle(event);
            return result;
        });
    }

    private EventHandler branch(DecodeContext ctx, Map<String, Object> map, String key, DataResultBuilder errors) {
        if (!map.containsKey(key)) return EventHandler.nop();
        try (var ignored = ctx.fromKey(key)) {
            var result = decode(ctx, YamlValue.wrap(map.get(key)));
            result.ifPresentError(errors::append);
            return result.hasResult() ? result.result() : EventHandler.nop();
        }
    }

    private DataResult<LegacyConditional> typedConditional(DecodeContext ctx, Map<String, Object> map) {
        return typedCheck(ctx, map).flatMap(check -> conditional(ctx, map, check));
    }

    private DataResult<LegacyConditional> conditional(DecodeContext ctx, Map<String, Object> map, CheckHandler check) {
        var errors = new DataResultBuilder();
        var success = branch(ctx, map, "do", errors);
        var failure = branch(ctx, map, "else", errors);
        return errors.result(new LegacyConditional(check, success, failure));
    }

    private DataResult<CheckHandler> typedCheck(DecodeContext ctx, Map<String, Object> map) {
        return YamlDecoder.STRING.decode(ctx, YamlValue.wrap(map.get("type"))).flatMap(type -> {
            String name = type.startsWith("!") ? type.substring(1) : type;
            List<String> required = switch (name) {
                case "m", "math" -> List.of("expression");
                case "hp", "has permission" -> List.of("permission");
                case "rm", "regex matches" -> List.of("regex", "input");
                default -> List.of("input", "output");
            };
            for (String field : required) {
                if (!map.containsKey(field) || map.get(field) == null) {
                    return DataResult.error("Missing '" + field + "' in BMenu requirement '" + type + "'");
                }
            }
            String expression;
            switch (name) {
                case "m", "math" -> expression = String.valueOf(map.get("expression"));
                case "hp", "has permission" -> expression = "has " + map.get("permission");
                case "se", "string equals" -> expression = null;
                case "sei", "string equals ignorecase" -> expression = null;
                case "sc", "string contains" -> expression = null;
                case "rm", "regex matches" -> expression = null;
                default -> {
                    return DataResult.error("Unknown BMenu requirement type: " + type);
                }
            }
            DataResult<CheckHandler> result;
            if (expression != null) {
                result = CHECKS.decode(ctx, YamlValue.wrap(expression));
            } else if (name.equals("rm") || name.equals("regex matches")) {
                try {
                    Pattern pattern = Pattern.compile(String.valueOf(map.get("regex")));
                    result = DataResult.success(event -> pattern.matcher(event.setPlaceholders(String.valueOf(map.get("input")))).find());
                } catch (RuntimeException e) {
                    return DataResult.error(e.getMessage());
                }
            } else {
                var op = switch (name) {
                    case "se", "string equals" -> dev.by1337.edsl.check.impl.StringsRequirement.Operation.EQUALS;
                    case "sei", "string equals ignorecase" -> dev.by1337.edsl.check.impl.StringsRequirement.Operation.EQUALS_IGNORE_CASE;
                    default -> dev.by1337.edsl.check.impl.StringsRequirement.Operation.CONTAINS;
                };
                result = DataResult.success(new dev.by1337.edsl.check.impl.StringsRequirement(op,
                        String.valueOf(map.get("input")), String.valueOf(map.get("output"))));
            }
            // Historical BMenu's typed math path ignores the ! prefix.
            if (type.startsWith("!") && !name.equals("m") && !name.equals("math")) {
                return result.map(check -> event -> !check.handle(event));
            }
            return result;
        });
    }

    private static void rename(Map<String, Object> map, String from, String to) {
        if (map.containsKey(from)) map.put(to, map.remove(from));
    }

    public static boolean isEmpty(EventHandler handler) {
        if (handler instanceof Sequence sequence) return isEmpty(sequence.handler);
        if (handler instanceof ListHandler list) return list.list().stream().allMatch(MenuEventDecoder::isEmpty);
        return false;
    }

    public static boolean hasBreak(EventHandler handler) {
        return handler instanceof Sequence sequence && sequence.hasBreak;
    }

    private record Sequence(EventHandler handler, boolean hasBreak) implements EventHandler {
        @Override
        public boolean handle(EventContext ctx) {
            return handler.handle(ctx);
        }
    }

    private record LegacyConditional(CheckHandler check, EventHandler success, EventHandler failure) implements CheckHandler {
        @Override
        public boolean handle(EventContext ctx) {
            boolean result = check.handle(ctx);
            (result ? success : failure).handle(ctx);
            return result;
        }

        boolean stops(boolean passed) {
            return hasBreak(passed ? success : failure);
        }
    }
}

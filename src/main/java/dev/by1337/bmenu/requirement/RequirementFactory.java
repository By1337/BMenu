package dev.by1337.bmenu.requirement;

import dev.by1337.yaml.YamlValue;

public class RequirementFactory {

    public static Requirement fromString(String input) {
        var result = fromString0(input);
        var c = result.compile();
        return c == null ? result : c;
    }

    private static Requirement fromString0(String input) {
        if (input.equalsIgnoreCase("true")) {
            return Requirement.TRUE;
        } else if (input.equalsIgnoreCase("false")) {
            return Requirement.FALSE;
        }

        if (input.startsWith("has") || input.startsWith("!has")) {
            PermissionRequirement.Operation op;
            if (input.startsWith("!")) op = PermissionRequirement.Operation.NO;
            else op = PermissionRequirement.Operation.YES;
            return new PermissionRequirement(op, input.split(" ")[1]);
        } else if (input.startsWith("nearby") || input.startsWith("!nearby")) {
            return NearbyRequirement.CODEC.decode(YamlValue.wrap(input)).getOrThrow();
        }

        String[] args = input.split(" ");
        if (args.length == 3) {
            var op = StringsRequirement.Operation.byOperator(args[1]);
            if (op == null) return new MathRequirement(input);
            return new StringsRequirement(op, args[0], args[2]);
        } else {
            return new MathRequirement(input);
        }
    }
}

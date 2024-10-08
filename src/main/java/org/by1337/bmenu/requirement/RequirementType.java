package org.by1337.bmenu.requirement;

import org.by1337.blib.configuration.YamlContext;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public enum RequirementType {
    MATH("math", MathRequirement::new, List.of("m")),
    STRING_EQUALS("string equals", StringEqualsRequirement::new, List.of("se")),
    STRING_EQUALS_IGNORE_CASE("string equals ignorecase", StringEqualsIgnoreCaseRequirement::new, List.of("sei")),
    STRING_CONTAINS("string contains", StringContainsRequirement::new, List.of("sc")),
    REGEX_MATCHES_REQUIREMENT("regex matches", RegexMatchesRequirement::new, List.of("rm")),
    HAS_PERMISSION("has permission", HasPermisionRequirement::new, List.of("hp")),
    ;
    public final String id;
    public final Function<YamlContext, Requirement> fromYaml;
    public final List<String> aliases;

    RequirementType(String id, Function<YamlContext, Requirement> fromYaml) {
        this.id = id;
        this.fromYaml = fromYaml;
        aliases = Collections.emptyList();
    }

    RequirementType(String id, Function<YamlContext, Requirement> fromYaml, List<String> aliases) {
        this.id = id;
        this.fromYaml = fromYaml;
        this.aliases = Collections.unmodifiableList(aliases);
    }

    @Nullable
    public static RequirementType byName(String name) {
        String id;
        if (name.charAt(0) == '!') {
            id = name.substring(1);
        } else {
            id = name;
        }
        for (RequirementType type : values()) {
            if (type.id.equals(id) || type.aliases.contains(id)) {
                return type;
            }
        }
        return null;
    }
}

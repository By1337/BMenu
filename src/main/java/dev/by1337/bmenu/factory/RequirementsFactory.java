package dev.by1337.bmenu.factory;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.requirement.RequirementType;
import dev.by1337.bmenu.requirement.Requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequirementsFactory {
    private static final YamlCodec<Map<String, YamlMap>> STRING_TO_YAML_MAP_CODEC = YamlCodec.mapOf(YamlCodec.STRING, YamlCodec.YAML_MAP);

    @Deprecated
    public static Requirements readLegacy(YamlValue data) {
        List<Requirement> requirements = new ArrayList<>();
        Map<String, YamlMap> map = data.decode(STRING_TO_YAML_MAP_CODEC).getOrThrow();
        for (YamlMap value : map.values()) {
            String type = value.get("type").decode(YamlCodec.STRING).getOrThrow();
            RequirementType requirementType = RequirementType.byName(type);
            if (requirementType == null) {
                throw new IllegalArgumentException("unknown requirement type: " + type);
            }
            requirements.add(requirementType.fromYaml.createRequirement(value));
        }
        return new Requirements(requirements);
    }
}

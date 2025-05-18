package org.by1337.bmenu.factory;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.bmenu.requirement.Requirement;
import org.by1337.bmenu.requirement.RequirementType;
import org.by1337.bmenu.requirement.Requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequirementsFactory {

    @Deprecated
    public static Requirements readLegacy(YamlValue data) {
        List<Requirement> requirements = new ArrayList<>();
        Map<String, YamlMap> map = data.decode(YamlCodec.STRING_TO_YAML_MAP);
        for (YamlMap value : map.values()) {
            String type = value.get("type").getAsString();
            RequirementType requirementType = RequirementType.byName(type);
            if (requirementType == null) {
                throw new IllegalArgumentException("unknown requirement type: " + type);
            }
            requirements.add(requirementType.fromYaml.createRequirement(value));
        }
        return new Requirements(requirements);
    }

    @Deprecated(forRemoval = true)
    public static Requirements read(org.by1337.blib.configuration.YamlValue ctx) {
        return Requirements.CODEC.decode(MenuFilePostprocessor.fromBLib(ctx));
    }
}

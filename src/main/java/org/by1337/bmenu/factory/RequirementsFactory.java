package org.by1337.bmenu.factory;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.by1337.bmenu.requirement.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequirementsFactory {

    @Deprecated
    public static Requirements readLegacy(YamlValue data) {
        List<Requirement> requirements = new ArrayList<>();
        Map<String, YamlMap> map = data.decode(YamlCodec.STRING_TO_YAML_MAP_MAP);
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
        throw new UnsupportedOperationException();//todo
    }
}

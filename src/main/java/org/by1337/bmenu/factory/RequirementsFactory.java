package org.by1337.bmenu.factory;

import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.requirement.Requirement;
import org.by1337.bmenu.requirement.RequirementType;
import org.by1337.bmenu.requirement.Requirements;

import java.util.ArrayList;
import java.util.List;

public class RequirementsFactory {
    public static Requirement read(YamlContext ctx, MenuLoader loader, Placeholder argsReplacer) {
        List<Requirement> requirements = new ArrayList<>();
        for (YamlContext context : ctx.get().getAsMap(YamlValue::getAsYamlContext).values()) {
            String type = context.getAsString("type");
            RequirementType requirementType = RequirementType.byName(type);
            if (requirementType == null) {
                throw new IllegalArgumentException("unknown requirement type: " + type);
            }
            requirements.add(requirementType.fromYaml.createRequirement(context, argsReplacer));
        }
        return new Requirements(requirements);
    }
}

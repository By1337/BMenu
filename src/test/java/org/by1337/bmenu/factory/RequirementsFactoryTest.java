package org.by1337.bmenu.factory;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bmenu.requirement.*;
import org.by1337.bmenu.requirement.Requirement;
import org.by1337.bmenu.requirement.Requirements;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class RequirementsFactoryTest {
    private static final String YAML = """
            requirements:
              - check: '100 + 100 * 100 == 15'
                commands: [ ]
                deny_commands: [ ]
              - check: '100.5 + 100.5 == 201'
              - check: 'has super.permission'
              - check: '!has super.permission'
              - check: 'string has str'
              - check: 'string !has str'
              - check: 'string HAS str'
              - check: 'string !HAS str'
              - check: 'string == string'
              - check: 'string != string'
            """;

    @Test
    public void run() throws InvalidConfigurationException {
        YamlContext ctx = load(YAML);
        Requirements req = RequirementsFactory.read(ctx.get("requirements"), null);
        Assertions.assertEquals(10, req.getRequirements().size());
        Assertions.assertTrue(req.getRequirements().get(0) instanceof MathRequirement);
        Assertions.assertTrue(req.getRequirements().get(1) instanceof MathRequirement);
        Assertions.assertTrue(req.getRequirements().get(2) instanceof HasPermisionRequirement);
        Assertions.assertTrue(req.getRequirements().get(3) instanceof HasPermisionRequirement);
        Assertions.assertTrue(req.getRequirements().get(4) instanceof StringContainsRequirement);
        Assertions.assertTrue(req.getRequirements().get(5) instanceof StringContainsRequirement);
        Assertions.assertTrue(req.getRequirements().get(6) instanceof StringEqualsIgnoreCaseRequirement);
        Assertions.assertTrue(req.getRequirements().get(7) instanceof StringEqualsIgnoreCaseRequirement);
        Assertions.assertTrue(req.getRequirements().get(8) instanceof StringEqualsRequirement);
        Assertions.assertTrue(req.getRequirements().get(9) instanceof StringEqualsRequirement);

    }

    private YamlContext load(String s) throws InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.loadFromString(s);
        return new YamlContext(configuration);
    }

}
package dev.by1337.bmenu.factory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.bmenu.slot.component.ClickMapComponent;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.bmenu.factory.YamlReaderImpl;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class RequirementsFactoryTest {

    private static String YAML = """
      on_left_click:
        requirements:
          check:
            type: math
            expression: '%vault_eco_balance% >= ${PRICE}'
        deny_commands:
          - '[MESSAGE] &cУ Вас не достаточно баланса!'
        commands:
          - '[CONSOLE] eco take %player_name% ${PRICE}'
          - '[CONSOLE] give %player_name% ${MATERIAL} ${AMOUNT}'
          - '[MESSAGE] &aВы успешно купили ${NAME}&a в количестве ${AMOUNT}'""";
     @Test
    public void run(){
      //  YamlMap.setYamlReader(new YamlReaderImpl());
        YamlMap map = YamlMap.load(new StringReader(YAML));
        // var codec = YamlCodec.mapOf(YamlCodec.STRING, MenuEventHandler.CODEC);
        var handlers = map.get().decode(ClickMapComponent.CODEC).getOrThrow();

        System.out.println(handlers);

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        System.out.println( gson.toJson(handlers));

        System.out.println(ClickMapComponent.CODEC.encode(handlers).asYamlMap().getOrThrow().saveToString());

    }
   /* private static final String YAML = """
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
        Requirements req = RequirementsFactory.read(ctx.get("requirements"));
        Assertions.assertEquals(10, req.getRequirements().size());
        Assertions.assertTrue(req.getRequirements().get(0) instanceof MathRequirement);
        Assertions.assertTrue(req.getRequirements().get(1) instanceof MathRequirement);
        Assertions.assertTrue(req.getRequirements().get(2) instanceof HasPermissionRequirement);
        Assertions.assertTrue(req.getRequirements().get(3) instanceof HasPermissionRequirement);
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
    }*/

}
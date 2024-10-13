package org.by1337.bmenu.factory;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.yaml.RawYamlContext;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MenuFilePostprocessorTest {

/*    @Test
    void applyTest() throws InvalidConfigurationException, InvalidMenuConfigException {
        YamlContext ctx = load("""
                title: '&7Премиальный Скупщик' &123
                title-2: *123""");


        RawYamlContext raw = MenuFilePostprocessor.apply(ctx, LoggerFactory.getLogger("test"));

        System.out.println(saveToString(raw.getRaw()));

//        assertEquals(raw.get("map.test-soft.args.test").getAsString(), "test3");
//        assertEquals(raw.get("map.test-soft.args.test2").getAsString(), "test2");
    }*/

    private YamlContext load(String s) throws InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.loadFromString(s);
        return new YamlContext(configuration);
    }

    public String saveToString(Map<String, Object> raw) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        StringWriter writer = new StringWriter();
        yaml.dump(raw, writer);
        return writer.toString();
    }
}
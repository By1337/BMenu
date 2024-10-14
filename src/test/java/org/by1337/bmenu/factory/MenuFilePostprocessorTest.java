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

    @Test
    void applyTest() throws InvalidConfigurationException, InvalidMenuConfigException {
        YamlContext ctx = load("""
                ok:
                  item-money: &seller-money
                    args:
                      MONEY: '&#CC0000Цена: ${PRICE}'
                      SUPER_LORE:
                        - '&7line 1'
                        - '&7line 2'
                        - '&7line 3'
                  item-totem: &seller-item-totem
                    args:
                      MATERIAL: totem_of_undying
                      PRICE: 10000
                      PRICE_FORMAT: '10,000'
                      LORE: '&6Тотем'
                      AMOUNT: 1
                      NAME: '&6Тотем'
                    display_name: ${NAME}
                    material: totem_of_undying
                
                
                  item-elytra: &seller-elytra
                    args:
                      MATERIAL: elytra
                      PRICE_FORMAT: '20,000'
                      PRICE: 20000
                      LORE: '&6Элитры'
                      AMOUNT: 1
                      NAME: '&6Элитры'
                    display_name: ${NAME}
                    material: elytra
                   \s
                items:
                  totem:
                    <<+:
                      - *seller-money
                      - *seller-item-totem
                    lore:
                      - '${SUPER_LORE}'
                      - '${MONEY}'
                    on_click:
                      requirements:
                        check:
                          type: math
                          expression: '%vault_eco_balance% >= ${PRICE}'
                      deny_commands:
                        - '[MESSAGE] &cУ Вас не достаточно баланса!'
                      commands:
                        - '[OPEN] example:prem-seller-confirm ["[SET_PARAM] SELECTED_ITEM totem", "[IMPORT_PARAMS] totem"]'
                
                  elytra:
                    <<+:
                      - *seller-money
                      - *seller-elytra
                    lore:
                      - '${SUPER_LORE}'
                      - '${MONEY}'
                    on_click:
                      requirements:
                        check:
                          type: math
                          expression: '%vault_eco_balance% >= ${PRICE}'
                      deny_commands:
                        - '[MESSAGE] &cУ Вас не достаточно баланса!'
                      commands:
                        - '[OPEN] example:prem-seller-confirm ["[SET_PARAM] SELECTED_ITEM elytra", "[IMPORT_PARAMS] elytra"]'""");


        RawYamlContext raw = MenuFilePostprocessor.apply(ctx, LoggerFactory.getLogger("test"));

     //   System.out.println(saveToString(raw.getRaw()));

//        assertEquals(raw.get("map.test-soft.args.test").getAsString(), "test3");
//        assertEquals(raw.get("map.test-soft.args.test2").getAsString(), "test2");
    }

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
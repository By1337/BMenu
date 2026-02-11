package org.by1337.bmenu.factory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.slot.component.OnViewComponent;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class RequirementsFactoryTest {

    private static String YAML = """
            legacy_test:
              on_left_click:
                requirements:
                  - check: '{playing} == true'
                    commands: 'do {playing}'
                    deny_commands: 'else {playing}'
                commands: 'do on_left_click'
                deny_commands: 'else on_left_click'
              on_reght_click:
                requirements:
                  check:
                    type: math
                    expression: '100 + 100 == 10 * (5 * 4)'
                    deny_commands: [ ]
                    commands: [ ]
                  check-1:
                    type: string equals
                    input: 'str'
                    output: 'str'
                    commands:
                      - '[MESSAGE] str == str'
                      - '[BREAK]'
                deny_commands: [ ]
              on_reght_click2:
                requirements: # алиас req
                  check: # если условие окажется ложным, то проверка остальных условий прервётся
                    type: math
                    expression: '100 + 100 == 10 * (5 * 4)'
                    deny_commands: [ ] # во всех проверках тоже можно указывать команды
                    commands: [ ]
                  check-1:
                    type: string equals
                    input: 'str'
                    output: 'str'
                    commands:
                      - '[MESSAGE] str == str'
                      - '[BREAK]' # Эта команда доступна только в условиях, она прерывает проверку следующих условий и выполнение следующих команд
                      - '[CLOSE]' # Так как выше команда [BREAK] команда [CLOSE] не будет выполнена
                  check-2:
                    type: string equals ignorecase
                    input: 'str'
                    output: 'STR'
                  check-3:
                    type: string contains
                    input: 'str_str_str'
                    output: 'str'
                  check-4:
                    type: regex matches
                    regex: '^(str)\\d+'
                    input: 'str88ing'
                  check-5:
                    type: has permission
                    permission: 'admin.use'
                  # Здесь можно выполнить команды в зависимости от результатов условий
                deny_commands: [ ] # если хотя бы одно из условий окажется ложным
              on_click:
                commands:
                  - '[console] kick ${PLAYER} 1.1'
                  - '[message] &aИгрок ${PLAYER} был кикнут по причине 1.1'
                  - '[close]'
              on_view: '100 == 100'
              on_view2:
                if: '100 == 99'
                else: 'else'
                do: 'do'
              on_view3:
                - if: '123 == 123'
                - if: '321 == 321'
                - '999 == 999'
            """;
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    @Test
    public void run() {
        //legacy_test.on_left_click
        //legacy_test.on_reght_click
        YamlMap map = YamlMap.load(new StringReader(YAML));

      //  decodeTest(map.get("legacy_test.on_left_click"), Commands.CODEC);
      //  decodeTest(map.get("legacy_test.on_reght_click"), Commands.CODEC);
      //  decodeTest(map.get("legacy_test.on_reght_click2"), Commands.CODEC);
        decodeTest(map.get("legacy_test.on_click"), Commands.CODEC);
        decodeTest(map.get("legacy_test.on_view"), OnViewComponent.CODEC);
        decodeTest(map.get("legacy_test.on_view2"), OnViewComponent.CODEC);
        decodeTest(map.get("legacy_test.on_view3"), OnViewComponent.CODEC);
    }

    private static <T> void decodeTest(YamlValue v, YamlCodec<T> c) {
        DataResult<T> res = c.decode(v);
        if (res.hasError()) {
            System.out.println(res.error());
        }
        T t = res.getOrThrow();
       // System.out.println(gson.toJson(t));
        var encoded = c.encode(t);
        YamlMap map = new YamlMap();
        map.set("encoded", encoded);
        System.out.println(map.saveToString());
    }

}
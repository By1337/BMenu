package org.by1337.bmenu.factory;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.util.YamlReader;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.Map;

@ApiStatus.Internal
public class YamlReaderImpl implements YamlReader {
    @Override
    public YamlMap read(String data) {
        try {
            LoaderOptions options = new LoaderOptions();
            Yaml yaml = new Yaml(options);
            return new  YamlMap(yaml.load(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String saveToString(Map<String, Object> map) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        StringWriter writer = new StringWriter();
        yaml.dump(map, writer);
        return writer.toString();
    }
}

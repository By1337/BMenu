package dev.by1337.bmenu.compat;

import dev.by1337.bmenu.factory.MenuFilePostprocessor;
import dev.by1337.cmd.CommandReader;
import dev.by1337.yaml.YamlMap;

public final class LegacySyntax {
    private LegacySyntax() {
    }

    public static void normalize(YamlMap yamlMap) {
        //{rand_10} -> {rand:10}
        //{rand_100} -> {rand:100}
        //{rand_1000} -> {rand:1000}
        //{rand_10000} -> {rand:10000}
        //{rand_100000} -> {rand:100000}
        //math[(.*?)] -> {math:(.*?)}
        //${(.*?)} -> {(.*?)}
        yamlMap.deepReplace(s -> fixCommands(s
                .replace("{rand_10}", "{rand:10}")
                .replace("{rand_100}", "{rand:100}")
                .replace("{rand_1000}", "{rand:1000}")
                .replace("{rand_10000}", "{rand:10000}")
                .replace("{rand_100000}", "{rand:100000}")
                .replaceAll("math\\[(.*?)]", "{math:$1}")
                .replaceAll("\\$\\{(.*?)}", "{$1}")));
    }

    //[set_item_to_layer] <slots> <layer> <item> -> [layer] <layer+2> [set] <item> <slots>
    //[clear_layer] <layer> -> [layer] <layer+2> [clear]
    private static String fixCommands(String src) {
        if (src.startsWith("[set_item_to_layer] ") || src.startsWith("[SET_ITEM_TO_LAYER] ")) {
            String args = src.substring("[set_item_to_layer] ".length());
            //<slots> <layer> <item>
            CommandReader reader = new CommandReader(args);
            String slots = reader.readString();
            reader.next();
            String layer = reader.readString();
            reader.next();
            String item = reader.readString();
            try {
                int x = Integer.parseInt(layer);
                return "[layer] " + (x + 2) + " set " + MenuFilePostprocessor.quoteAndEscape(item, false) + " " + MenuFilePostprocessor.quoteAndEscape(slots, false);
            } catch (NumberFormatException e) {
                return src;
            }
        } else if (src.startsWith("[clear_layer] ") || src.startsWith("[CLEAR_LAYER] ")) {
            String layer = src.substring("[clear_layer] ".length());
            try {
                int x = Integer.parseInt(layer);
                return "[layer] " + (x + 2) + " clear";
            } catch (NumberFormatException e) {
                return src;
            }

        }
        return src;
    }


}

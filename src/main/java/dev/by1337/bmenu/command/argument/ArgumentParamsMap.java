package dev.by1337.bmenu.command.argument;

import dev.by1337.cmd.*;

import java.util.HashMap;
import java.util.Map;

public class ArgumentParamsMap<C> extends Argument<C, Map<String, String>> {

    public ArgumentParamsMap(String name) {
        super(name);
    }

    @Override
    public void parse(C ctx, CommandReader reader, ArgumentMap out) throws CommandMsgError {
        String src = reader.src();
        int idx = reader.ridx();
        if (idx < src.length()) {
            out.put(this.name, parse(src.substring(idx)));
            reader.ridx(src.length());
        }
    }

    @Override
    public void suggest(C ctx, CommandReader reader, SuggestionsList suggestions, ArgumentMap args) throws CommandMsgError {
        reader.ridx(reader.length());
    }

    @Override
    public boolean allowAsync() {
        return true;
    }

    @Override
    public boolean compilable() {
        return true;
    }

    public static Map<String, String> parse(String input) {
        Map<String, String> map = new HashMap<>();

        int i = 0;
        int len = input.length();
        StringBuilder buffer = new StringBuilder();

        while (i < len) {

            i = readString(input, i, buffer);
            String key = buffer.toString();
            buffer.setLength(0);

            if (i >= len || input.charAt(i) != '=')
                throw new IllegalArgumentException("Expected '=' at " + i);

            i++;

            i = readString(input, i, buffer);
            String value = buffer.toString();
            buffer.setLength(0);

            map.put(key, value);

            if (i < len) {
                if (input.charAt(i) != ',')
                    throw new IllegalArgumentException("Expected ',' at " + i);
                i++;
            }
        }

        return map;
    }

    private static int readString(String s, int start, StringBuilder out) {
        boolean quoted = s.charAt(start) == '"';

        if (quoted) start++;

        for (int i = start; i < s.length(); i++) {

            char c = s.charAt(i);

            if (quoted) {
                if (c == '\\') {
                    if (++i >= s.length())
                        throw new IllegalArgumentException("Invalid escape");
                    c = s.charAt(i);
                    switch (c) {
                        case 'n' -> out.append('\n');
                        case 'r' -> out.append('\r');
                        case '\\' -> out.append('\\');
                        case 't' -> out.append('\t');
                        default -> out.append(c);
                    }
                    continue;
                }

                if (c == '"') {
                    return i + 1;
                }

            } else {
                if (c == '=' || c == ',' || c == ' ') {
                    return i;
                }
            }

            out.append(c);
        }

        if (quoted)
            throw new IllegalArgumentException("Unclosed string");

        return s.length();
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}

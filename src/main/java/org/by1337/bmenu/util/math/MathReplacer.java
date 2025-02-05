package org.by1337.bmenu.util.math;

import org.by1337.blib.math.MathParser;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathReplacer {

    public static String replace(String input) throws ParseException {
        MathParserType type = input.contains(".") ? MathParserType.DOUBLE : MathParserType.DEFAULT;

        String pattern = "math\\[(.*?)]";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(input);

        while (matcher.find()) {
            String s = MathParser.replaceStrings(matcher.group(1));
            s = s.replace(" ", "");

            input = input.replace(matcher.group(0), type.processor.apply(s));
        }
        return input.replace(".0", "");
    }
}

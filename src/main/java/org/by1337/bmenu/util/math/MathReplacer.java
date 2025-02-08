package org.by1337.bmenu.util.math;

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
            String s = replaceStrings(matcher.group(1));
            s = s.replace(" ", "");

            input = input.replace(matcher.group(0), type.processor.apply(s));
        }
        return input.replace(".0", "");
    }

    public static String replaceStrings(String s) {
        String step1 = s.replaceAll("\\btrue\\b", "1");
        String step2 = step1.replaceAll("\\bfalse\\b", "0");
        Pattern pattern = Pattern.compile("([^\\s=><!^%()&|+\\-*/\\d.]+)");
        Matcher matcher = pattern.matcher(step2);
        StringBuilder resultBuffer = new StringBuilder();
        while (matcher.find()) {
            String replacement = String.valueOf(matcher.group().hashCode());
            matcher.appendReplacement(resultBuffer, replacement);
        }
        matcher.appendTail(resultBuffer);
        return resultBuffer.toString();
    }
}

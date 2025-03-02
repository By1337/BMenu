package org.by1337.bmenu.util.math;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathReplacer {
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static String replace(String input) throws ParseException {
        MathParserType type = input.contains(".") ? MathParserType.DOUBLE : MathParserType.DEFAULT;

        String pattern = "math\\[(.*?)]";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(input);

        while (matcher.find()) {
            String s = replaceStrings(matcher.group(1));
            s = s.replace(" ", "");

            String result;
            if (type == MathParserType.DOUBLE) {
                result = df.format(Double.parseDouble(type.processor.apply(s)));
            } else {
                result = type.processor.apply(s);
            }
            input = input.replace(matcher.group(0), result);
        }
        return input;
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

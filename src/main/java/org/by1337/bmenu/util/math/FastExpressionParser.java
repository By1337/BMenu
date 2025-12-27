package org.by1337.bmenu.util.math;

import dev.by1337.plc.PlaceholderFormat;
import dev.by1337.plc.PlaceholderResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class FastExpressionParser {
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    public static final PlaceholderResolver<Void> PLACEHOLDER = new PlaceholderResolver<>() {
        @Override
        public boolean has(String key, PlaceholderFormat format) {
            return key.equals("math");
        }

        @Override
        public @Nullable String replace(String key, String params, @Nullable Void ctx, PlaceholderFormat format) {
            try {
                return String.valueOf(FastExpressionParser.parse(params));
            } catch (FastExpressionParser.MathFormatException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    };
    private static final double TRUE = 1.0;
    private static final double FALSE = 0.0;

    public static String replacePlaceholders(String input)  {
        return PLACEHOLDER.replace(input, null);
    }
    public static double parse(String input) throws MathFormatException {
        ExpReader reader = new ExpReader(input);
        double d = logical(reader);
        if (reader.next() != '\0') {
            throw reader.expected("EOF");
        }
        return d;
    }

    private static double logical(ExpReader reader) {
        double value = plusMinus(reader);
        while (true) {
            switch (reader.next()) {
                case ' ' -> {
                    //nop
                }
                case '=' -> {
                    if (reader.next() == '=') {
                        value = value == plusMinus(reader) ? TRUE : FALSE;
                    } else {
                        throw reader.expected('=');
                    }
                }
                case '&' -> {
                    if (reader.next() == '&') {
                        double next = logical(reader);
                        value = next == TRUE && value == TRUE ? TRUE : FALSE;
                    } else {
                        throw reader.expected('&');
                    }
                }
                case '|' -> {
                    if (reader.next() == '|') {
                        double next = plusMinus(reader);
                        value = (next == TRUE || value == TRUE) ? TRUE : FALSE;
                    } else {
                        throw reader.expected('|');
                    }
                }
                case '!' -> {
                    if (reader.next() == '=') {
                        value = value != plusMinus(reader) ? TRUE : FALSE;
                    } else {
                        throw reader.expected('=');
                    }
                }
                case '<' -> {
                    if (reader.next() == '=') {
                        value = value <= plusMinus(reader) ? TRUE : FALSE;
                        break;
                    }
                    reader.back();
                    value = value < plusMinus(reader) ? TRUE : FALSE;
                }
                case '>' -> {
                    if (reader.next() == '=') {
                        value = value >= plusMinus(reader) ? TRUE : FALSE;
                        break;
                    }
                    reader.back();
                    value = value > plusMinus(reader) ? TRUE : FALSE;
                }
                case '\0' -> {
                    return value;
                }
                default -> {
                    reader.back();
                    return value;
                }
            }

        }
    }

    private static double plusMinus(ExpReader reader) {
        double value = multdiv(reader);
        while (true) {
            switch (reader.next()) {
                case ' ' -> {
                    //nop
                }
                case '+' -> value = value + multdiv(reader);
                case '-' -> value = value - multdiv(reader);
                case '\0' -> {
                    return value;
                }
                default -> {
                    reader.back();
                    return value;
                }
            }

        }
    }

    private static double multdiv(ExpReader reader) {
        double value = factor(reader);
        while (true) {
            switch (reader.next()) {
                case ' ' -> {
                    //nop
                }
                case '*' -> value = value * factor(reader);
                case '/' -> value = value / factor(reader);
                case '%' -> value = value % factor(reader);
                case '\0' -> {
                    return value;
                }
                default -> {
                    reader.back();
                    return value;
                }
            }
        }
    }

    private static double factor(ExpReader reader) {
        char c = reader.next();
        while (c == ' ') {
            c = reader.next();
        }
        if (c == '\0') {
            throw reader.expected("digit");
        }
        if (c == '-') {
            return -factor(reader);
        }
        if (c == '(') {
            double value = logical(reader);
            if (reader.next() != ')') {
                throw reader.expected(')');
            }
            return value;
        }
        if (c == '!') {
            double v = factor(reader);
            return v == TRUE ? FALSE : TRUE;
        }
        reader.back();
        if (c >= '0' && c <= '9') {
            return readNumber(reader);
        }
        return readString(reader);
    }

    private static final double[] POW10 = {
            1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9,
            1e10, 1e11, 1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18
    };

    private static double readNumber(ExpReader reader) throws MathFormatException {
        boolean negative = false;
        boolean expNegative = false;

        long intPart = 0;
        double fracPart = 0.0;
        int expValue = 0;
        boolean hasDot = false;
        boolean hasExp = false;

        char c = reader.next();

        if (c == '-' || c == '+') {
            negative = (c == '-');
            if (!reader.hasNext()) throw reader.expected("digit");
            c = reader.next();
        }

        double fracMultiplier = 0.1;

        outer:
        while (true) {
            if (c >= '0' && c <= '9') {
                if (!hasDot) {
                    intPart = intPart * 10 + (c - '0');
                } else {
                    fracPart += (c - '0') * fracMultiplier;
                    fracMultiplier *= 0.1;
                }
            } else if ((c == '.' /*|| c == ','*/) && !hasDot) {
                hasDot = true;
            } else if (c == 'E' || c == 'e') {
                hasExp = true;
                c = reader.next();
                if (c == '-' || c == '+') {
                    expNegative = (c == '-');
                    if (!reader.hasNext()) throw reader.expected("digit");
                    c = reader.next();
                }
                while (c >= '0' && c <= '9') {
                    expValue = expValue * 10 + (c - '0');
                    if (!reader.hasNext()) break outer;
                    c = reader.next();
                }
                break;
            } else {
                reader.back();
                break;
            }

            if (!reader.hasNext()) break;
            c = reader.next();
        }

        double result = intPart + fracPart;

        if (hasExp && expValue != 0) {
            int exp = expNegative ? -expValue : expValue;
            if (exp >= -18 && exp <= 18) {
                result *= (exp >= 0) ? POW10[exp] : 1.0 / POW10[-exp];
            } else {
                result *= Math.pow(10, exp);
            }
        }

        while (reader.hasNext()) {
            c = reader.next();
            if (c == 'k' || c == 'K') {
                result *= 1000;
            } else if (c == 'm' || c == 'M') {
                result *= 1_000_000;
            } else {
                reader.back();
                break;
            }
        }

        return negative ? -result : result;
    }


    private static final Function RND = new Function() {
        private final Random random = new Random();

        @Override
        public double run(ExpReader reader) {
            return random.nextDouble();
        }

        @Override
        public double run(double bound, ExpReader reader) {
            double r = random.nextDouble();
            r = r * bound;
            if (r >= bound)
                r = Math.nextDown(bound);
            return r;
        }

        @Override
        public double run(double d, double d1, ExpReader reader) {
            return run(Math.abs(d1 - d), reader) + d;
        }
    };
    private static final Function IRND = new Function() {
        private final Random random = new Random();

        @Override
        public double run(ExpReader reader) {
            return random.nextInt();
        }

        @Override
        public double run(double d, ExpReader reader) {
            return random.nextInt((int) d);
        }

        @Override
        public double run(double d, double d1, ExpReader reader) {
            return random.nextInt((int) Math.abs(d1 - d)) + (int) d;
        }
    };
    private static final Function CEIL = new Function() {
        @Override
        public double run(double d, ExpReader reader) {
            return Math.ceil(d);
        }
    };
    private static final Function ABS = new Function() {
        @Override
        public double run(double d, ExpReader reader) {
            return Math.abs(d);
        }
    };
    private static final Function MAX = new Function() {
        @Override
        public double run(double d, double d1, ExpReader reader) {
            return Math.max(d1, d);
        }
    };
    private static final Function MIN = new Function() {
        @Override
        public double run(double d, double d1, ExpReader reader) {
            return Math.min(d1, d);
        }
    };
    private static final Function COS = new Function() {
        @Override
        public double run(double d, ExpReader reader) {
            return Math.cos(d);
        }
    };
    private static final Function SIN = new Function() {
        @Override
        public double run(double d, ExpReader reader) {
            return Math.sin(d);
        }
    };

    private static int hash(String s) {
        int hash = 0;
        for (char c : s.toCharArray()) {
            hash = (hash << 5) - hash + c;
        }
        return hash;
    }

    private static final int RND_HASH = 113064;
    private static final int IRND_HASH = 3241119;
    private static final int CEIL_HASH = 3049733;
    private static final int ABS_HASH = 96370;
    private static final int MAX_HASH = 107876;
    private static final int MIN_HASH = 108114;
    private static final int COS_HASH = 98695;
    private static final int SIN_HASH = 113880;

    //=&|!<>\0+-*/%()0-9
    private static double readString(ExpReader reader) throws MathFormatException {
        int hash = 0;

        loop:
        while (reader.hasNext()) {
            char c = reader.next();
            switch (c) {
                case ' ':
                case ')':
                case '(':
                case '%':
                case '*':
                case '/':
                case '-':
                case '+':
                case '\0':
                case '>':
                case '<':
                case '!':
                case '|':
                case '=':
                case '&': {
                    reader.back();
                    break loop;
                }
            }
            hash = (hash << 5) - hash + c;
        }

        if (hash == 0) {
            throw reader.expected("string");
        }
        //true 3569038
        //TRUE 2583950
        //yes 119527
        //false 97196323
        //FALSE 66658563
        //no 3521
        return switch (hash) {
            case 3569038, 2583950, 119527 -> TRUE;
            case 97196323, 66658563, 3521 -> FALSE;
            case RND_HASH -> RND.read(reader, hash);
            case IRND_HASH -> IRND.read(reader, hash);
            case CEIL_HASH -> CEIL.read(reader, hash);
            case ABS_HASH -> ABS.read(reader, hash);
            case MAX_HASH -> MAX.read(reader, hash);
            case MIN_HASH -> MIN.read(reader, hash);
            case COS_HASH -> COS.read(reader, hash);
            case SIN_HASH -> SIN.read(reader, hash);
            default -> hash;
        };
    }


    public static final class ExpReader {
        private int ridx;
        private final String exp;
        private final int size;

        public ExpReader(String exp) {
            this.exp = exp;
            size = exp.length();
        }


        public char next() {
            if (ridx < size) {
                return exp.charAt(ridx++);
            }
            ridx++;
            return '\0';
        }

        public boolean hasNext() {
            return ridx < size;
        }

        public char last() {
            if (size == 0) return '\0';
            return exp.charAt(Math.max(0, Math.min(Math.max(0, ridx), size - 1)));
        }

        public void back() {
            ridx--;
        }

        public int ridx() {
            return ridx;
        }

        public void ridx(int ridx) {
            this.ridx = ridx;
        }

        private String context() {

            return "\n" + exp + "\n" +
                    " ".repeat(Math.max(0, ridx)) +
                    "^ ridx=" + ridx + ", length=" + exp.length() + "\n";
        }

        public MathFormatException badNumber(NumberFormatException e) throws MathFormatException {
            return new MathFormatException("Bad number: " + e.getMessage() + "\n" + context());
        }

        public MathFormatException expected(String s) throws MathFormatException {
            return new MathFormatException("Expected " + s + " but got '" + last() + "' at " + ridx + "\n" + context());
        }

        public MathFormatException expected(char c) throws MathFormatException {
            return new MathFormatException("Expected '" + c + "' but got '" + last() + "' at " + ridx + "\n" + context());
        }

    }

    public static class MathFormatException extends NumberFormatException {
        public MathFormatException() {
        }

        public MathFormatException(String s) {
            super(s);
        }
    }

    public interface Function {
        default double read(ExpReader reader, int hash) {
            if (reader.next() == '(') {
                if (reader.next() == ')') {
                    return run(reader);
                } else {
                    reader.back();
                }
                double d = logical(reader);
                if (reader.next() == ',') {
                    double d1 = logical(reader);
                    if (reader.next() != ')') throw reader.expected(")");
                    return run(d, d1, reader);
                } else {
                    reader.back();
                }
                if (reader.next() != ')') throw reader.expected(")");
                return run(d, reader);
            }
            reader.back();
            return hash;
        }

        default double run(ExpReader reader) {
            throw reader.expected("Does not support 0 arguments");
        }

        default double run(double d, ExpReader reader) {
            throw reader.expected("Does not support 1 arguments");
        }

        default double run(double d, double d1, ExpReader reader) {
            throw reader.expected("Does not support 2 arguments");
        }
    }
}

package org.by1337.bmenu.util.math;

import org.by1337.blib.math.DoubleMathParser;
import org.by1337.blib.math.MathParser;

public enum MathParserType {
    DEFAULT(s -> MathParser.math("math[" + s + "]")),
    DOUBLE(s -> DoubleMathParser.math(s, true));
    public final MathFunction<String, String> processor;

    MathParserType(MathFunction<String, String> processor) {
        this.processor = processor;
    }
}
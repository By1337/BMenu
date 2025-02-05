package org.by1337.bmenu.util.math;

import java.text.ParseException;

public interface MathFunction<T, R> {
    R apply(T t) throws ParseException;
}
package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.util.LazyLoad;
import dev.by1337.plc.PlaceholderApplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public record StringsRequirement(Operation op, String s, String s1) implements Requirement {


    @Override
    public boolean test(Menu menu, PlaceholderApplier placeholders) {
        return op.test.test(placeholders.setPlaceholders(s), placeholders.setPlaceholders(s1));
    }

    @Override
    public @Nullable Requirement compile() {
        if (s.contains("{") || s.contains("%") ||
                s1.contains("{") || s1.contains("%")
        ) return null;
        return op.test.test(s, s1) ? TRUE : FALSE;
    }

    public StringsRequirement invert() {
        return new StringsRequirement(op.invert.get(), s, s1);
    }

    @Override
    public @NotNull String toString() {
        return s + " " + op.name + " " + s1;
    }

    public enum Operation {
        CONTAINS(String::contains, "has", not_contains()),
        CONTAINS_IGNORE_CASE((s, s1) -> s.toLowerCase().contains(s1.toLowerCase()), "HAS", not_contains_ignore_case()),
        NOT_CONTAINS(CONTAINS.test.negate(), "!has", contains()),
        NOT_CONTAINS_IGNORE_CASE(CONTAINS_IGNORE_CASE.test.negate(), "!HAS", contains_ignore_case()),
        EQUALS(String::equals, "==", not_equals()),
        EQUALS_IGNORE_CASE(String::equalsIgnoreCase, "===", not_equals_ignore_case()),
        NOT_EQUALS_IGNORE_CASE(EQUALS_IGNORE_CASE.test.negate(), "!===", equals_ignore_case()),
        NOT_EQUALS(EQUALS.test.negate(), "!=", equals());

        private static final Map<String, Operation> BY_OPERATOR;
        private final BiPredicate<String, String> test;
        private final String name;
        private final LazyLoad<Operation> invert;


        Operation(BiPredicate<String, String> test, String name, LazyLoad<Operation> invert) {
            this.test = test;
            this.name = name;
            this.invert = invert;
        }
        public static @Nullable Operation byOperator(String op){
            return BY_OPERATOR.get(op);
        }

        public BiPredicate<String, String> test() {
            return test;
        }

        public @NotNull String configName() {
            return name;
        }

        public LazyLoad<Operation> invert() {
            return invert;
        }

        private static LazyLoad<Operation> contains() {
            return new LazyLoad<>(() -> CONTAINS);
        }

        private static LazyLoad<Operation> not_contains() {
            return new LazyLoad<>(() -> NOT_CONTAINS);
        }

        private static LazyLoad<Operation> contains_ignore_case() {
            return new LazyLoad<>(() -> CONTAINS_IGNORE_CASE);
        }

        private static LazyLoad<Operation> not_contains_ignore_case() {
            return new LazyLoad<>(() -> NOT_CONTAINS_IGNORE_CASE);
        }

        private static LazyLoad<Operation> equals_ignore_case() {
            return new LazyLoad<>(() -> EQUALS_IGNORE_CASE);
        }
        private static LazyLoad<Operation> not_equals_ignore_case() {
            return new LazyLoad<>(() -> NOT_EQUALS_IGNORE_CASE);
        }

        private static LazyLoad<Operation> equals() {
            return new LazyLoad<>(() -> EQUALS);
        }

        private static LazyLoad<Operation> not_equals() {
            return new LazyLoad<>(() -> NOT_EQUALS);
        }
        static {
            BY_OPERATOR = new HashMap<>();
            for (Operation value : values()) {
                BY_OPERATOR.put(value.name, value);
            }
        }
    }
}

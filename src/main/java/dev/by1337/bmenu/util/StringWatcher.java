package dev.by1337.bmenu.util;

import java.util.Objects;
import java.util.function.Consumer;

public class StringWatcher {
    private String str;
    private final Consumer<String> onChange;

    public StringWatcher(String str, Consumer<String> onChange) {
        this.str = str;
        this.onChange = onChange;
    }

    public String data() {
        return str;
    }

    public void setData(String str) {
        if (!Objects.equals(str, this.str)){
            onChange.accept(this.str = str);
        }
    }
}

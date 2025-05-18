package org.by1337.bmenu.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.by1337.blib.BLib;
import org.by1337.bmenu.util.math.MathReplacer;

public class CachedComponent {
    private final String source;
    private final Component cached;

    public CachedComponent(String source) {
        this.source = source;
        if (canBeCached(source)) {
            cached = BLib.getApi().getMessage().componentBuilder(MathReplacer.safeReplace(source)).decoration(TextDecoration.ITALIC, false);
        } else {
            cached = null;
        }
    }
    public boolean isCached(){
        return cached != null;
    }

    public String getSource() {
        return source;
    }

    public Component getCached() {
        return cached;
    }

    private static boolean canBeCached(String s) {
        return !s.contains("{") && !s.contains("%");
    }
}

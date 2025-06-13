package org.by1337.bmenu.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.by1337.blib.BLib;
import org.by1337.blib.util.Version;
import org.by1337.bmenu.util.math.MathReplacer;
import org.jetbrains.annotations.Nullable;

public class CachedComponent {
    private static final boolean JSON_SUPPORT = Version.is1_20_4orOlder();
    private final String source;
    private final Component cached;
    private final String cachedJson;

    public CachedComponent(String source) {
        this.source = source;
        if (canBeCached(source)) {
            cached = BLib.getApi().getMessage().componentBuilder(MathReplacer.safeReplace(source)).decoration(TextDecoration.ITALIC, false);
            if (JSON_SUPPORT){
                cachedJson = GsonComponentSerializer.gson().serializer().toJson(cached);
            }else {
                cachedJson = null;
            }
        } else {
            cached = null;
            cachedJson = null;
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

    public @Nullable String getCachedJson() {
        return cachedJson;
    }

    private static boolean canBeCached(String s) {
        return !s.contains("{") && !s.contains("%");
    }
}

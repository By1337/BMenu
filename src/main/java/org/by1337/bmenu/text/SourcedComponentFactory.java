package org.by1337.bmenu.text;


import dev.by1337.core.ServerVersion;

public class SourcedComponentFactory {
    private static final boolean JSON_SUPPORT = ServerVersion.is1_20_4orOlder();

    public static SourcedComponentLike create(String source) {
        if (canBeCached(source)) {
            if (JSON_SUPPORT) {
                return new JsonComponent(source);
            }
            return new SourcedComponent(source);
        } else {
            return new RawTextComponent(source);
        }
    }

    private static boolean canBeCached(String s) {
        return !s.contains("{") && !s.contains("%");
    }
}

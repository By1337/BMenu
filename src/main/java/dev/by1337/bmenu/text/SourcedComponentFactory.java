package dev.by1337.bmenu.text;


import dev.by1337.core.ServerVersion;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;

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

    public static SourcedComponentLike of(ComponentLike componentLike) {
        if (componentLike instanceof SourcedComponentLike s) return s;
        var src = Bukkit.getUnsafe().legacyComponentSerializer().serialize(componentLike.asComponent());
        return create(src);
    }

    private static boolean canBeCached(String s) {
        return !s.contains("{") && !s.contains("%");
    }
}

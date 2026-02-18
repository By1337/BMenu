package dev.by1337.bmenu.placeholder;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.PlaceholderSyntax;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderResolverList implements PlaceholderResolver<Menu> {
    private final List<PlaceholderResolver<Menu>> resolvers = new ArrayList<>();

    public void addResolver(PlaceholderResolver<Menu> resolver){
        resolvers.add(resolver);
    }

    @Override
    public boolean has(String s, PlaceholderSyntax placeholderSyntax) {
        for (PlaceholderResolver<Menu> resolver : resolvers) {
            if (resolver.has(s, placeholderSyntax)) return true;
        }
        return false;
    }

    @Override
    public @Nullable String resolve(String s, String s1, @Nullable Menu menu, PlaceholderSyntax placeholderSyntax) {
        for (PlaceholderResolver<Menu> resolver : resolvers) {
            var v = resolver.resolve(s, s1, menu, placeholderSyntax);
            if (v != null) return v;
        }
        return null;
    }
}

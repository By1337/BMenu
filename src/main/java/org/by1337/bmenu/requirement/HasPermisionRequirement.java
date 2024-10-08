package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuLoader;

public class HasPermisionRequirement implements Requirement {
    private final String permission;
    private final boolean not;

    public HasPermisionRequirement(YamlContext context, Placeholder argsReplacer) {
        permission = argsReplacer.replace(context.getAsString("permission"));
        not = context.getAsString("type").startsWith("!");
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        return not ? !clicker.hasPermission(placeholderable.replace(permission)) : clicker.hasPermission(placeholderable.replace(permission));
    }
}

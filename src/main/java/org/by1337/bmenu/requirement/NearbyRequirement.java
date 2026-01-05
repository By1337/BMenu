package org.by1337.bmenu.requirement;

import dev.by1337.plc.PlaceholderResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.by1337.bmenu.menu.Menu;

import java.util.List;

public class NearbyRequirement implements Requirement {
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final int radius;
    private final int radiusSq;
    private final boolean not;
    private final List<String> commands;
    private final List<String> denyCommands;

    public NearbyRequirement(String world, int x, int y, int z, int radius, boolean not, List<String> commands, List<String> denyCommands) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.not = not;
        this.commands = commands;
        this.denyCommands = denyCommands;
        radiusSq = radius * radius;
    }


    @Override
    public boolean test(Menu menu, PlaceholderResolver<Menu> placeholderable, Player clicker) {
        if (!clicker.getWorld().getName().equals(world)) {
            return not;
        }
        return not != (distanceSquared(clicker.getLocation()) < radiusSq);
    }

    private double distanceSquared(Location loc) {
        return square(x - loc.getX()) + square(y - loc.getY()) + square(z - loc.getZ());
    }

    private double square(double d) {
        return d * d;
    }

    @Override
    public List<String> getCommands() {
        return commands;
    }

    @Override
    public List<String> getDenyCommands() {
        return denyCommands;
    }
}

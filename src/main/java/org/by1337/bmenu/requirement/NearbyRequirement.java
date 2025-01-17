package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.geom.Vec3d;
import org.by1337.bmenu.Menu;

import java.util.List;

public class NearbyRequirement implements Requirement {
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final int radius;
    private final int radiusSq;
    private final boolean not;
    private final List<String> commands;
    private final List<String> denyCommands;
    private final Vec3d pos;

    public NearbyRequirement(String world, int x, int y, int z, int radius, boolean not, List<String> commands, List<String> denyCommands) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.not = not;
        this.commands = commands;
        this.denyCommands = denyCommands;
        pos = new Vec3d(x, y, z);
        radiusSq = radius * radius;
    }


    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        if (!clicker.getWorld().getName().equals(world)) {
            return not;
        }
        return not != (pos.distanceSquared(new Vec3d(clicker.getLocation())) < radiusSq);
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

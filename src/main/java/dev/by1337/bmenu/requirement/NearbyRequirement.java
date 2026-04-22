package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.PlayerContext;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.InlineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Location;
import org.jetbrains.annotations.UnknownNullability;

public class NearbyRequirement implements Requirement {
    public static YamlCodec<NearbyRequirement> CODEC = InlineYamlCodecBuilder.inline(
            "\\s+",
            "nearby <wolrd> <x> <y> <z> <radius>",
            NearbyRequirement::new,
            YamlCodec.STRING.fieldOf(null, v -> v.name),
            YamlCodec.STRING.fieldOf(null, v -> v.world),
            YamlCodec.DOUBLE.fieldOf(null, v -> v.x),
            YamlCodec.DOUBLE.fieldOf(null, v -> v.y),
            YamlCodec.DOUBLE.fieldOf(null, v -> v.z),
            YamlCodec.INT.fieldOf(null, v -> v.radius)
    );
    private final String name;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final int radius;
    private final int radiusSq;
    private boolean not;
    private final String value;

    public NearbyRequirement(String name, String world, double x, double y, double z, int radius) {
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.radiusSq = radius * radius;
        not = name.startsWith("!");
        value = toString();
    }

    @Override
    public boolean test(@UnknownNullability PlayerContext ctx, PlaceholderApplier placeholders) {
        var pl = ctx.getPlayer();
        if (!pl.getWorld().getName().equals(world)) {
            ctx.tracer().log("if '%s' -> %s", value, not);
            return not;
        }
        var v = not != (distanceSquared(pl.getLocation()) < radiusSq);
        ctx.tracer().log("if '%s' -> %s", value, v);
        return v;
    }

    private double distanceSquared(Location loc) {
        return square(x - loc.getX()) + square(y - loc.getY()) + square(z - loc.getZ());
    }

    private double square(double d) {
        return d * d;
    }

    @Override
    public String toString() {
        return name + " " + world + " " + x + " " + y + " " + z + " " + radius;
    }
}

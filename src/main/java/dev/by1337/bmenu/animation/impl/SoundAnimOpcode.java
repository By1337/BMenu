package dev.by1337.bmenu.animation.impl;

import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.item.MenuItem;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SoundAnimOpcode implements FrameOpcode {
    public static YamlCodec<SoundAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER).map(
            SoundAnimOpcode::new,
            o -> o.sound.getKey().getKey() + " " + o.volume + " " + o.pitch
    ).schema(
            SchemaTypes.STRING.or(BukkitYamlCodecs.SOUND.schema())
    );
    private Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAnimOpcode(String ctx) {
        String string = ctx;
        String[] args = string.split(" ");
        String snd = args[0];
        try {
            if (snd.contains(":")) {
                sound = Registry.SOUNDS.get(NamespacedKey.fromString(snd));
            } else {
                sound = Registry.SOUNDS.get(NamespacedKey.minecraft(snd));
            }
        }catch (IllegalArgumentException ignore){
        }
        if (sound == null) {
            sound = Sound.valueOf(snd.toUpperCase(Locale.ENGLISH));
        }
        if (args.length > 1) {
            volume = Float.parseFloat(args[1]);
            if (args.length > 2) {
                pitch = Float.parseFloat(args[2]);
            } else {
                pitch = 1f;
            }
        } else {
            volume = 1f;
            pitch = 1f;
        }
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        Player viewer = menu.getViewer();
        viewer.playSound(viewer.getLocation(), sound, volume, pitch);
    }

    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.SOUND;
    }
}

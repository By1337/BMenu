package org.by1337.bmenu.animation.impl;

import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.Sound;
import dev.by1337.yaml.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.FrameOpcodes;
import org.jetbrains.annotations.Nullable;

public class SoundAnimOpcode implements FrameOpcode {
    public static YamlCodec<SoundAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER).map(
            SoundAnimOpcode::new,
            o -> o.sound.getKey().getKey() + " " + o.volume + " " + o.pitch
    ).schema(
            SchemaTypes.STRING.or(BukkitYamlCodecs.SOUND.schema())
    );
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAnimOpcode(String ctx) {
        String string = ctx;
        String[] args = string.split(" ");
        sound = Sound.valueOf(args[0]); //todo?
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
        menu.getLoader().getMessage().sendSound(menu.getViewer(), sound, volume, pitch);
    }
    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.SOUND;
    }
}

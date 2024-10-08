package org.by1337.bmenu.animation.impl;

import org.bukkit.Sound;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;

public class SoundAnimOpcode implements FrameOpcode {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAnimOpcode(YamlValue ctx) {
        String string = ctx.getAsString();
        String[] args = string.split(" ");
        sound = Sound.valueOf(args[0]);
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
}

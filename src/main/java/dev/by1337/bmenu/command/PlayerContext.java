package dev.by1337.bmenu.command;

import dev.by1337.bmenu.handler.trace.EventTracer;
import org.bukkit.entity.Player;


public interface PlayerContext {
    Player getPlayer();
    EventTracer tracer();
}

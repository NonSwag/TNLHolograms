package net.nonswag.tnl.holograms.listeners;

import net.nonswag.tnl.holograms.Holograms;
import net.nonswag.tnl.listener.v1_15_R1.api.player.TNLPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class KickListener implements Listener {

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (!event.isCancelled()) {
            Holograms.unloadAll(TNLPlayer.cast(event.getPlayer()));
        }
    }
}

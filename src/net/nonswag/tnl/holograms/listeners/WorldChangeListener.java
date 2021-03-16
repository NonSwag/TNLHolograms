package net.nonswag.tnl.holograms.listeners;

import net.nonswag.tnl.holograms.Holograms;
import net.nonswag.tnl.listener.api.player.v1_15_R1.NMSPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!event.getFrom().equals(event.getPlayer().getWorld())) {
            Holograms.reloadAll(NMSPlayer.cast(event.getPlayer()));
        }
    }
}

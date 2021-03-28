package net.nonswag.tnl.holograms.listeners;

import net.nonswag.tnl.holograms.Holograms;
import net.nonswag.tnl.listener.api.player.v1_15.R1.NMSPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Holograms.unloadAll(NMSPlayer.cast(event.getPlayer()));
    }
}

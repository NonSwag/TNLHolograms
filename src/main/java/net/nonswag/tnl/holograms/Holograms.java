package net.nonswag.tnl.holograms;

import net.nonswag.tnl.holograms.commands.HologramCommand;
import net.nonswag.tnl.holograms.completer.HologramCommandTabCompleter;
import net.nonswag.tnl.listener.api.command.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Holograms extends JavaPlugin {

    @Override
    public void onEnable() {
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand("hologram", "tnl.holograms", new HologramCommand(), new HologramCommandTabCompleter());
    }
}

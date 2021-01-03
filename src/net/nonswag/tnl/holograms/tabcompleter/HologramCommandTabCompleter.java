package net.nonswag.tnl.holograms.tabcompleter;

import net.nonswag.tnl.holograms.Holograms;
import net.nonswag.tnl.holograms.api.Option;
import net.nonswag.tnl.listener.NMSMain;
import net.nonswag.tnl.listener.v1_15_R1.TNLListener;
import net.nonswag.tnl.listener.v1_15_R1.api.playerAPI.TNLPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class HologramCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length <= 1) {
            suggestions.add("list");
            suggestions.add("save");
            suggestions.add("delete");
            suggestions.add("create");
            suggestions.add("load");
            suggestions.add("unload");
            suggestions.add("set");
            suggestions.add("teleport");
            suggestions.add("reload");
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("teleport")) {
                if (args.length < 3) {
                    suggestions.addAll(Holograms.cachedNameValues());
                } else if (args.length == 3) {
                    suggestions.addAll(NMSMain.getWorlds());
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                if (args.length < 3) {
                    suggestions.addAll(Holograms.cachedNameValues());
                } else if (args.length == 3) {
                    for (Option option : Option.values()) {
                        suggestions.add(option.getName());
                    }
                } else if (args.length == 4) {
                    try {
                        suggestions.addAll(Option.valueOf(args[2].toUpperCase()).getValues());
                    } catch (Throwable ignored) {
                    }
                }
            } else if (args[0].equalsIgnoreCase("unload")) {
                if (args.length < 3) {
                    suggestions.addAll(Holograms.cachedNameValues());
                } else if (args.length == 3) {
                    for (TNLPlayer all : TNLListener.getOnlinePlayers()) {
                        suggestions.add(all.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("load")) {
                if (args.length < 3) {
                    suggestions.addAll(Holograms.cachedNameValues());
                } else if (args.length == 3) {
                    for (TNLPlayer all : TNLListener.getOnlinePlayers()) {
                        suggestions.add(all.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length == 2) {
                    suggestions.addAll(Holograms.list());
                }
            } else if (args[0].equalsIgnoreCase("save")) {
                if (args.length == 2) {
                    suggestions.addAll(Holograms.cachedNameValues());
                }
            }
        }
        if (!suggestions.isEmpty() && args.length >= 1) {
            suggestions.removeIf(suggestion -> !suggestion.toLowerCase().startsWith(args[args.length - 1]));
        }
        return suggestions;
    }
}

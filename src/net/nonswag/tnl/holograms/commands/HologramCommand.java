package net.nonswag.tnl.holograms.commands;

import net.nonswag.tnl.holograms.Holograms;
import net.nonswag.tnl.holograms.api.Hologram;
import net.nonswag.tnl.holograms.api.Option;
import net.nonswag.tnl.listener.api.message.ChatComponent;
import net.nonswag.tnl.listener.api.player.v1_15_R1.NMSPlayer;
import net.nonswag.tnl.listener.api.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class HologramCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            NMSPlayer player = NMSPlayer.cast((Player) sender);
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("create")) {
                    if (args.length >= 2) {
                        String name = args[1];
                        if (Holograms.get(name) == null) {
                            if (args.length >= 3) {
                                Hologram hologram = Holograms.create(name, true);
                                hologram.setLocation(player.getLocation());
                                hologram.addLines(Arrays.asList(args).subList(2, args.length));
                                hologram.save();
                                hologram.loadAll();
                                sender.sendMessage(ChatComponent.getText("%prefix% §aSuccessfully created hologram §6" + hologram.getName()));
                            } else {
                                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram create " + name + " §8[§6Lines§8]"));
                            }
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §cAn hologram with the name §4" + Holograms.get(name).getName() + "§c does already exist"));
                        }
                    } else {
                        sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram create §8[§6Name§8] §8[§6Lines§8]"));
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    List<String> nameValues = Holograms.list();
                    sender.sendMessage(ChatComponent.getText("%prefix% §7Holograms §8(§6" + nameValues.size() + "§8): §6" + String.join("§8, §6", nameValues)));
                } else if (args[0].equalsIgnoreCase("set")) {
                    if (args.length >= 2) {
                        Hologram hologram = Holograms.get(args[1]);
                        if (hologram != null) {
                            if (args.length >= 3) {
                                try {
                                    Option option = Option.valueOf(args[2].toUpperCase());
                                    if (args.length >= 4) {
                                        if (option.getClazz().equals(Double.class)) {
                                            try {
                                                double value = Double.parseDouble(args[3]);
                                                if (option.equals(Option.LINE_DISTANCE)) {
                                                    if (hologram.getLineDistance() != value) {
                                                        hologram.setLineDistance(value);
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §aUpdated the line distance of the hologram §6" + hologram.getName()));
                                                    } else {
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §cNothing has changed"));
                                                    }
                                                } else if (option.equals(Option.X_POSITION)) {
                                                    if (hologram.getX() != value) {
                                                        hologram.getLocation().setX(value);
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §aUpdated the location of the hologram §6" + hologram.getName()));
                                                    } else {
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §cNothing has changed"));
                                                    }
                                                } else if (option.equals(Option.Y_POSITION)) {
                                                    if (hologram.getY() != value) {
                                                        hologram.getLocation().setY(value);
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §aUpdated the location of the hologram §6" + hologram.getName()));
                                                    } else {
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §cNothing has changed"));
                                                    }
                                                } else if (option.equals(Option.Z_POSITION)) {
                                                    if (hologram.getZ() != value) {
                                                        hologram.getLocation().setZ(value);
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §aUpdated the location of the hologram §6" + hologram.getName()));
                                                    } else {
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §cNothing has changed"));
                                                    }
                                                } else {
                                                    sender.sendMessage(ChatComponent.getText("%prefix% §7Unrecognized Option§8: §6" + option.getName()));
                                                }
                                            } catch (Throwable t) {
                                                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set " + hologram.getName() + " " + option.getName() + " §8[§6" + option.getType() + "§8]"));
                                            }
                                        } else if (option.getClazz().equals(Integer.class)) {
                                            try {
                                                int value = Integer.parseInt(args[3]);
                                                if (option.equals(Option.DARKNESS)) {
                                                    if (hologram.getDarkness() != value) {
                                                        hologram.setDarkness(value);
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §aUpdated the darkness of the hologram §6" + hologram.getName()));
                                                    } else {
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §cNothing has changed"));
                                                    }
                                                } else {
                                                    sender.sendMessage(ChatComponent.getText("%prefix% §7Unrecognized Option§8: §6" + option.getName()));
                                                }
                                            } catch (Throwable t) {
                                                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set " + hologram.getName() + " " + option.getName() + " §8[§6" + option.getType() + "§8]"));
                                            }
                                        } else if (option.getClazz().equals(World.class)) {
                                            World world = Bukkit.getWorld(args[3]);
                                            if (world != null) {
                                                if (option.equals(Option.WORLD)) {
                                                    if (!hologram.getWorld().equals(world)) {
                                                        hologram.getLocation().setWorld(world);
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §aUpdated the location of the hologram §6" + hologram.getName()));
                                                    } else {
                                                        sender.sendMessage(ChatComponent.getText("%prefix% §cNothing has changed"));
                                                    }
                                                } else {
                                                    sender.sendMessage(ChatComponent.getText("%prefix% §7Unrecognized Option§8: §6" + option.getName()));
                                                }
                                            } else {
                                                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set " + hologram.getName() + " " + option.getName() + " §8[§6" + option.getType() + "§8]"));
                                            }
                                        } else {
                                            sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set " + hologram.getName() + " " + option.getName() + " §8[§6" + option.getType() + "§8]"));
                                        }
                                    } else {
                                        sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set " + hologram.getName() + " " + option.getName() + " §8[§6" + option.getType() + "§8]"));
                                    }
                                } catch (Throwable t) {
                                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set " + hologram.getName() + " §8[§6Option§8] §8[§6Value§8]"));
                                }
                            } else {
                                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set " + hologram.getName() + " §8[§6Option§8] §8[§6Value§8]"));
                            }
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set §8[§6Hologram§8] §8[§6Option§8] §8[§6Value§8]"));
                        }
                    } else {
                        sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set §8[§6Hologram§8] §8[§6Option§8] §8[§6Value§8]"));
                    }
                } else if (args[0].equalsIgnoreCase("teleport")) {
                    if (args.length >= 2) {
                        Hologram hologram = Holograms.get(args[1]);
                        if (hologram != null) {
                            if (args.length >= 3) {
                                NMSPlayer arg = NMSPlayer.cast(args[2]);
                                if (arg != null) {
                                    hologram.teleport(player.getLocation(), arg);
                                    sender.sendMessage(ChatComponent.getText("%prefix% §aTeleported hologram for §6" + arg.getName()));
                                } else {
                                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram teleport §8[§6Hologram§8] §8(§6Player§8)"));
                                }
                            } else {
                                hologram.setLocation(player.getLocation());
                                hologram.teleportAll(hologram.getLocation());
                                sender.sendMessage(ChatComponent.getText("%prefix% §aTeleported hologram for §6all§a players"));
                            }
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram teleport §8[§6Hologram§8] §8(§6Player§8)"));
                        }
                    } else {
                        sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram teleport §8[§6Hologram§8] §8(§6Player§8)"));
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    if (args.length >= 2) {
                        Hologram hologram = Holograms.get(args[1]);
                        if (hologram != null) {
                            hologram.delete();
                            sender.sendMessage(ChatComponent.getText("%prefix% §aSuccessfully deleted the hologram §6" + hologram.getName()));
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram delete §8[§6Hologram§8]"));
                        }
                    } else {
                        sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram delete §8[§6Hologram§8]"));
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    double now = System.currentTimeMillis();
                    List<Hologram> holograms = Holograms.cachedValues();
                    Title title = new Title("§7- §aSaving §7-", "§8[§60§8/§6" + holograms.size() + "§8]").setTimeStay(Integer.MAX_VALUE);
                    player.sendTitle(title);
                    int i = 0;
                    for (Hologram hologram : holograms) {
                        i++;
                        hologram.save();
                        hologram.reloadAll();
                        player.sendTitle(title.setTitle("§7- §aSaved §7-").setSubtitle("§8[§6" + i + "§8/§6" + holograms.size() + "§8]"));
                    }
                    player.sendTitle(title.setTimeStay(70));
                    player.sendMessage(ChatComponent.getText("§8[§f§lTNL§8] §aReloaded in §6" + ((System.currentTimeMillis() - now) / 1000) + " seconds"));
                } else if (args[0].equalsIgnoreCase("load")) {
                    if (args.length >= 2) {
                        Hologram hologram = Holograms.get(args[1]);
                        if (hologram != null) {
                            if (args.length >= 3) {
                                NMSPlayer arg = NMSPlayer.cast(args[2]);
                                if (arg != null) {
                                    hologram.load(arg);
                                    sender.sendMessage(ChatComponent.getText("%prefix% §aLoaded hologram for §6" + arg.getName()));
                                } else {
                                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram load §8[§6Hologram§8] §8(§6Player§8)"));
                                }
                            } else {
                                hologram.loadAll();
                                sender.sendMessage(ChatComponent.getText("%prefix% §aLoaded hologram for §6all§a players"));
                            }
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram load §8[§6Hologram§8] §8(§6Player§8)"));
                        }
                    } else {
                        sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram load §8[§6Hologram§8] §8(§6Player§8)"));
                    }
                } else if (args[0].equalsIgnoreCase("unload")) {
                    if (args.length >= 2) {
                        Hologram hologram = Holograms.get(args[1]);
                        if (hologram != null) {
                            if (args.length >= 3) {
                                NMSPlayer arg = NMSPlayer.cast(args[2]);
                                if (arg != null) {
                                    hologram.unload(arg);
                                    sender.sendMessage(ChatComponent.getText("%prefix% §aUnloaded hologram for §6" + arg.getName()));
                                } else {
                                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram unload §8[§6Hologram§8] §8(§6Player§8)"));
                                }
                            } else {
                                hologram.unloadAll();
                                sender.sendMessage(ChatComponent.getText("%prefix% §aUnloaded hologram for §6all§a players"));
                            }
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram unload §8[§6Hologram§8] §8(§6Player§8)"));
                        }
                    } else {
                        sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram unload §8[§6Hologram§8] §8(§6Player§8)"));
                    }
                } else if (args[0].equalsIgnoreCase("save")) {
                    if (args.length >= 2) {
                        Hologram hologram = Holograms.get(args[1]);
                        if (hologram != null) {
                            hologram.save();
                            sender.sendMessage(ChatComponent.getText("%prefix% §aSuccessfully saved the hologram §6" + hologram.getName()));
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram save §8[§6Hologram§8]"));
                        }
                    } else {
                        sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram save §8[§6Hologram§8]"));
                    }
                } else {
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram teleport §8[§6Hologram§8] §8(§6World§8) §8(§6X§8) §8(§6Y§8) §8(§6Z§8)"));
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set §8[§6Hologram§8] §8[§6Option§8] §8[§6Value§8]"));
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram unload §8[§6Hologram§8] §8(§6Player§8)"));
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram load §8[§6Hologram§8] §8(§6Player§8)"));
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram create §8[§6Name§8] §8[§6Lines§8]"));
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram delete §8[§6Hologram§8]"));
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram save §8[§6Hologram§8]"));
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram list"));
                }
            } else {
                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram teleport §8[§6Hologram§8] §8(§6World§8) §8(§6X§8) §8(§6Y§8) §8(§6Z§8)"));
                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram set §8[§6Hologram§8] §8[§6Option§8] §8[§6Value§8]"));
                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram unload §8[§6Hologram§8] §8(§6Player§8)"));
                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram load §8[§6Hologram§8] §8(§6Player§8)"));
                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram create §8[§6Name§8] §8[§6Lines§8]"));
                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram delete §8[§6Hologram§8]"));
                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram save §8[§6Hologram§8]"));
                sender.sendMessage(ChatComponent.getText("%prefix% §c/hologram list"));
            }
        } else {
            sender.sendMessage(ChatComponent.getText("%prefix% §cThis is a player command"));
        }
        return false;
    }
}

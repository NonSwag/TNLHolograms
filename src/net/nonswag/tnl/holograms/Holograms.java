package net.nonswag.tnl.holograms;

import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;
import net.nonswag.tnl.holograms.api.Hologram;
import net.nonswag.tnl.holograms.commands.HologramCommand;
import net.nonswag.tnl.holograms.listeners.JoinListener;
import net.nonswag.tnl.holograms.listeners.KickListener;
import net.nonswag.tnl.holograms.listeners.QuitListener;
import net.nonswag.tnl.holograms.listeners.WorldChangeListener;
import net.nonswag.tnl.holograms.tabcompleter.HologramCommandTabCompleter;
import net.nonswag.tnl.listener.NMSMain;
import net.nonswag.tnl.listener.api.fileAPI.Configuration;
import net.nonswag.tnl.listener.api.objectAPI.Object;
import net.nonswag.tnl.listener.v1_15_R1.TNLListener;
import net.nonswag.tnl.listener.v1_15_R1.api.playerAPI.TNLPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftArmorStand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Holograms extends JavaPlugin {

    @Nullable private static Plugin plugin;
    @Nonnull private static final Configuration saves = new Configuration("plugins/TNLHolograms/", "saves.tnldatabase");
    @Nonnull private static final HashMap<String, Hologram> hologramHashMap = new HashMap<>();
    @Nonnull private static final String serverName = new File("").getAbsoluteFile().getName();

    @Override
    public void onEnable() {
        setPlugin(this);
        unloadAll();
        PluginCommand hologramCommand = getCommand("hologram");
        if (hologramCommand != null) {
            hologramCommand.setExecutor(new HologramCommand());
            hologramCommand.setTabCompleter(new HologramCommandTabCompleter());
            hologramCommand.setPermission("tnl.hologram");
            hologramCommand.setPermissionMessage(NMSMain.getPrefix() + " §cYou have no Rights §8(§4tnl.hologram§8)");
        }
        NMSMain.registerEvents(new JoinListener(), getPlugin());
        NMSMain.registerEvents(new QuitListener(), getPlugin());
        NMSMain.registerEvents(new KickListener(), getPlugin());
        NMSMain.registerEvents(new WorldChangeListener(), getPlugin());
        loadAll();
    }

    private static void setPlugin(@Nullable Plugin plugin) {
        Holograms.plugin = plugin;
    }

    @Nullable
    public static Plugin getPlugin() {
        return plugin;
    }

    @Nonnull
    public static Configuration getSaves() {
        return saves;
    }

    @Nonnull
    public static String getServerName() {
        return serverName;
    }

    public static void loadAll(Hologram hologram) {
        for (TNLPlayer all : TNLListener.getOnlinePlayers()) {
            load(hologram, all);
        }
    }

    public static void loadAll(TNLPlayer player) {
        for (Hologram hologram : getHologramHashMap().values()) {
            load(hologram, player);
        }
    }

    public static void loadAll() {
        getHologramHashMap().clear();
        List<String> holograms = list();
        for (String s : holograms) {
            Hologram hologram = getOrDefault(s, new Hologram(s));
            try {
                hologram.setDarkness(new Object<>(getSaves().getInteger(hologram.getName() + "-darkness")).getOrDefault(1));
                hologram.setLineDistance(new Object<>(getSaves().getDouble(hologram.getName() + "-line-distance")).getOrDefault(0.25D));
                World world = Bukkit.getWorld(getSaves().getString(hologram.getName() + "-world"));
                double x = getSaves().getDouble(hologram.getName() + "-x-position");
                double y = getSaves().getDouble(hologram.getName() + "-y-position");
                double z = getSaves().getDouble(hologram.getName() + "-z-position");
                int lines = getSaves().getInteger(hologram.getName() + "-lines");
                hologram.setLocation(new Location(world, x, y, z));
                for (int i = 0; i < lines; i++) {
                    try {
                        hologram.getLines().add(getSaves().getString(hologram.getName() + "-line-" + i));
                    } catch (Throwable t) {
                        hologram.getLines().add("");
                    }
                }
            } catch (Throwable t) {
                NMSMain.stacktrace(t);
                continue;
            }
            loadAll(hologram);
        }
    }

    public static void load(Hologram hologram, TNLPlayer player) {
        if (player.getWorld().equals(hologram.getWorld())) {
            for (int line = 0; line < hologram.getLines().size(); line++) {
                if (hologram.getLines().get(line) == null || hologram.getLines().get(line).isEmpty()) {
                    continue;
                }
                for (int darkness = 0; darkness < hologram.getDarkness(); darkness++) {
                    EntityArmorStand entityArmorStand = new EntityArmorStand(((CraftWorld) player.getWorld()).getHandle(),
                            hologram.getX(),
                            (hologram.getY() - 1) + (line * hologram.getLineDistance()),
                            hologram.getZ());
                    CraftArmorStand armorStand = new CraftArmorStand(((CraftServer) Bukkit.getServer()), entityArmorStand);
                    armorStand.setVisible(false);
                    armorStand.setSmall(true);
                    armorStand.setCollidable(false);
                    armorStand.setInvulnerable(true);
                    armorStand.setGravity(false);
                    armorStand.setCustomName(hologram.getLines().get(line).
                            replace("&", "§").
                            replace(">>", "»").
                            replace("<<", "«").
                            replace("%player%", player.getName()).
                            replace("%display_name%", player.getDisplayName()).
                            replace("%language%", player.getLocale()).
                            replace("%server%", getServerName())
                    );
                    armorStand.setCustomNameVisible(true);
                    armorStand.setBasePlate(false);
                    player.sendPacket(new PacketPlayOutSpawnEntity(armorStand.getHandle()));
                    player.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getEntityId(), armorStand.getHandle().getDataWatcher(), true));
                    player.getVirtualStorage().put("hologram=" + hologram.getName() + ",line=" + line + ",darkness=" + darkness, armorStand.getEntityId());
                }
            }
        }
    }

    public static void reloadAll() {
        unloadAll();
        loadAll();
    }

    public static void reloadAll(TNLPlayer player) {
        unloadAll(player);
        loadAll(player);
    }

    public static void reloadAll(Hologram hologram) {
        unloadAll(hologram);
        loadAll(hologram);
    }

    public static void reload(Hologram hologram, TNLPlayer player) {
        unload(hologram, player);
        load(hologram, player);
    }

    public static void unloadAll(Hologram hologram) {
        for (TNLPlayer all : TNLListener.getOnlinePlayers()) {
            unload(hologram, all);
        }
    }

    public static void unloadAll(TNLPlayer player) {
        for (Hologram hologram : getHologramHashMap().values()) {
            unload(hologram, player);
        }
    }

    public static void unloadAll() {
        for (Hologram hologram : cachedValues()) {
            unloadAll(hologram);
        }
    }

    public static void unload(Hologram hologram, TNLPlayer player) {
        for (int line = 0; line < hologram.getLines().size(); line++) {
            if (hologram.getLines().get(line) == null || hologram.getLines().get(line).isEmpty()) {
                continue;
            }
            for (int darkness = 0; darkness < hologram.getDarkness(); darkness++) {
                String key = "hologram=" + hologram.getName() + ",line=" + line + ",darkness=" + darkness;
                java.lang.Object o = player.getVirtualStorage().get(key);
                if (o != null) {
                    if (o instanceof Integer) {
                        player.sendPacket(new PacketPlayOutEntityDestroy(((int) o)));
                    }
                    player.getVirtualStorage().remove(key);
                }
            }
        }
    }

    @Nonnull
    private static HashMap<String, Hologram> getHologramHashMap() {
        return hologramHashMap;
    }

    @Nonnull
    public static Hologram getOrDefault(@Nonnull String name, @Nonnull Hologram hologram) {
        return getHologramHashMap().getOrDefault(name.toLowerCase(), hologram);
    }

    @Nullable
    public static Hologram get(@Nonnull String name) {
        return getHologramHashMap().get(name.toLowerCase());
    }

    public static Hologram create(@Nonnull String name) {
        return new Hologram(name.toLowerCase());
    }

    public static void save(@Nonnull Hologram hologram) {
        getHologramHashMap().put(hologram.getName(), hologram);
    }

    public static void delete(@Nonnull Hologram hologram) {
        getHologramHashMap().remove(hologram.getName());
    }

    @Nonnull
    public static List<Hologram> cachedValues() {
        return new ArrayList<>(getHologramHashMap().values());
    }

    @Nonnull
    public static List<String> cachedNameValues() {
        return new ArrayList<>(getHologramHashMap().keySet());
    }

    @Nonnull
    public static List<String> list() {
        return new Object<>(getSaves().getStringList("holograms")).getOrDefault(new ArrayList<>());
    }
}

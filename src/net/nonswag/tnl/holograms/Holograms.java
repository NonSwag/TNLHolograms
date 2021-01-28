package net.nonswag.tnl.holograms;

import net.minecraft.server.v1_15_R1.*;
import net.nonswag.tnl.holograms.api.Hologram;
import net.nonswag.tnl.holograms.commands.HologramCommand;
import net.nonswag.tnl.holograms.listeners.JoinListener;
import net.nonswag.tnl.holograms.listeners.KickListener;
import net.nonswag.tnl.holograms.listeners.QuitListener;
import net.nonswag.tnl.holograms.listeners.WorldChangeListener;
import net.nonswag.tnl.holograms.runnables.UpdateRunnable;
import net.nonswag.tnl.holograms.tabcompleter.HologramCommandTabCompleter;
import net.nonswag.tnl.listener.NMSMain;
import net.nonswag.tnl.listener.api.file.Configuration;
import net.nonswag.tnl.listener.api.object.Object;
import net.nonswag.tnl.listener.api.server.Server;
import net.nonswag.tnl.listener.TNLListener;
import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.listener.utils.PacketUtil;
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
    private static long updateTime = 5000L;

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
        Bukkit.getPluginManager().registerEvents(new JoinListener(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new QuitListener(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new KickListener(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new WorldChangeListener(), getPlugin());
        if (getSaves().getLong("update-time") == null) {
            getSaves().setValue("update-time", updateTime);
        } else {
            setUpdateTime(getSaves().getLong("update-time"));
        }
        UpdateRunnable.start();
        loadAll();
    }

    @Override
    public void onDisable() {
        UpdateRunnable.stop();
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

    public static long getUpdateTime() {
        return updateTime;
    }

    public static void setUpdateTime(long updateTime) {
        Holograms.updateTime = updateTime;
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
            Hologram hologram = getOrDefault(s, new Hologram(s, true));
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
                    String s = hologram.getLines().get((hologram.getLines().size() - 1) - line).
                            replace("&", "§").
                            replace(">>", "»").
                            replace("<<", "«").
                            replace("%player%", player.getName()).
                            replace("%display_name%", player.getDisplayName()).
                            replace("%language%", player.getLocale()).
                            replace("%server%", getServerName()).
                            replace("%online%", Bukkit.getOnlinePlayers().size() + "").
                            replace("%max_online%", Bukkit.getMaxPlayers() + "").
                            replace("%world%", player.getWorld().getName() + "").
                            replace("%world_alias%", player.getWorldAlias() + "");
                    if (s.contains("%status_")
                            || s.contains("%online_")
                            || s.contains("%max_online_")
                            || s.contains("%players_")
                    ) {
                        for (Server server : Server.getServers()) {
                            if (s.contains("%status_" + server.getName() + "%")) {
                                s = s.replace("%status_" + server.getName() + "%", server.isOnline() ? "Online" : "Offline");
                            }
                            if (s.contains("%online_" + server.getName() + "%")) {
                                s = s.replace("%online_" + server.getName() + "%", server.getPlayerCount() + "");
                            }
                            if (s.contains("%max_online_" + server.getName() + "%")) {
                                s = s.replace("%max_online_" + server.getName() + "%", server.getMaxPlayerCount() + "");
                            }
                        }
                        for (World world : Bukkit.getWorlds()) {
                            if (s.contains("%players_" + world.getName() + "%")) {
                                s = s.replace("%players_" + world.getName() + "%", world.getPlayerCount() + "");
                            }
                        }
                    }
                    armorStand.setCustomName(s);
                    armorStand.setCustomNameVisible(true);
                    armorStand.setBasePlate(false);
                    player.sendPacket(new PacketPlayOutSpawnEntity(armorStand.getHandle()));
                    player.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getEntityId(), armorStand.getHandle().getDataWatcher(), true));
                    player.getVirtualStorage().put("hologram=" + hologram.getName() + ",line=" + line + ",darkness=" + darkness, armorStand.getEntityId());
                    player.getVirtualStorage().put("hologram-by-id=" + armorStand.getEntityId(), armorStand.getHandle());
                }
            }
        }
    }

    public static void update(Hologram hologram, TNLPlayer player) {
        for (int line = 0; line < hologram.getLines().size(); line++) {
            for (int darkness = 0; darkness < hologram.getDarkness(); darkness++) {
                java.lang.Object id = player.getVirtualStorage().get("hologram=" + hologram.getName() + ",line=" + line + ",darkness=" + darkness);
                java.lang.Object hologramById = player.getVirtualStorage().get("hologram-by-id=" + id);
                if (id instanceof Integer && hologramById instanceof EntityArmorStand) {
                    EntityArmorStand armorStand = ((EntityArmorStand) hologramById);
                    String s = hologram.getLines().get((hologram.getLines().size() - 1) - line).
                            replace("&", "§").
                            replace(">>", "»").
                            replace("<<", "«").
                            replace("%player%", player.getName()).
                            replace("%display_name%", player.getDisplayName()).
                            replace("%language%", player.getLocale()).
                            replace("%server%", getServerName()).
                            replace("%online%", Bukkit.getOnlinePlayers().size() + "").
                            replace("%max_online%", Bukkit.getMaxPlayers() + "").
                            replace("%world%", player.getWorld().getName() + "").
                            replace("%world_alias%", player.getWorldAlias() + "");
                    if (s.contains("%status_")
                            || s.contains("%online_")
                            || s.contains("%max_online_")
                            || s.contains("%players_")
                    ) {
                        for (Server server : Server.getServers()) {
                            if (s.contains("%status_" + server.getName() + "%")) {
                                s = s.replace("%status_" + server.getName() + "%", server.isOnline() ? "Online" : "Offline");
                            }
                            if (s.contains("%online_" + server.getName() + "%")) {
                                s = s.replace("%online_" + server.getName() + "%", server.getPlayerCount() + "");
                            }
                            if (s.contains("%max_online_" + server.getName() + "%")) {
                                s = s.replace("%max_online_" + server.getName() + "%", server.getMaxPlayerCount() + "");
                            }
                        }
                        for (World world : Bukkit.getWorlds()) {
                            if (s.contains("%players_" + world.getName() + "%")) {
                                s = s.replace("%players_" + world.getName() + "%", world.getPlayerCount() + "");
                            }
                        }
                    }
                    armorStand.setCustomName(new ChatMessage(s));
                    armorStand.setCustomNameVisible(true);
                    PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(((Integer) id), armorStand.getDataWatcher(), true);
                    player.sendPacket(metadataPacket);
                }
            }
        }
    }

    public static void updateAll(Hologram hologram) {
        for (TNLPlayer all : TNLListener.getOnlinePlayers()) {
            update(hologram, all);
        }
    }

    public static void teleport(Hologram hologram, Location location, TNLPlayer player) {
        for (int line = 0; line < hologram.getLines().size(); line++) {
            for (int darkness = 0; darkness < hologram.getDarkness(); darkness++) {
                java.lang.Object id = player.getVirtualStorage().get("hologram=" + hologram.getName() + ",line=" + line + ",darkness=" + darkness);
                if (id instanceof Integer) {
                    PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport();
                    PacketUtil.setPacketField(teleportPacket, "a", id);
                    PacketUtil.setPacketField(teleportPacket, "b", location.getX());
                    PacketUtil.setPacketField(teleportPacket, "c", (location.getY() - 1) + (line * hologram.getLineDistance()));
                    PacketUtil.setPacketField(teleportPacket, "d", location.getZ());
                    player.sendPacket(teleportPacket);
                }
            }
        }
    }

    public static void teleportAll(Hologram hologram, Location location) {
        for (TNLPlayer all : TNLListener.getOnlinePlayers()) {
            teleport(hologram, location, all);
        }
    }

    public static void teleportAll(Hologram hologram, double offsetX, double offsetY, double offsetZ) {
        hologram.teleportAll(hologram.getLocation().clone().add(offsetX, offsetY, offsetZ));
    }

    public static void teleport(Hologram hologram, double offsetX, double offsetY, double offsetZ, TNLPlayer player) {
        hologram.teleport(hologram.getLocation().clone().add(offsetX, offsetY, offsetZ), player);
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

    public static Hologram create(@Nonnull String name, boolean cache) {
        return new Hologram(name.toLowerCase(), cache);
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

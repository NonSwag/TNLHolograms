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
import net.nonswag.tnl.listener.api.command.CommandManager;
import net.nonswag.tnl.listener.api.file.Configuration;
import net.nonswag.tnl.listener.api.logger.Logger;
import net.nonswag.tnl.listener.api.object.Objects;
import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.listener.api.player.v1_15_R1.NMSPlayer;
import net.nonswag.tnl.listener.api.plugin.PluginUpdate;
import net.nonswag.tnl.listener.api.reflection.Reflection;
import net.nonswag.tnl.listener.api.server.Server;
import net.nonswag.tnl.listener.TNLListener;
import net.nonswag.tnl.listener.api.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Holograms extends JavaPlugin {

    private static Holograms instance;

    @Nonnull
    private static final Configuration saves = new Configuration("plugins/TNLHolograms/", "saves.tnldatabase");
    @Nonnull
    private static final HashMap<String, Hologram> hologramHashMap = new HashMap<>();
    @Nonnull
    private static final String serverName = new File("").getAbsoluteFile().getName();
    private static long updateTime = 5000L;

    @Override
    public void onEnable() {
        setInstance(this);
        unloadAll();
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand("hologram", "tnl.holograms", new HologramCommand(), new HologramCommandTabCompleter());
        Bukkit.getPluginManager().registerEvents(new JoinListener(), getInstance());
        Bukkit.getPluginManager().registerEvents(new QuitListener(), getInstance());
        Bukkit.getPluginManager().registerEvents(new KickListener(), getInstance());
        Bukkit.getPluginManager().registerEvents(new WorldChangeListener(), getInstance());
        if (getSaves().getLong("update-time") == null) {
            getSaves().setValue("update-time", updateTime);
        } else {
            setUpdateTime(getSaves().getLong("update-time"));
        }
        UpdateRunnable.start();
        loadAll();
        if (Settings.AUTO_UPDATER.getValue()) {
            new PluginUpdate(getInstance()).downloadUpdate();
        }
    }

    @Override
    public void onDisable() {
        UpdateRunnable.stop();
    }

    protected static void setInstance(@Nonnull Holograms instance) {
        Holograms.instance = instance;
    }

    public static Holograms getInstance() {
        return instance;
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

    public static void loadAll(@Nonnull Hologram hologram) {
        for (TNLPlayer<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> all : TNLListener.getInstance().getOnlinePlayers()) {
            load(hologram, ((NMSPlayer) all));
        }
    }

    public static void loadAll(@Nonnull NMSPlayer player) {
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
                hologram.setDarkness(Objects.getOrDefault(getSaves().getInteger(hologram.getName() + "-darkness"), 1));
                hologram.setLineDistance(Objects.getOrDefault(getSaves().getDouble(hologram.getName() + "-line-distance"), 0.25D));
                World world = Bukkit.getWorld(getSaves().getString(hologram.getName() + "-world"));
                double x = getSaves().getDouble(hologram.getName() + "-x-position");
                double y = getSaves().getDouble(hologram.getName() + "-y-position");
                double z = getSaves().getDouble(hologram.getName() + "-z-position");
                int lines = getSaves().getInteger(hologram.getName() + "-lines");
                hologram.setLocation(new Location(world, x, y, z));
                for (int i = 0; i < lines; i++) {
                    String string = getSaves().getString(hologram.getName() + "-line-" + i);
                    if (string != null && !string.replace(" ", "").isEmpty()) {
                        hologram.getLines().add(string);
                    } else {
                        hologram.getLines().add("");
                    }
                }
            } catch (Exception e) {
                Logger.error.println(e);
                continue;
            }
            loadAll(hologram);
        }
    }

    public static void load(@Nonnull Hologram hologram, @Nonnull NMSPlayer player) {
        if (player.getWorld().equals(hologram.getWorld())) {
            for (int line = 0; line < hologram.getLines().size(); line++) {
                if (hologram.getLines().get((hologram.getLines().size() - 1) - line) == null || hologram.getLines().get((hologram.getLines().size() - 1) - line).isEmpty()) {
                    continue;
                }
                for (int darkness = 0; darkness < hologram.getDarkness(); darkness++) {
                    EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) player.getWorld()).getHandle(),
                            hologram.getX(),
                            (hologram.getY() - 1) + (line * hologram.getLineDistance()),
                            hologram.getZ());
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
                    if (s.contains("%status_") || s.contains("%online_") || s.contains("%max_online_") || s.contains("%players_")) {
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
                    armorStand.setInvisible(true);
                    armorStand.setSmall(true);
                    armorStand.setCustomNameVisible(true);
                    armorStand.setBasePlate(true);
                    armorStand.setMarker(false);
                    armorStand.setCustomName(new ChatMessage(s));
                    player.sendPacket(new PacketPlayOutSpawnEntity(armorStand));
                    player.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
                    player.getVirtualStorage().put("hologram=" + hologram.getName() + ",line=" + line + ",darkness=" + darkness, armorStand.getId());
                    player.getVirtualStorage().put("hologram-by-id=" + armorStand.getId(), armorStand);
                }
            }
        }
    }

    public static void update(@Nonnull Hologram hologram, @Nonnull NMSPlayer player) {
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
                        armorStand.setCustomName(new ChatMessage(s));
                        armorStand.setCustomNameVisible(true);
                        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(((Integer) id), armorStand.getDataWatcher(), true);
                        player.sendPacket(metadataPacket);
                    }
                }
            }
        }
    }

    public static void updateAll(@Nonnull Hologram hologram) {
        for (TNLPlayer<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> all : TNLListener.getInstance().getOnlinePlayers()) {
            update(hologram, ((NMSPlayer) all));
        }
    }

    public static void teleport(@Nonnull Hologram hologram, @Nonnull Location location, @Nonnull NMSPlayer player) {
        for (int line = 0; line < hologram.getLines().size(); line++) {
            for (int darkness = 0; darkness < hologram.getDarkness(); darkness++) {
                java.lang.Object id = player.getVirtualStorage().get("hologram=" + hologram.getName() + ",line=" + line + ",darkness=" + darkness);
                if (id instanceof Integer) {
                    PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport();
                    Reflection.setField(teleportPacket, "a", id);
                    Reflection.setField(teleportPacket, "b", location.getX());
                    Reflection.setField(teleportPacket, "c", (location.getY() - 1) + (line * hologram.getLineDistance()));
                    Reflection.setField(teleportPacket, "d", location.getZ());
                    player.sendPacket(teleportPacket);
                }
            }
        }
    }

    public static void teleportAll(@Nonnull Hologram hologram, @Nonnull Location location) {
        for (TNLPlayer<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> all : TNLListener.getInstance().getOnlinePlayers()) {
            teleport(hologram, location, ((NMSPlayer) all));
        }
    }

    public static void teleportAll(@Nonnull Hologram hologram, double offsetX, double offsetY, double offsetZ) {
        hologram.teleportAll(hologram.getLocation().clone().add(offsetX, offsetY, offsetZ));
    }

    public static void teleport(@Nonnull Hologram hologram, double offsetX, double offsetY, double offsetZ, @Nonnull NMSPlayer player) {
        hologram.teleport(hologram.getLocation().clone().add(offsetX, offsetY, offsetZ), player);
    }

    public static void reloadAll() {
        unloadAll();
        loadAll();
    }

    public static void reloadAll(@Nonnull NMSPlayer player) {
        unloadAll(player);
        loadAll(player);
    }

    public static void reloadAll(@Nonnull Hologram hologram) {
        unloadAll(hologram);
        loadAll(hologram);
    }

    public static void reload(@Nonnull Hologram hologram, @Nonnull NMSPlayer player) {
        unload(hologram, player);
        load(hologram, player);
    }

    public static void unloadAll(@Nonnull Hologram hologram) {
        for (TNLPlayer<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> all : TNLListener.getInstance().getOnlinePlayers()) {
            unload(hologram, (NMSPlayer) all);
        }
    }

    public static void unloadAll(@Nonnull NMSPlayer player) {
        for (Hologram hologram : getHologramHashMap().values()) {
            unload(hologram, player);
        }
    }

    public static void unloadAll() {
        for (Hologram hologram : cachedValues()) {
            unloadAll(hologram);
        }
    }

    public static void unload(@Nonnull Hologram hologram, @Nonnull NMSPlayer player) {
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
        return Objects.getOrDefault(getSaves().getStringList("holograms"), new ArrayList<>());
    }
}

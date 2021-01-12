package net.nonswag.tnl.holograms.api;

import net.nonswag.tnl.holograms.Holograms;
import net.nonswag.tnl.listener.NMSMain;
import net.nonswag.tnl.listener.v1_15_R1.api.player.TNLPlayer;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Hologram {

    /*
    %player% -> The name of the player who is reading this
    %display_name% -> The display name of the player who is reading this
    %language% -> The client language of the player who is reading this
    %server% -> The name of the current server
    %status_$SERVER% -> The status of the server (Online/Offline)
    %online_$SERVER% -> The player count of the server
    %max_online_$SERVER% -> The maximum player count of the server

    & = §
    >> = »
    << = «
     */

    @Nonnull private final String name;
    @Nonnull private final List<String> lines = new ArrayList<>();
    @Nullable private Location location;
    private double lineDistance = 0.25D;
    private int darkness = 1;

    public Hologram(@Nonnull String name, boolean cache, String... lines) {
        this.name = name.toLowerCase();
        getLines().addAll(Arrays.asList(lines));
        if (cache) {
            Holograms.save(this);
        }
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public List<String> getLines() {
        return lines;
    }

    public void addLines(String... lines) {
        getLines().addAll(Arrays.asList(lines));
    }

    public void addLines(List<String> lines) {
        getLines().addAll(lines);
    }

    public void addLine(String line) {
        getLines().add(line);
    }

    public double getLineDistance() {
        return lineDistance;
    }

    public int getDarkness() {
        return darkness;
    }

    public void setLineDistance(double lineDistance) {
        this.lineDistance = lineDistance;
    }

    public void setDarkness(int darkness) {
        if (darkness > 10) {
            darkness = 10;
            NMSMain.stacktrace(new IllegalArgumentException("The hologram darkness can't be higher then 10"));
        } else if (darkness < 1) {
            darkness = 1;
            NMSMain.stacktrace(new IllegalArgumentException("The hologram darkness can't be lower then 1"));
        }
        this.darkness = darkness;
    }

    public void setLocation(@Nonnull Location location) {
        this.location = location;
    }

    @Nullable
    public Location getLocation() {
        return location;
    }

    public double getX() {
        return getLocation().getX();
    }

    public double getY() {
        return getLocation().getY();
    }

    public double getZ() {
        return getLocation().getZ();
    }

    public World getWorld() {
        return getLocation().getWorld();
    }

    public void save() {
        Holograms.getSaves().setValue(this.getName() + "-darkness", this.getDarkness());
        Holograms.getSaves().setValue(this.getName() + "-line-distance", this.getLineDistance());
        Holograms.getSaves().setValue(this.getName() + "-world", this.getLocation().getWorld().getName());
        Holograms.getSaves().setValue(this.getName() + "-x-position", this.getLocation().getX());
        Holograms.getSaves().setValue(this.getName() + "-y-position", this.getLocation().getY());
        Holograms.getSaves().setValue(this.getName() + "-z-position", this.getLocation().getZ());
        Holograms.getSaves().setValue(this.getName() + "-lines", this.getLines().size());
        for (int i = 0; i < this.getLines().size(); i++) {
            if (this.getLines().get(i) != null && !this.getLines().get(i).isEmpty()) {
                Holograms.getSaves().setValue(this.getName() + "-line-" + i, this.getLines().get(i));
            }
        }
        List<String> holograms = new ArrayList<>(Holograms.list());
        if (!holograms.contains(this.getName())) {
            holograms.add(this.getName());
        }
        Holograms.getSaves().setValue("holograms", String.join(", ", holograms));
        Holograms.save(this);
    }

    public void delete() {
        Holograms.getSaves().removeValue(this.getName() + "-darkness");
        Holograms.getSaves().removeValue(this.getName() + "-line-distance");
        Holograms.getSaves().removeValue(this.getName() + "-world");
        Holograms.getSaves().removeValue(this.getName() + "-x-position");
        Holograms.getSaves().removeValue(this.getName() + "-y-position");
        Holograms.getSaves().removeValue(this.getName() + "-z-position");
        Holograms.getSaves().removeValue(this.getName() + "-lines");
        for (int i = 0; i < this.getLines().size(); i++) {
            Holograms.getSaves().removeValue(this.getName() + "-line-" + i);
        }
        List<String> holograms = new ArrayList<>(Holograms.list());
        holograms.remove(this.getName());
        if (!holograms.isEmpty()) {
            Holograms.getSaves().setValue("holograms", String.join(", ", holograms));
        } else {
            Holograms.getSaves().removeValue("holograms");
        }
        unloadAll();
        Holograms.delete(this);
    }

    public void teleport(Location location, TNLPlayer player) {
        Holograms.teleport(this, location, player);
    }

    public void teleportAll(Location location) {
        Holograms.teleportAll(this, location);
    }

    public void teleportAll(double offsetX, double offsetY, double offsetZ) {
        Holograms.teleportAll(this, offsetX, offsetY, offsetZ);
    }

    public void teleport(double offsetX, double offsetY, double offsetZ, TNLPlayer player) {
        Holograms.teleport(this, offsetX, offsetY, offsetZ, player);
    }

    public void load(TNLPlayer player) {
        Holograms.load(this, player);
    }

    public void loadAll() {
        Holograms.loadAll(this);
    }

    public void unload(TNLPlayer player) {
        Holograms.unload(this, player);
    }

    public void unloadAll() {
        Holograms.unloadAll(this);
    }

    public void reloadAll() {
        Holograms.reloadAll(this);
    }

    @Override
    public String toString() {
        return "Hologram{" +
                "name='" + name + '\'' +
                ", lines=" + lines +
                ", location=" + location +
                ", lineDistance=" + lineDistance +
                ", darkness=" + darkness +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hologram hologram = (Hologram) o;
        return Double.compare(hologram.lineDistance, lineDistance) == 0 && darkness == hologram.darkness && name.equals(hologram.name) && lines.equals(hologram.lines) && Objects.equals(location, hologram.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lines, location, lineDistance, darkness);
    }
}

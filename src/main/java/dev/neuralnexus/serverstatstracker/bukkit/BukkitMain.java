package dev.neuralnexus.serverstatstracker.bukkit;

import dev.neuralnexus.serverstatstracker.DataAccumulator;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.neuralnexus.serverstatstracker.Utils.isFolia;
import static dev.neuralnexus.serverstatstracker.bukkit.BukkitUtils.getDataAccumulator;
import static dev.neuralnexus.serverstatstracker.bukkit.BukkitUtils.getNMSVersion;

public final class BukkitMain extends JavaPlugin {
    // Config

    // Singleton instance
    private static BukkitMain instance;
    public static BukkitMain getInstance() {
        return instance;
    }

    public static boolean FOLIA = isFolia();

    public static boolean ENABLED = true;

    public static void repeatTaskAsync(Plugin plugin, Runnable run, Long delay, Long period) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTaskTimer(plugin, run, delay, period);
            return;
        }
        ForkJoinPool.commonPool().submit(() -> {
            try {
                Thread.sleep(delay*1000/20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    Thread.sleep(period*1000/20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                run.run();
            }
        });
    }

    @Override
    public void onEnable() {
        // Initialize the singleton instance
        instance = this;

        // Folia check
        if (FOLIA) {
            getLogger().info("Folia detected, using our own scheduler");
        } else {
            getLogger().info("Using the Bukkit scheduler");
        }

        getLogger().info("Attempting compatibility for version: " + getNMSVersion());

        // Initialize the data accumulator
        Server server = Bukkit.getServer();
        DataAccumulator<Server> da = getDataAccumulator();
        da.init("plugins");

        AtomicInteger retries = new AtomicInteger();
        repeatTaskAsync(this, () -> {
            try {
               if (!ENABLED) return;
               da.sendData(server);
            }
            catch(Exception e) {
                if (retries.getAndIncrement() > 10) {
                    getLogger().info("Could not connect to the ServerStatsTracker backend, please contact NeuralNexus");
                    ENABLED = false;
                }
            }
        }, 20L * 10L, 20L*10L);

        // Plugin enable message
        getLogger().info("SystemStatsTracker has been enabled!");

    }

    @Override
    public void onDisable() {
        // Plugin disable message
        getLogger().info("SystemStatsTracker has been disabled!");
    }
}

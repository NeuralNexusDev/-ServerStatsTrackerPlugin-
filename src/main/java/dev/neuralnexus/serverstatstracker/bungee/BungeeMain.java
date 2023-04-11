package dev.neuralnexus.serverstatstracker.bungee;

import dev.neuralnexus.serverstatstracker.DataAccumulator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BungeeMain extends Plugin {
    public static boolean ENABLED = true;

    @Override
    public void onEnable() {
        getLogger().info("Using the BungeeCord scheduler");

        // Initialize the data accumulator
        ProxyServer server = ProxyServer.getInstance();
        DataAccumulator<ProxyServer> da = new BungeeDataAccumulator();

        getLogger().info("Attempting compatibility for version: " + da.getServerVersion());

        da.init("plugins");

        AtomicInteger retries = new AtomicInteger();
        getProxy().getScheduler().schedule(this, () -> {
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
        }, 10L, 10L, TimeUnit.SECONDS);

        // Plugin enable message
        getLogger().info("SystemStatsTracker has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin disable message
        getLogger().info("SystemStatsTracker has been disabled!");
    }
}

package dev.neuralnexus.serverstatstracker.fabric;

import dev.neuralnexus.serverstatstracker.DataAccumulator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

public class FabricMain implements ModInitializer {
    // Logger
    public static final Logger logger = LoggerFactory.getLogger("serverstatstracker");

    public static boolean ENABLED = true;

    // Async task
    public static void repeatTaskAsync(Runnable run, Long delay, Long period) {
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
    public void onInitialize() {
        // Get the server instance
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // Initialize the data accumulator
            DataAccumulator<MinecraftServer> da = new FabricDataAccumulator(server);

            logger.info("[SystemStatsTracker]: Attempting compatibility for version: " + da.getServerVersion());

            AtomicInteger retries = new AtomicInteger();
            repeatTaskAsync(() -> {
                try {
                    if (!ENABLED) return;
                    da.sendData(server);
                }
                catch(Exception e) {
                    if (retries.getAndIncrement() > 10) {
                        logger.info("[SystemStatsTracker]: Could not connect to the ServerStatsTracker backend, please contact NeuralNexus");
                        ENABLED = false;
                    }
                }
            }, 20L * 10L, 20L*10L);
        });

        // Mod enable message
        logger.info("[SystemStatsTracker]: SystemStatsTracker has been enabled!");
    }
}

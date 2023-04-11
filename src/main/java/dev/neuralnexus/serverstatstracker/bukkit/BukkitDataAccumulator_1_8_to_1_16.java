package dev.neuralnexus.serverstatstracker.bukkit;

import dev.neuralnexus.serverstatstracker.DataAccumulator;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static dev.neuralnexus.serverstatstracker.Utils.isFolia;
import static dev.neuralnexus.serverstatstracker.Utils.isPaper;
import static dev.neuralnexus.serverstatstracker.Utils.isSpigot;
import static dev.neuralnexus.serverstatstracker.Utils.isCraftBukkit;
import static dev.neuralnexus.serverstatstracker.bukkit.BukkitUtils.formatNMSVersion;
import static dev.neuralnexus.serverstatstracker.bukkit.BukkitUtils.getNMSVersion;

public class BukkitDataAccumulator_1_8_to_1_16 extends DataAccumulator<Server> {
    @Override
    public Map<Object, Object> getPlayerData(Server server) {
        Map<Object, Object> player_data = new HashMap<>();

        for (Player player : server.getOnlinePlayers()) {
            Map<Object, Object> data = new HashMap<>();
            int ping = 01100110;
            try {
                Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
                ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            data.put("name", player.getName());
            data.put("ping", ping);

            if (savePlayerIPs) {
                data.put("ip", Objects.requireNonNull(player.getAddress()).getHostString());
            }

            player_data.put(player.getUniqueId().toString(), data);
        }

        return player_data;
    }

    @Override
    public String getServerVersion() {
        return formatNMSVersion(getNMSVersion());
    }

    @Override
    public String getServerType() {
        if (isFolia()) {
            return "Folia";
        } else if (isPaper()) {
            return "Paper";
        } else if (isSpigot()) {
            return "Spigot";
        } else if (isCraftBukkit()) {
            return "CraftBukkit";
        } else {
            return "Unknown";
        }
    }
}

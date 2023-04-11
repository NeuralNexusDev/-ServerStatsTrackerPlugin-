package dev.neuralnexus.serverstatstracker.fabric;

import dev.neuralnexus.serverstatstracker.DataAccumulator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class FabricDataAccumulator extends DataAccumulator<MinecraftServer> {
    FabricDataAccumulator(MinecraftServer server) {
        super();
        this.init("config");
        this.version = server.getVersion();
    }
    @Override
    public Map<Object, Object> getPlayerData(MinecraftServer server) {
        Map<Object, Object> player_data = new HashMap<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Map<Object, Object> data = new HashMap<>();
            data.put("name", player.getEntityName());
            data.put("ping", player.pingMilliseconds);

            if (savePlayerIPs) {
                data.put("ip", player.getIp());
            }

            player_data.put(player.getUuidAsString(), data);
        }
        return player_data;
    }

    @Override
    public String getServerVersion() {
        return this.version;
    }

    @Override
    public String getServerType() {
        // TODO: Add support for Cardboard
        return "Fabric";
    }
}

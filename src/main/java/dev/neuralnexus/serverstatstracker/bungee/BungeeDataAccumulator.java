package dev.neuralnexus.serverstatstracker.bungee;

import dev.neuralnexus.serverstatstracker.DataAccumulator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;

public class BungeeDataAccumulator extends DataAccumulator<ProxyServer> {
    @Override
    public Map<Object, Object> getPlayerData(ProxyServer server) {
        Map<Object, Object> player_data = new HashMap<>();

        for (ProxiedPlayer player : server.getPlayers()) {
            Map<Object, Object> data = new HashMap<>();
            data.put("name", player.getName());
            data.put("ping", player.getPing());

            if (savePlayerIPs) {
                data.put("ip", player.getAddress().getAddress().getHostAddress());
            }

            player_data.put(player.getUniqueId().toString(), data);
        }

        return player_data;
    }

    @Override
    public String getServerVersion() {
        if (ProxyServer.getInstance().getGameVersion().contains(", ")) {
            String[] versions = ProxyServer.getInstance().getGameVersion().split(", ");
            if (versions.length == 1) return versions[0];
            return versions[0] + "-" + versions[versions.length - 1];
        } else return ProxyServer.getInstance().getGameVersion();
    }

    @Override
    public String getServerType() {
        // TODO: Add support for Waterfall
        // TODO: Add support for Travertine
        // TODO: Add support for HexaCord
        return "BungeeCord";
    }
}

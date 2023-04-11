package dev.neuralnexus.serverstatstracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.dejvokep.boostedyaml.YamlDocument;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static dev.neuralnexus.serverstatstracker.Utils.hasSpark;

public abstract class DataAccumulator<T> {
    // Stats config
    private static String endpointURL;
    private static String serverName;
    private static String serverUUID;
    protected static boolean savePlayerIPs = false;

    // Alert config
    private static boolean webhookEnabled = false;
    private static String webhookURL;
    private static String webhookUsername;
    private static String webhookAvatarURL;
    private static int pingThreshold;
    private static boolean pingAlertsEnabled = false;
    private static int memoryThreshold;
    private static boolean memoryAlertsEnabled = false;
    private static int tpsThreshold;
    private static boolean tpsAlertsEnabled = false;
    private static int cpuThreshold;
    private static boolean cpuAlertsEnabled = false;

    // Alert Cooldowns
    private static int pingCooldown = 0;
    private static int memoryCooldown = 0;
    private static int tpsCooldown = 0;
    private static int cpuCooldown = 0;

    private final String type = getServerType();
    protected String version = getServerVersion();
    private static final boolean SPARK = hasSpark();

    public void init(String configPath) {
        // Config
        try {
            YamlDocument config = YamlDocument.create(new File("./" + configPath + "/ServerStatsTracker", "config.yml"),
                    Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("config.yml"))
            );
            config.reload();

            // Get config values
            endpointURL = config.getString("stats.endpointURL");
            serverName = config.getString("stats.serverName");
            serverUUID = config.getString("stats.serverUUID");
            savePlayerIPs = config.getBoolean("stats.savePlayerIPs");

            webhookEnabled = config.getBoolean("alerts.webhookEnabled");
            if (webhookEnabled) {
                webhookURL = config.getString("alerts.webhookURL");
                webhookUsername = config.getString("alerts.webhookUsername");
                webhookAvatarURL = config.getString("alerts.webhookAvatarURL");

                pingThreshold = config.getInt("alerts.pingThreshold");
                pingAlertsEnabled = config.getBoolean("alerts.pingAlertsEnabled");

                memoryThreshold = config.getInt("alerts.memoryThreshold");
                memoryAlertsEnabled = config.getBoolean("alerts.memoryAlertsEnabled");

                tpsThreshold = config.getInt("alerts.tpsThreshold");
                tpsAlertsEnabled = config.getBoolean("alerts.tpsAlertsEnabled");

                cpuThreshold = config.getInt("alerts.cpuThreshold");
                cpuAlertsEnabled = config.getBoolean("alerts.cpuAlertsEnabled");
            }

            if (serverUUID.equals("00000000-0000-0000-0000-000000000000") || serverUUID.equals("null") || serverUUID.equals("")) {
                serverUUID = UUID.randomUUID().toString();
                config.set("stats.serverUUID", serverUUID);
                config.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Embed Class
    private static class Embed {
        String title;
        String description;
        int color;

        public Map<Object, Object> GetEmbedMap() {
            Map<Object, Object> embedMap = new HashMap<>();
            embedMap.put("title", title);
            embedMap.put("description", description);
            embedMap.put("color", color);
            return embedMap;
        }

        public Embed(String title, String description, int color) {
            this.title = title;
            this.description = description;
            this.color = color;
        }
    }

    // Send Webhook
    public void sendWebhook(Embed embed) {
        Map<Object, Object> data = new HashMap<>();
        data.put("username", webhookUsername);
        if (!Objects.equals(webhookAvatarURL, "")) {
            data.put("avatar_url", webhookAvatarURL);
        }
        data.put("content", null);
        data.put("embeds", new Object[]{embed.GetEmbedMap()});
        data.put("attachments", new Object[]{});
        APICall(webhookURL, data);
    }

    // Get player data
    public Map<Object, Object> getPlayerData(T server) {
        return null;
    }

    // Get server version
    public String getServerVersion() {
        return null;
    }

    // Get server type
    public String getServerType() {
        return null;
    }

    public Map<Object, Object> getServerData() {
        // Server data
        Map<Object, Object> serverMap = new HashMap<>();

        // Get CPU and TPS data if spark is installed
        if (SPARK) {
            serverMap = SparkModule.getServerInfo();

            // TPS alert
            if (webhookEnabled && tpsAlertsEnabled && serverMap.get("tpsLast5Mins") != null) {
                double tps = (double) serverMap.get("tpsLast5Mins");
                if (tpsCooldown > 0) {
                    tpsCooldown--;
                } else if (tps <= tpsThreshold) {
                    sendWebhook(new Embed(serverName + " TPS Alert:", "The Server's TPS is at " + tps, 12521231));
                    tpsCooldown = 6;
                }
            }

            // CPU usage alert
            if (webhookEnabled && cpuAlertsEnabled && serverMap.get("cpuLast1Min") != null) {
                double cpu = (double) serverMap.get("cpuLast1Min");
                if (cpuCooldown > 0) {
                    cpuCooldown--;
                } else if (cpu*100 >= cpuThreshold) {
                    sendWebhook(new Embed(serverName + " CPU Alert:", "The Server's CPU usage is at " + cpu*100 + "%", 12521231));
                    cpuCooldown = 6;
                }
            }
        }

        // Get memory data
        double memUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1048576.0f;
        double memMaxMB = Runtime.getRuntime().maxMemory()/1048576.0f;
        int memUsedPercent = (int) (memUsedMB/memMaxMB*100);

        // Memory usage alert
        if (webhookEnabled && memoryAlertsEnabled) {
            if (memoryCooldown > 0) {
                memoryCooldown--;
            } else if (memUsedPercent >= memoryThreshold) {
                sendWebhook(new Embed(serverName + " Memory Alert:", "The Server's RAM usage is at " + memUsedPercent + "%", 15126834));
                memoryCooldown = 6;
            }
        }

        serverMap.put("memUsedMB", memUsedMB);
        serverMap.put("memMaxMB", memMaxMB);

        // Get server version and server type
        if (version != null) {
            serverMap.put("version", version);
        }
        if (type != null) {
            serverMap.put("type", type);
        }
        if (serverName != null) {
            serverMap.put("name", serverName);
        }

        return serverMap;
    }

    public Map sendData(T server) {
        // Get player data
        Map<Object, Object> playerMap = getPlayerData(server);

        // Ping alert
        if (webhookEnabled && pingAlertsEnabled) {
            for (Object player : playerMap.keySet()) {
                Map<Object, Object> playerData = (Map<Object, Object>) playerMap.get(player);
                if (playerData.get("ping") != null) {
                    int ping = (int) playerData.get("ping");
                    if (pingCooldown > 0) {
                        pingCooldown--;
                    } else if (ping >= pingThreshold) {
                        sendWebhook(new Embed(serverName + " Ping Alert:", "Player " + playerData.get("name") + "'s ping is at " + ping, 15126834));
                        pingCooldown = 6;
                    }
                }
            }
        }

        // Get server data
        Map<Object, Object> serverMap = getServerData();

        // Send data
        Map<Object, Object> data = new HashMap<>();
        data.put("uuid", serverUUID);
        data.put("server", serverMap);
        data.put("players", playerMap);

        return APICall(endpointURL, data);
    }

    // API Call
    private static Map APICall(String dataSource, Map<Object, Object> data) {
        try {
            Gson gson = new GsonBuilder().create();
            String data_json = gson.toJson(data);

            URL url = new URL(dataSource);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-Agent", "NeuralNexus ServerStatsTracker");
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(data_json);
            osw.flush();
            osw.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            return gson.fromJson(br.readLine(), Map.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
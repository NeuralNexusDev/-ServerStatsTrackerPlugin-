package dev.neuralnexus.serverstatstracker.bukkit;

import dev.neuralnexus.serverstatstracker.DataAccumulator;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class BukkitUtils {
    // Get NMS version (NMS = net.minecraft.server)
    public static String getNMSVersion(){
        String v = Bukkit.getServer().getClass().getPackage().getName();
        return v.substring(v.lastIndexOf('.') + 1);
    }

    // Format NMS version to readable version
    public static String formatNMSVersion(String nms) {
        switch (nms) {
            case "v_1_7_R1":
                return "1.7.2";
            case "v_1_7_R2":
                return "1.7.5";
            case "v_1_7_R3":
                return "1.7.8";
            case "v_1_7_R4":
                return "1.7.10";
            case "v1_8_R1":
                return "1.8";
            case "v1_8_R2":
                return "1.8.3";
            case "v1_8_R3":
                return "1.8.8";
            case "v1_9_R1":
                return "1.9.2";
            case "v1_9_R2":
                return "1.9.4";
            case "v1_10_R1":
                return "1.10.2";
            case "v1_11_R1":
                return "1.11.2";
            case "v1_12_R1":
                return "1.12.2";
            case "v1_13_R1":
                return "1.13";
            case "v1_13_R2":
                return "1.13.2";
            case "v1_14_R1":
                return "1.14.4";
            case "v1_15_R1":
                return "1.15.2";
            case "v1_16_R1":
                return "1.16.1";
            case "v1_16_R2":
                return "1.16.3";
            case "v1_16_R3":
                return "1.16.5";
            case "v1_17_R1":
                return "1.17.1";
            case "v1_18_R1":
                return "1.18.1";
            case "v1_18_R2":
                return "1.18.2";
            case "v1_19_R1":
                return "1.19.2";
            case "v1_19_R2":
                return "1.19.3";
            case "v1_19_R3":
                return "1.19.4";
            default:
                return nms;
        }
    }

    // Get DataAccumulator class for the server version
    public static DataAccumulator<Server> getDataAccumulator() {
        DataAccumulator<Server> da = null;
        String nms = getNMSVersion();
        try {
            String packageName = BukkitMain.class.getPackage().getName();
            String da_class = "";
            switch (nms) {
                case "v1_8_R1":
                case "v1_8_R2":
                case "v1_8_R3":
                case "v1_9_R1":
                case "v1_9_R2":
                case "v1_10_R1":
                case "v1_11_R1":
                case "v1_12_R1":
                case "v1_13_R1":
                case "v1_13_R2":
                case "v1_14_R1":
                case "v1_15_R1":
                case "v1_16_R1":
                case "v1_16_R2":
                    da_class = "BukkitDataAccumulator_1_8_to_1_16";
                    break;
                case "v1_16_R3":
                case "v1_17_R1":
                case "v1_18_R1":
                case "v1_18_R2":
                case "v1_19_R1":
                case "v1_19_R2":
                case "v1_19_R3":
                    da_class = "BukkitDataAccumulator_1_17_to_1_19";
                    break;
            }
            da = (DataAccumulator<Server>) Class.forName(packageName + "." + da_class).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException exception) {
            BukkitMain.getInstance().getLogger().info("ServerStatsTracker could not find a valid implementation for this server version: " + nms);
        }
        return da;
    }
}

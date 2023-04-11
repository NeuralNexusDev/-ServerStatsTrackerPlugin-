package dev.neuralnexus.serverstatstracker;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;

import java.util.HashMap;
import java.util.Map;

public class SparkModule {
    static double roundOff(double f) {
        return Math.round(f * 100.0) / 100.0;
    }
    static Map<Object, Object> getServerInfo() {
        Map<Object, Object> server_info = new HashMap<>();

        Spark spark = SparkProvider.get();

        // Get TPS
        DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
        if (tps != null) {
            double tpsLast10Secs = roundOff(tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10));
            double tpsLast5Mins = roundOff(tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5));
            double tpsLast15Mins = roundOff(tps.poll(StatisticWindow.TicksPerSecond.MINUTES_15));

            server_info.put("tpsLast10Secs", tpsLast10Secs);
            server_info.put("tpsLast5Mins", tpsLast5Mins);
            server_info.put("tpsLast15Mins", tpsLast15Mins);
        }

        // Get MSPT
        GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = spark.mspt();
        if (mspt != null) {
            DoubleAverageInfo msptLastMin = mspt.poll(StatisticWindow.MillisPerTick.MINUTES_1);
            double msptMean = roundOff(msptLastMin.mean());
            double mspt95Percentile = roundOff(msptLastMin.percentile95th());

            server_info.put("msptMean", msptMean);
            server_info.put("mspt95Percentile", mspt95Percentile);
        }

        // Get CPU
        DoubleStatistic<StatisticWindow.CpuUsage> cpuUsage = spark.cpuProcess();
        double cpuLast10Secs = roundOff(cpuUsage.poll(StatisticWindow.CpuUsage.SECONDS_10));
        double cpuLast1Min = roundOff(cpuUsage.poll(StatisticWindow.CpuUsage.MINUTES_1));
        double cpuLast15Mins = roundOff(cpuUsage.poll(StatisticWindow.CpuUsage.MINUTES_15));

        server_info.put("cpuLast10Secs", cpuLast10Secs);
        server_info.put("cpuLast1Min", cpuLast1Min);
        server_info.put("cpuLast15Mins", cpuLast15Mins);

        return server_info;
    }
}

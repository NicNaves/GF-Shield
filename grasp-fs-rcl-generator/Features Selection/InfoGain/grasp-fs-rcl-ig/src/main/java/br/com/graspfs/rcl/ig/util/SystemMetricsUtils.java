package br.com.graspfs.rcl.ig.util;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

public class SystemMetricsUtils {

    private static final SystemInfo systemInfo = new SystemInfo();
    private static final CentralProcessor processor = systemInfo.getHardware().getProcessor();
    private static final GlobalMemory memory = systemInfo.getHardware().getMemory();

    public static float getCpuUsage() {
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(100); // breve espera para calcular corretamente a média
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        return (float) (cpuLoad * 100);
    }

    public static float getMemoryUsageMB() {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        return used / (1024f * 1024f); // bytes para MB
    }

    public static float getMemoryTotalMB() {
        return memory.getTotal() / (1024f * 1024f);
    }

    public static float getMemoryUsagePercent() {
        return getMemoryUsageMB() * 100 / getMemoryTotalMB();
    }

    public static class MetricsCollector implements Runnable {

        private volatile boolean running = true;
        private int samples = 0;
        private float totalCpu = 0;
        private float totalMemory = 0;
        private float totalMemoryPercent = 0;
    
        public void run() {
            while (running) {
                totalCpu += SystemMetricsUtils.getCpuUsage();
                totalMemory += SystemMetricsUtils.getMemoryUsageMB();
                totalMemoryPercent += SystemMetricsUtils.getMemoryUsagePercent();
                samples++;
                try {
                    Thread.sleep(100); // coleta a cada 100ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    
        public void stop() {
            running = false;
        }
    
        public float getAvgCpu() {
            return samples == 0 ? 0 : totalCpu / samples;
        }
    
        public float getAvgMemory() {
            return samples == 0 ? 0 : totalMemory / samples;
        }
    
        public float getAvgMemoryPercent() {
            return samples == 0 ? 0 : totalMemoryPercent / samples;
        }
    }

}


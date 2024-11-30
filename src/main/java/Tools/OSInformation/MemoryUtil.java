package Tools.OSInformation;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;


public class MemoryUtil {
    public static String convertSize(long sizeInBytes) {
        double size = sizeInBytes;
        String[] units = {"bytes", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size = size / 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    public static Map<String, Object> getMemoryInfo() {
        Map<String, Object> memoryInfo = new HashMap<>();
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=Memory");
            CompositeDataSupport compositeDataSupport = (CompositeDataSupport) mbs.getAttribute(name, "HeapMemoryUsage");
            long heapMemoryUsed = (Long) compositeDataSupport.get("used");
            compositeDataSupport = (CompositeDataSupport) mbs.getAttribute(name, "HeapMemoryUsage");
            long heapMemoryMax = (Long) compositeDataSupport.get("max");
            double heapUsage = (double) heapMemoryUsed / heapMemoryMax * 100;
            DecimalFormat df = new DecimalFormat("#.##");
            memoryInfo.put("heapMemoryUsed", convertSize(heapMemoryUsed));
            memoryInfo.put("heapMemoryMax", convertSize(heapMemoryMax));
            memoryInfo.put("heapUsage", df.format(heapUsage) + "%");
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
        return memoryInfo;
    }
}
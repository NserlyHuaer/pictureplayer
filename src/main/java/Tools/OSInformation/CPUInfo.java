package Tools.OSInformation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CPUInfo {
    public static String getCpuInfo() {
        String os = System.getProperty("os.name").toLowerCase();
        String cpuInfo = null;
        try {
            if (os.contains("win")) {
                ProcessBuilder pb = new ProcessBuilder("wmic", "cpu", "get", "name");
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                boolean skipFirst = true;
                while ((line = reader.readLine()) != null) {
                    if (skipFirst) {
                        skipFirst = false;
                        continue;
                    }
                    cpuInfo = (line.trim());
                    break;
                }
                reader.close();
                process.destroy();
            } else if (os.contains("linux")) {
                ProcessBuilder pb = new ProcessBuilder("cat", "/proc/cpuinfo");
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                boolean found = false;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("model name")) {
                        cpuInfo = (line.split(":")[1].trim());
                        found = true;
                        break;
                    }
                }
                reader.close();
                process.destroy();
            } else {
                System.out.println("Unsupported operating system for CPU information retrieval.");
            }
        } catch (IOException e) {
            System.out.println("Error occurred while retrieving CPU info:" + e.getMessage());
        }
        return cpuInfo;
    }
}

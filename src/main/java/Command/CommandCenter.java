package Command;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class CommandCenter {
    public static final int FOR_UPDATE = 0;
    private static final String TEMP_DIR = "./cache/"; // 临时目录
    private static final String BACKUP_NAME = "backup.jar";
    private static String CURRENT_JAR_NAME; // 你的实际JAR文件名

    static {
        ClassLoader classLoader = CommandCenter.class.getClassLoader();
        URL url = classLoader.getResource(CommandCenter.class.getName().replace('.', '/') + ".class");
        CURRENT_JAR_NAME = url.getPath().substring(5, url.getPath().lastIndexOf("!"));
    }

    public CommandCenter(int runnable) {
        switch (runnable) {
            case FOR_UPDATE -> {
                backupCurrentVersion();
            }
        }
    }

    public static void backupCurrentVersion() {
        try {
            Path currentJarPath = Paths.get(CURRENT_JAR_NAME);
            File f = new File(CURRENT_JAR_NAME);
            if (f.exists()) f.delete();
            Files.copy(currentJarPath, Paths.get(TEMP_DIR, BACKUP_NAME));
            System.out.println("Current version backed up.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to create backup.");
        }
    }

    public static void replace(String DownloadFilePath) throws IOException {
        // 假设updateFile()是一个自定义方法，负责从网络或其他来源获取新版jar文件
        Path newJarPath = Paths.get(DownloadFilePath);
        Path oldJarPath = Paths.get(CURRENT_JAR_NAME);
        Files.move(newJarPath, oldJarPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Updated to new version.");
    }


    public static void restart() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String name = runtimeMXBean.getName();
        int pid = Integer.parseInt(name.split("@")[0]);

        try {
            String javaCmd = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String classPath = System.getProperty("java.class.path");
            List<String> cmd = Arrays.asList(javaCmd, "-classpath", classPath, CommandCenter.class.getCanonicalName(), Integer.toString(pid));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true); // 合并标准输出和错误输出
            pb.inheritIO(); // 输入输出流继承自父进程

            Process process = pb.start();
            process.waitFor();

            System.out.println("Restarted successfully.");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to restart.");
            System.exit(1);
        }
    }
}
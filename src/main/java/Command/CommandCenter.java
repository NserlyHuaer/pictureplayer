package Command;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;

public class CommandCenter {
    public static final int FOR_UPDATE = 0;
    private static String CURRENT_JAR_PATH; // 当前JAR文件路径
    private static String CURRENT_JAR_NAME;//当前JAR文件路径

    static {
        ClassLoader classLoader = CommandCenter.class.getClassLoader();
        URL url = classLoader.getResource(CommandCenter.class.getName().replace('.', '/') + ".class");
        CURRENT_JAR_PATH = url.getPath().substring(5, url.getPath().lastIndexOf("!"));
        CURRENT_JAR_NAME = CURRENT_JAR_PATH.substring(CURRENT_JAR_PATH.lastIndexOf("/") + 1);
    }


    public static void moveFileToDirectory(String DownloadFilePath) throws IOException {
        Path sourcePath = Path.of(DownloadFilePath);
        Path destinationPath = Path.of("./" + DownloadFilePath.substring(DownloadFilePath.lastIndexOf("/") + 1));
        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File moved successfully.");
    }

    public static String detectOSType() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "windows";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return "linux";
        } else {
            return "unknown";
        }
    }

    public static void executeOSSpecificCommands(String osType, String DownloadFilePath) {
        switch (osType) {
            case "windows":
                try {
                    createAndRunWindowsBatchFile(DownloadFilePath);
                } catch (IOException e) {
                    System.out.println("Error:" + e);
                }
                break;
            case "linux":
                try {
                    createAndRunLinuxShellScript(DownloadFilePath);
                } catch (IOException e) {
                    System.out.println("Error:" + e);
                }
                break;
            default:
                System.out.println("Unsupported OS:\"" + osType + "\"");
        }
    }

    public static void createAndRunWindowsBatchFile(String DownloadFilePath) throws IOException {
        String batchContent = "timeout /t 1\n"
                + "del .\\" + CURRENT_JAR_NAME + "\r\n"
                + "ren " + DownloadFilePath.substring(DownloadFilePath.lastIndexOf("/") + 1) + " " + CURRENT_JAR_NAME + "\n"
                + "java -jar " + CURRENT_JAR_NAME;
        Path batchPath = Path.of("./replace.bat");
        Files.write(batchPath, batchContent.getBytes());
        batchContent = "start replace.bat";
        batchPath = Path.of("./runnable.bat");
        Files.write(batchPath, batchContent.getBytes());
        Runtime.getRuntime().exec("runnable.bat");
        System.exit(0);
    }

    public static void createAndRunLinuxShellScript(String DownloadFilePath) throws IOException {
        String shellContent =
                "sleep 1\n"
                        + "rm " + CURRENT_JAR_NAME + "\n"
                        + "mv " + DownloadFilePath.substring(DownloadFilePath.lastIndexOf("/") + 1) + " " + CURRENT_JAR_NAME + "\n"
                        + "java -jar " + CURRENT_JAR_NAME;
        Path shellPath = Path.of("./replace.sh");
        Files.write(shellPath, shellContent.getBytes());
        Files.setPosixFilePermissions(shellPath, PosixFilePermissions.fromString("rwx------"));
        Runtime.getRuntime().exec("nohup sh ./replace.sh &");
        System.exit(0);
    }


    /*public static void replace(String DownloadFilePath) throws IOException {
        // 假设updateFile()是一个自定义方法，负责从网络或其他来源获取新版jar文件
        System.out.println(DownloadFilePath);
        Path newJarPath = Paths.get(DownloadFilePath);
        Path oldJarPath = Paths.get(CURRENT_JAR_PATH);
        Files.move(newJarPath, oldJarPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Updated to new version.");
    }*/


    /*private static void restart() {
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
    }*/
}
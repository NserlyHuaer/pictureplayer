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
        String batchContent = "echo @off\n" +
                "timeout /t 1\n"
                + "del .\\" + CURRENT_JAR_NAME + "\r\n"
                + "ren " + DownloadFilePath.substring(DownloadFilePath.lastIndexOf("/") + 1) + " " + CURRENT_JAR_NAME + "\n" +
                "cls\n"
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

}
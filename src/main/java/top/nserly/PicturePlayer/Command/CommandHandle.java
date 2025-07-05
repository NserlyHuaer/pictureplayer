package top.nserly.PicturePlayer.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.nserly.PicturePlayer.Version.DownloadChecker.CheckAndDownloadUpdate;
import top.nserly.SoftwareCollections_API.String.RandomString;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;

public class CommandHandle {
    private static final String CURRENT_JAR_PATH; // 当前JAR文件路径
    private static final String CURRENT_JAR_NAME;//当前JAR文件路径
    private static final Logger logger = LoggerFactory.getLogger(CommandHandle.class);

    private static final String MainFileSuffix;

    static {
        ClassLoader classLoader = CommandHandle.class.getClassLoader();
        URL url = classLoader.getResource(CommandHandle.class.getName().replace('.', '/') + ".class");
        CURRENT_JAR_PATH = url.getPath().substring(5, url.getPath().lastIndexOf("!"));
        CURRENT_JAR_NAME = CURRENT_JAR_PATH.substring(CURRENT_JAR_PATH.lastIndexOf("/") + 1);
        MainFileSuffix = RandomString.getRandomString(5);
    }


    public static void moveFileToDirectory(String DownloadMainFilePath) throws IOException {
        DownloadMainFilePath = DownloadMainFilePath.replace("\\", "/");
        Path sourcePath = Path.of(DownloadMainFilePath);
        Path destinationPath = Path.of("./" + DownloadMainFilePath.substring(DownloadMainFilePath.lastIndexOf("/") + 1) + MainFileSuffix);
        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Main File moved successfully.");
    }

    public static void moveFileToLibDirectory(ArrayList<String> DownloadLibFilePath) throws IOException {
        for (String string : DownloadLibFilePath) {
            string = string.replace("\\", "/");
            String fileName = new File(string).getName();
            int LastIndex1 = fileName.lastIndexOf("-");
            if (LastIndex1 == -1) {
                LastIndex1 = fileName.lastIndexOf(".jar");
            }
            String currentHandlerDependencyName = fileName.substring(0, LastIndex1);
            for (String DependencyName : CheckAndDownloadUpdate.DependenciesName) {
                if (DependencyName.contains(currentHandlerDependencyName)) {
                    String deleteFilePath = CheckAndDownloadUpdate.DependenciesFilePath.get(DependencyName);
                    new File(deleteFilePath).delete();
                }
            }

            Path sourcePath = Path.of(string);
            Path destinationPath = Path.of("./lib/" + string.substring(string.lastIndexOf("/") + 1));
            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        }
        logger.info("Lib File moved successfully.");
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

    public static void executeOSSpecificCommands(String osType, String DownloadFilePath, String OpenedPicturePath) {
        switch (osType) {
            case "windows":
                try {
                    createAndRunWindowsBatchFile(DownloadFilePath, OpenedPicturePath);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    logger.error("Automatic update failed");
                }
                break;
            case "linux":
                try {
                    createAndRunLinuxShellScript(DownloadFilePath, OpenedPicturePath);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    logger.error("Automatic update failed");
                }
                break;
            default:
                logger.error("Unsupported OS:\"{}\"", osType);
        }
    }

    public static void executeOSSpecificCommands(String osType, String DownloadFilePath) {
        executeOSSpecificCommands(osType, DownloadFilePath, null);
    }

    public static void createAndRunWindowsBatchFile(String DownloadFilePath) throws IOException {
        createAndRunWindowsBatchFile(DownloadFilePath, null);
    }

    public static void createAndRunWindowsBatchFile(String DownloadFilePath, String OpenedPicturePath) throws IOException {
        String batchContent =
                "echo @off\n" +
                        "timeout /t 3\n"
                        + "del \".\\" + CURRENT_JAR_NAME + "\"\n"
                        + "ren \"" + DownloadFilePath.substring(DownloadFilePath.lastIndexOf("/") + 1) + MainFileSuffix + "\" \"" + CURRENT_JAR_NAME + "\"\n" +
                        "cls\n"
                        + "\"" + System.getProperty("sun.boot.library.path") + "\\java.exe\" -cp \"" + CURRENT_JAR_NAME + ";lib\\*\" top.nserly.GUIStarter -Dsun.java2d.opengl=true ";
        if (OpenedPicturePath != null && !OpenedPicturePath.isBlank()) {
            batchContent = batchContent + "\"" + OpenedPicturePath + "\"";
        }

        Path batchPath = Path.of("./replace.bat");
        Files.write(batchPath, batchContent.getBytes(StandardCharsets.US_ASCII));

        batchContent = "start replace.bat";
        batchPath = Path.of("./runnable.bat");
        Files.write(batchPath, batchContent.getBytes());

        logger.info("The script file is created!");
        logger.info("Start running the script file and end the current software...");

        Runtime.getRuntime().exec(new String[]{"runnable.bat"});
        logger.info("Program Termination!");

        System.exit(0);
    }

    public static void createAndRunLinuxShellScript(String DownloadFilePath) throws IOException {
        createAndRunLinuxShellScript(DownloadFilePath, null);
    }

    public static void createAndRunLinuxShellScript(String DownloadFilePath, String OpenedPicturePath) throws IOException {
        String shellContent =
                "sleep 1\n"
                        + "rm " + CURRENT_JAR_NAME + "\n"
                        + "mv " + DownloadFilePath.substring(DownloadFilePath.lastIndexOf("/") + 1) + MainFileSuffix + " " + CURRENT_JAR_NAME + "\n"
                        + "\"" + System.getProperty("sun.boot.library.path") + "\\java.exe\" -cp \"" + CURRENT_JAR_NAME + ";lib\\*\" top.nserly.GUIStarter -Dsun.java2d.opengl=true ";
        if (OpenedPicturePath != null && !OpenedPicturePath.isBlank()) {
            shellContent = shellContent + OpenedPicturePath;
        }
        Path shellPath = Path.of("./replace.sh");
        Files.write(shellPath, shellContent.getBytes());
        Files.setPosixFilePermissions(shellPath, PosixFilePermissions.fromString("rwx------"));
        logger.info("The script file is created");
        logger.info("Start running the script file and end the current software...");
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "nohup sh ./replace.sh &"});
        logger.info("Program Termination!");
        System.exit(0);
    }

}
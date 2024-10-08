package Dev;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class BufferedImageRotator {
    //key:hash值前或后25位(windows后，linux前，自带后) value:文件路径
    private static HashMap<String, String> hashMap;


    static {
        hashMap = new HashMap<>();
    }

    public String obtainStoragePath(String originalImagePath) {
        String hashcode = getSupportHashCode(originalImagePath);
        if (hashMap.containsKey(hashcode)) {
            return hashMap.get(hashcode);
        }
        String time = String.valueOf(System.currentTimeMillis());
        File file = new File("cache/" + hashcode.substring(0, 6) + time.substring(time.length() - 5) + ".temp");
        hashMap.put(hashcode, file.getPath());
        return file.getPath();
    }


    public static String getSupportHashCode(String originalImagePath) {
        String osName = System.getProperty("os.name");
        String command;
        //根据操作系统选择命令
        if (osName.contains("win")) {
            command = "powershell -Command \"(Get-FileHash " + originalImagePath + " -Algorithm SHA256).Hash\"";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            command = "shasum -a 256 " + originalImagePath;
        } else {
            System.out.println("Error:Unsupported OS:" + osName);
            System.out.println("program will be acquired using the program that comes with the software!");
            StringBuilder stringBuilder = new StringBuilder(Objects.requireNonNull(getHashCode(originalImagePath)));
            if (stringBuilder.length() < 25) {
                for (int i = 0; i < 25 - stringBuilder.length(); i++) {
                    stringBuilder.append("3");
                }
            }
            return stringBuilder.substring(stringBuilder.length() - 25);
        }
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder hexCode = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                hexCode.append(line);
            }
            return hexCode.substring(0, 26);
        } catch (IOException e) {
            System.out.println("Error:The system call failed");
            StringBuilder stringBuilder = new StringBuilder(Objects.requireNonNull(getHashCode(originalImagePath)));
            if (stringBuilder.length() < 25) {
                for (int i = 0; i < 25 - stringBuilder.length(); i++) {
                    stringBuilder.append("3");
                }
            }
            return stringBuilder.substring(stringBuilder.length() - 25);
        }


    }

    private static String getHashCode(String originalImagePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream inputStream = new FileInputStream(originalImagePath)) {
                byte[] buffer = new byte[8551325];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
                byte[] hash = digest.digest();
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                }
                return hexString.toString();
            } catch (IOException ignored) {

            }
        } catch (NoSuchAlgorithmException ignored) {

        }
        return null;
    }

    public void close() {

    }

    public static String getImageFormatName(String path) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(path));
            Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(new File(path));
            if (imageReaderIterator.hasNext()) {
                ImageReader imageReader = imageReaderIterator.next();
                //获取图片格式名称
                return imageReader.getFormatName();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

package Tools.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileContents {
    private static final Logger logger = LoggerFactory.getLogger(FileContents.class);

    public static String read(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[102400];
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            while (true) {
                int index = bufferedReader.read(buffer);
                if (index == -1)
                    return stringBuilder.toString();
                stringBuilder.append(new String(buffer, 0, index));
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}

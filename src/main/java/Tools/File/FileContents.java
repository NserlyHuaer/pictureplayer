package Tools.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileContents {
    private static final Logger logger = LoggerFactory.getLogger(FileContents.class);
    public static String read(String path) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            char[] buffer = new char[102400];
            int complete = bufferedReader.read(buffer);
            return new String(buffer, 0, complete);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}

package Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileCenter {
    private final File file;
    private static final Logger logger = LoggerFactory.getLogger(FileCenter.class);

    public FileCenter(String FileName) {
        file = new File("./cache/" + FileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public void Write(String data) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(data);
            bufferedWriter.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public String Read() {
        int size = 0;
        char[] b = new char[10240000];
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            size = bufferedReader.read(b);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return b.toString();
    }
}

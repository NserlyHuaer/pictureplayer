package Tools.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class ReverseSearch {
    public static String get(String fileName, String searchString) {
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "r");
            long length = raf.length();
            long pos = length - 1;
            boolean found = false;
            String lastMatchingLine = "";
            while (pos >= 0) {
                raf.seek(pos);
                char c = (char) raf.readByte();
                if (c == '\n') {
                    long start = pos + 1;
                    raf.seek(start);
                    byte[] buffer = new byte[(int) (length - start)];
                    raf.readFully(buffer);
                    String line = new String(buffer, StandardCharsets.UTF_8);
                    if (!line.isEmpty()) {
                        if (line.startsWith(searchString)) {
                            lastMatchingLine = line;
                            found = true;
                            break;
                        }
                    } else {
                        // 如果是空行，继续往前找
                        pos--;
                        continue;
                    }
                }
                pos--;
            }
            raf.close();
            if (found) {
                return lastMatchingLine;
            } else {
                System.out.println("Error:Couldn't find specific string");
            }
        } catch (IOException e) {
            System.out.println("Error:" + e);
        }
        return null;
    }
}
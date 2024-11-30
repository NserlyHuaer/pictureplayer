package Tools.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileContents {
    public static String read(String path) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            char[] buffer = new char[102400];
            int complete = bufferedReader.read(buffer);
            return new String(buffer, 0, complete);
        } catch (IOException e) {
            System.out.println("Error:" + e);
        }
        return null;
    }
}

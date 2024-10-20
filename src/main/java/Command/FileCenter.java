package Command;

import java.io.*;

public class FileCenter {
    private final File file;

    public FileCenter(String FileName) {
        file = new File("./cache/" + FileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error:" + e);
            }
        }
    }

    public void Write(String data) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(data);
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Error" + e);
        }
    }

    public String Read() {
        int size = 0;
        char[] b = new char[10240000];
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            size = bufferedReader.read(b);
        } catch (IOException e) {
            System.out.println("Error" + e);
        }
        return b.toString();
    }
}

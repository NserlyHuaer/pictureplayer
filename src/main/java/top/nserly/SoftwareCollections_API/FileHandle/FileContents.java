package top.nserly.SoftwareCollections_API.FileHandle;

import lombok.extern.slf4j.Slf4j;
import top.nserly.SoftwareCollections_API.Handler.Exception.ExceptionHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
public class FileContents {

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
            log.error(ExceptionHandler.getExceptionMessage(e));
        }
        return null;
    }
}

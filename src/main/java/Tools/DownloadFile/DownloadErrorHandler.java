package Tools.DownloadFile;

import java.io.IOException;

public interface DownloadErrorHandler {
    void handler(IOException ioException, FileDownloader fileDownloader);
}

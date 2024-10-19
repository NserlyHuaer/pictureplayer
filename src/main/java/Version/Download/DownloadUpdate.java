package Version.Download;


import java.io.File;
import java.io.IOException;

public class DownloadUpdate {
    File f;
    String webSide;

    public DownloadUpdate(String DownloadPath, String webSide) {
        this.webSide = webSide;
        f = new File(DownloadPath);
        if (!(f.exists() && f.isDirectory())) setDefaultDownloadPath();
    }

    public DownloadUpdate(String webSide) {
        this.webSide = webSide;
        setDefaultDownloadPath();
    }

    public void setDefaultDownloadPath() {
        f = new File("download\\");
    }

    //检查是否存在最新版本
    public boolean checkIfTheLatestVersion() throws IOException {
        DownloadFile downloadFile = new DownloadFile(webSide, f.getPath());
        downloadFile.startToDownload();

        return false;
    }
}

package Version.Download;


import Tools.String.Formation;
import Version.Version;

import java.io.*;
import java.util.List;

public class DownloadUpdate {
    File f;
    String webSide;
    private static String downloadWebSide;

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
        f = new File("./download/");
    }

    //检查是否存在最新版本
    public boolean checkIfTheLatestVersion() throws IOException {
        DownloadFile downloadFile = new DownloadFile(webSide, f.getPath());
        downloadFile.startToDownload();

        File path = new File(downloadFile.getSaveDir());
        String lastLine = "";
        try (BufferedReader br = new BufferedReader(new FileReader(path.getPath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                lastLine = line;
            }
        }
        Formation formation = new Formation(lastLine);
        List<String> list = formation.getArray();
        if (Long.parseLong(list.getFirst()) <= Long.parseLong(Version.getVersionID())) {
            path.delete();
            return false;
        }
        downloadWebSide = list.get(1);
        path.delete();
        return true;
    }
}

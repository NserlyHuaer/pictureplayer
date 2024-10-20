package Version.Download;


import Tools.String.Formation;
import Version.Version;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadUpdate {
    File f;
    String webSide;
    private static List<String> downloadWebSide;
    public int TotalDownloadingFile;
    public int HaveDownloadedFile;
    public DownloadFile CurrentDownloadingFile;
    private final static long startTime = System.currentTimeMillis();
    public List<String> FilePath = new ArrayList<>();

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
        f = new File("./download/" + startTime + "/");
    }

    //更新最新版本
    public List<String> getUpdateWebSide() {
        if (!(downloadWebSide != null && !downloadWebSide.isEmpty())) {
            try {
                checkIfTheLatestVersion();
            } catch (IOException e) {
                System.out.println("Error:" + e);
            }
        }
        return downloadWebSide;
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
        list.remove(0);
        downloadWebSide = list;
        path.delete();
        return true;
    }

    //一键下载所有文件(Map(下载网站,List[0]:文件存放路径;[1]下载类))[调用此方法时，推进使用新线程，否则窗体可能会无相应]
    public Map<String, List> download(List<String> downloadWebSide) {
        Map<String, List> finalA = new HashMap<>();
        if (downloadWebSide == null) return null;
        int index = 0;
        TotalDownloadingFile = downloadWebSide.size();
        for (String down : downloadWebSide) {
            FilePath.add(down);
            HaveDownloadedFile = index;
            try {
                CurrentDownloadingFile = new DownloadFile(down, f.getPath());
                CurrentDownloadingFile.startToDownload();
                List list = new ArrayList();
                String cache = CurrentDownloadingFile.getSaveDir();
                if (cache != null) {
                    list.add(cache);
                    list.add(CurrentDownloadingFile);
                    finalA.put(down, list);
                }
            } catch (IOException e) {
                System.out.println("Error:" + e);
            }
            index++;
        }
        return finalA;
    }

}

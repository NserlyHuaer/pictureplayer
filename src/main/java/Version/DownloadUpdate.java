package Version;


import Loading.Bundle;
import Tools.DownloadFile.FileDownloader;
import Tools.File.FileContents;
import Exception.UpdateException;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.swing.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class DownloadUpdate {
    File f;
    @Setter
    String webSide;
    public List<String> downloadFileWebSite;

    public String MainFileWebSite;

    public ArrayList<String> DependenciesWebSite;
    public int TotalDownloadingFile;
    public int HaveDownloadedFile;
    public FileDownloader CurrentFileDownloader;
    private final static long startTime = System.currentTimeMillis();
    public List<String> FilePath = new ArrayList<>();
    //启用安全连接模式
    private static boolean EnableSecureConnection = true;
    public long NewVersionID = 0;
    public String NewVersionName = "";
    public VersionID versionID;
    // 定义选项内容
    private Object[] options = {Bundle.getMessage("DownloadUpdateOptions_1st"), Bundle.getMessage("DownloadUpdateOptions_2nd"), Bundle.getMessage("DownloadUpdateOptions_3rd")};
    private boolean StopToUpdate;
    private static final Logger logger = LoggerFactory.getLogger(DownloadUpdate.class);

    private static boolean isChecked = false;

    public static final ArrayList<String> DependenciesName = new ArrayList<>();


    static {
        File[] files = new File("lib").listFiles();
        if (files != null)
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.endsWith(".jar")) {
                    int LastIndex1 = fileName.lastIndexOf("-");
                    if (LastIndex1 == -1) {
                        LastIndex1 = fileName.lastIndexOf(".jar");
                    }
                    DependenciesName.add(fileName.substring(0, LastIndex1));
                }
            }
    }

    public DownloadUpdate(String DownloadPath, String webSide) {
        this.webSide = webSide;
        f = new File(DownloadPath);
        if (f.exists() || f.isDirectory()) setDefaultDownloadPath();
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
        StopToUpdate = false;
        if (!isChecked) {
            try {
                return checkIfTheLatestVersion() ? downloadFileWebSite : null;
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return downloadFileWebSite;
    }

    //检查是否存在最新版本
    public boolean checkIfTheLatestVersion() throws IOException {
        StopToUpdate = false;
        isChecked = true;
        logger.info("Checking version...");
        FileDownloader fileDownloader = new FileDownloader(webSide, f.getPath());
        fileDownloader.startDownload();

        versionID = VersionID.gson.fromJson(FileContents.read(fileDownloader.getFinalPath()), VersionID.class);
        new File(fileDownloader.getFinalPath()).delete();
        if (versionID != null) {
            NewVersionID = Long.parseLong(VersionID.getString(versionID.getNormalVersionID(), versionID.getSpecialFields()));
        }
        if (versionID != null) {
            NewVersionName = VersionID.getString(versionID.getNormalVersion(), versionID.getSpecialFields());
        }

        MainFileWebSite = VersionID.getString(versionID.getNormalVersionMainFile(), versionID.getSpecialFields());
        DependenciesWebSite = new ArrayList<>();
        TreeMap<String, String> cache = versionID.getNormalDependencies();
        if (cache != null)
            for (String key : cache.keySet()) {
                String dependenciesName = VersionID.getString(key, versionID.getSpecialFields());
                if (!DependenciesName.contains(dependenciesName))
                    DependenciesWebSite.add(VersionID.getString(cache.get(key), versionID.getSpecialFields()));
            }
        downloadFileWebSite = (ArrayList<String>) DependenciesWebSite.clone();
        downloadFileWebSite.add(MainFileWebSite);

        if (NewVersionID <= Long.parseLong(Version.getVersionID())) {
            return false;
        }
        logger.info("Discover a new version");
        return true;
    }

    //强制获取更新
    public void ForceToGetUpdates() throws IOException {
        if (!isChecked) {
            checkIfTheLatestVersion();
        }
    }

    //一键下载所有文件(Map(Key:下载网站,Value:List[0]:文件存放路径;[1]下载类))[调用此方法时，推进使用新线程，否则窗体可能会无相应]
    public Map<String, ArrayList> download(List<String> downloadWebSide) {
        StopToUpdate = false;
        Map<String, ArrayList> finalA = new HashMap<>();
        if (downloadWebSide == null) return null;
        int index = 0;
        TotalDownloadingFile = downloadWebSide.size();

        for (String down : downloadWebSide) {
            FilePath.add(down);
            HaveDownloadedFile = index;
            switch (download(down, finalA, false)) {
                case 1 -> {
                    TotalDownloadingFile--;
                    continue;
                }
                case 2 -> {
                    return null;
                }
            }
            index++;
        }
        if (finalA.isEmpty()) return null;
        logger.info("Download completed!");
        return finalA;
    }

    //返回值：0.下载完成 1.跳过当前文件 2.取消下载
    private int download(String down, Map<String, ArrayList> finalA, boolean isTry) {
        try {
            if (StopToUpdate) {
                throw new UpdateException("Update ended,cause of User terminated software update");
            }
            if (!isTry) CurrentFileDownloader = new FileDownloader(down, f.getPath());
            CurrentFileDownloader.setDownloadErrorHandler((e, fileDownloader) -> {
                if (ExceptionHandling(e) == 0) {
                    download(down, finalA, true);
                } else {
                    CurrentFileDownloader.stopDownload();
                }
            });
            logger.info("Downloading " + down);
            if (!EnableSecureConnection) logger.warn("The connection is not secure from {}!", down);
            CurrentFileDownloader.startDownload();
            ArrayList list = new ArrayList();
            String cache = CurrentFileDownloader.getFinalPath();
            if (StopToUpdate) {
                throw new UpdateException("Update ended,cause of User terminated software update");
            }
            if (cache != null) {
                list.add(cache);
                list.add(CurrentFileDownloader);
                finalA.put(down, list);
            }
        } catch (UpdateException e) {
            logger.error(e.getMessage());
            return 2;
        }
        if (CurrentFileDownloader.isCompleted())
            return 0;
        else
            return 1;

    }

    private int ExceptionHandling(IOException e) {
        logger.error(e.getMessage());
        int choice = JOptionPane.showOptionDialog(null, Bundle.getMessage("DownloadUpdateError_Content") + "\n" + e, Bundle.getMessage("DownloadUpdateError_Title"), JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options);
        return choice;
    }

    //一键下载所有文件(Map(Key:下载网站,Value:List[0]:文件存放路径;[1]下载类))[调用此方法时，推进使用新线程，否则窗体可能会无相应]
    public Map<String, ArrayList> download() {
        StopToUpdate = false;
        return download(getUpdateWebSide());
    }

    //下载描述文件List[0]:文件存放路径;[1]下载类[调用此方法时，推进使用新线程，否则窗体可能会无相应]
    public List downloadDescribe() {
        logger.info("Start downloading describe version file...");
        StopToUpdate = false;
        String DescribeFileWebSide = VersionID.getString(versionID.getNormalVersionDescribe(), versionID.getSpecialFields());
        if (!(DescribeFileWebSide == null) && !DescribeFileWebSide.isEmpty()) {
            return download(Collections.singletonList(DescribeFileWebSide)).get(DescribeFileWebSide);
        }
        return null;
    }

    //终止更新
    public void stopToUpdate() {
        StopToUpdate = true;
        CurrentFileDownloader.stopDownload();
    }

    public static boolean isEnableSecureConnection() {
        return EnableSecureConnection;
    }

    public static void SecureConnection(boolean Enable) {
        if (EnableSecureConnection == Enable) {
            if (!Enable) {
                //创建一个自定义的TrustManager，它接受所有证书
                TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }};
                try {
                    //初始化SSLConText
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustManagers, new SecureRandom());
                    //获取默认的SSLSocketFactory,并设置为信任所有证书
                    SSLSocketFactory ssf = sc.getSocketFactory();
                    HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    logger.error(e.getMessage());
                    return;
                }
                logger.warn("SSL validation is turned off and your computer may be vulnerable!");
            } else {
                try {
                    //重新创建默认的SSLContext
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, null, null);
                    //设置默认的SSLSocketFactory
                    SSLSocketFactory ssf = sc.getSocketFactory();
                    HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    logger.error(e.getMessage());
                    return;
                }
                logger.info("SSL validation is enabled");
            }
        }
        EnableSecureConnection = Enable;
    }
}

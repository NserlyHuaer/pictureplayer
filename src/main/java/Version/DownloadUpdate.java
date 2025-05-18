package Version;


import Loading.Bundle;
import Tools.DownloadFile.DownloadFile;
import Tools.DownloadFile.FileDownloader;
import Tools.File.ReverseSearch;
import Tools.String.Formation;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadUpdate {
    File f;
    @Setter
    String webSide;
    public List<String> downloadFileWebSide;
    public int TotalDownloadingFile;
    public int HaveDownloadedFile;
    public FileDownloader CurrentFileDownloader;
    private final static long startTime = System.currentTimeMillis();
    public List<String> FilePath = new ArrayList<>();
    //启用安全连接模式
    private static boolean EnableSecureConnection = true;
    public long NewVersionID = 0;
    public String NewVersionName = "";
    public String DescribeFileWebSide = "";
    // 定义选项内容
    private Object[] options = {Bundle.getMessage("DownloadUpdateOptions_1st"), Bundle.getMessage("DownloadUpdateOptions_2nd"), Bundle.getMessage("DownloadUpdateOptions_3rd")};
    private boolean StopToUpdate;
    private static final Logger logger = LoggerFactory.getLogger(DownloadUpdate.class);

    private List<String> LastCheckForDownloadFileWebSide;

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
        if (!(downloadFileWebSide != null && !downloadFileWebSide.isEmpty())) {
            try {
                checkIfTheLatestVersion();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return downloadFileWebSide;
    }

    //检查是否存在最新版本
    public boolean checkIfTheLatestVersion() throws IOException {
        StopToUpdate = false;
        logger.info("Checking version...");
        DownloadFile downloadFile = new DownloadFile(webSide, f.getPath());
        downloadFile.startToDownload();

        File path = new File(downloadFile.getSavePath());
        String lastLine = null;
        lastLine = ReverseSearch.get(path.getPath(), "DDD");
        logger.info("Version Configuration file is downloaded");
        if (lastLine == null) {
            logger.error("You can't query the latest version from the version profile");
            return false;
        }
        lastLine = lastLine.substring(3);
        Formation formation = new Formation(lastLine);
        List<String> list = formation.getArray();
        String version = list.getFirst();
        NewVersionID = Long.parseLong(version.substring(0, version.indexOf("/")));
        NewVersionName = version.substring(version.indexOf("/") + 1);
        logger.info("The latest version in the version configuration file:{}({})", NewVersionName, NewVersionID);
        //判断描述文件是否存在，如果存在就在下载列表中删除
        Pattern pattern = Pattern.compile("\\*DESCRIBE\\*\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(lastLine);
        if (matcher.find()) {
            logger.info("Describe Version File was found");
            DescribeFileWebSide = matcher.group(1);
            //描述文件不下载
            list.remove(DescribeFileWebSide);
        }
        path.delete();
        list.removeFirst();
        LastCheckForDownloadFileWebSide = list;
        if (NewVersionID <= Long.parseLong(Version.getVersionID())) {
            return false;
        }
        downloadFileWebSide = list;
        logger.info("Discover a new version");
        return true;
    }

    //强制获取更新
    public void ForceToGetUpdates() throws IOException {
        if (LastCheckForDownloadFileWebSide == null) {
            checkIfTheLatestVersion();
        }
        downloadFileWebSide = LastCheckForDownloadFileWebSide;
    }

    //一键下载所有文件(Map(Key:下载网站,Value:List[0]:文件存放路径;[1]下载类))[调用此方法时，推进使用新线程，否则窗体可能会无相应]
    public Map<String, List> download(List<String> downloadWebSide) {
        StopToUpdate = false;
        Map<String, List> finalA = new HashMap<>();
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
    private int download(String down, Map<String, List> finalA, boolean isTry) {
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
            List list = new ArrayList();
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
    public Map<String, List> download() {
        StopToUpdate = false;
        return download(getUpdateWebSide());
    }

    //下载描述文件List[0]:文件存放路径;[1]下载类[调用此方法时，推进使用新线程，否则窗体可能会无相应]
    public List downloadDescribe() {
        logger.info("Start downloading describe version file...");
        StopToUpdate = false;
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

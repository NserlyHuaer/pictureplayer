package Version;


import Tools.DownloadFile.DownloadFile;
import Tools.File.ReverseSearch;
import Tools.String.Formation;
import Exception.UpdateException;
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
    String webSide;
    public List<String> downloadFileWebSide;
    public int TotalDownloadingFile;
    public int HaveDownloadedFile;
    public DownloadFile CurrentDownloadingFile;
    private final static long startTime = System.currentTimeMillis();
    public List<String> FilePath = new ArrayList<>();
    //启用安全连接模式
    private static boolean EnableSecureConnection = true;
    public long NewVersionID = 0;
    public String NewVersionName = "";
    public String DescribeFileWebSide = "";
    // 定义选项内容
    private Object[] options = {"重试", "跳过当前文件", "退出"};
    private boolean StopToUpdate;
    private static final Logger logger = LoggerFactory.getLogger(DownloadUpdate.class);

    public DownloadUpdate(String DownloadPath, String webSide) {
        this.webSide = webSide;
        f = new File(DownloadPath);
        if (f.exists() || f.isDirectory()) setDefaultDownloadPath();
    }

    public DownloadUpdate(String webSide) {
        this.webSide = webSide;
        setDefaultDownloadPath();
    }

    public void setWebSide(String webSide) {
        this.webSide = webSide;
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
        DownloadFile downloadFile = new DownloadFile(webSide, f.getPath());
        logger.info("Checking version...");
        downloadFile.startToDownload();

        File path = new File(downloadFile.getSaveDir());
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
        if (NewVersionID <= Long.parseLong(Version.getVersionID())) {
            path.delete();
            return false;
        }
        list.removeFirst();
        downloadFileWebSide = list;
        path.delete();
        logger.info("Discover a new version");
        return true;
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
            if (!isTry) CurrentDownloadingFile = new DownloadFile(down, f.getPath());
            logger.info("Downloading " + down);
            if (!EnableSecureConnection) logger.warn("The connection is not secure from {}!", down);
            if (isTry) CurrentDownloadingFile.retryToDownload();
            else CurrentDownloadingFile.startToDownload();
            List list = new ArrayList();
            String cache = CurrentDownloadingFile.getSaveDir();
            if (StopToUpdate) {
                throw new UpdateException("Update ended,cause of User terminated software update");
            }
            if (cache != null) {
                list.add(cache);
                list.add(CurrentDownloadingFile);
                finalA.put(down, list);
            }
        } catch (IOException e) {
            switch (ExceptionHandling(e)) {
                case 0 -> {
                    return download(down, finalA, true);
                }
                case 1 -> {
                    return 1;
                }
                case 2 -> {
                    return 2;
                }
            }
        } catch (UpdateException e) {
            logger.error(e.getMessage());
            return 2;
        }
        return 0;

    }

    private int ExceptionHandling(IOException e) {
        logger.error(e.getMessage());
        int choice = JOptionPane.showOptionDialog(null, "在下载过程中出现了错误：\n" + e, "下载失败", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options);
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
        CurrentDownloadingFile.stopToDownload();
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

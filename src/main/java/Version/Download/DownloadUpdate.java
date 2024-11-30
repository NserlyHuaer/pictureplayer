package Version.Download;


import Tools.File.ReverseSearch;
import Tools.String.Formation;
import Version.Version;

import javax.net.ssl.*;
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
        if (!(downloadFileWebSide != null && !downloadFileWebSide.isEmpty())) {
            try {
                checkIfTheLatestVersion();
            } catch (IOException e) {
                System.out.println("Error:" + e);
            }
        }
        return downloadFileWebSide;
    }

    //检查是否存在最新版本
    public boolean checkIfTheLatestVersion() throws IOException {
        DownloadFile downloadFile = new DownloadFile(webSide, f.getPath());
        System.out.println("Checking version...");
        downloadFile.startToDownload();

        File path = new File(downloadFile.getSaveDir());
        String lastLine = null;
        lastLine = ReverseSearch.get(path.getPath(), "DDD");
        if (lastLine == null)
            return false;
        lastLine = lastLine.substring(3);
        Formation formation = new Formation(lastLine);
        List<String> list = formation.getArray();
        String version = list.getFirst();
        NewVersionID = Long.parseLong(version.substring(0, version.indexOf("/")));
        NewVersionName = version.substring(version.indexOf("/") + 1);
        //判断猫叔文件是否存在，如果存在就在下载列表中删除
        Pattern pattern = Pattern.compile("\\*DESCRIBE\\*\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(lastLine);
        if (matcher.find()) {
            DescribeFileWebSide = matcher.group(1);
            //描述文件不下载
            list.remove(DescribeFileWebSide);
        }
        if (NewVersionID <= Long.parseLong(Version.getVersionID())) {
            path.delete();
            return false;
        }
        list.remove(0);
        downloadFileWebSide = list;
        path.delete();
        return true;
    }

    //一键下载所有文件(Map(Key:下载网站,Value:List[0]:文件存放路径;[1]下载类))[调用此方法时，推进使用新线程，否则窗体可能会无相应]
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
                System.out.println("Downloading " + down);
                if (!EnableSecureConnection)
                    System.out.println("Waring:The connection is not secure from " + down + "!");
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
        System.out.println("Download completed!");
        return finalA;
    }

    //一键下载所有文件(Map(Key:下载网站,Value:List[0]:文件存放路径;[1]下载类))[调用此方法时，推进使用新线程，否则窗体可能会无相应]
    public Map<String, List> download() {
        return download(getUpdateWebSide());
    }

    //下载描述文件List[0]:文件存放路径;[1]下载类[调用此方法时，推进使用新线程，否则窗体可能会无相应]
    public List downloadDescribe() {
        if (!(DescribeFileWebSide == null) && !DescribeFileWebSide.isEmpty()) {
            return download(Collections.singletonList(DescribeFileWebSide)).get(DescribeFileWebSide);
        }
        return null;
    }

    public static boolean isEnableSecureConnection() {
        return EnableSecureConnection;
    }

    public static void SecureConnection(boolean Enable) {
        if (EnableSecureConnection == Enable) {
            if (!Enable) {
                //创建一个自定义的TrustManager，它接受所有证书
                TrustManager[] trustManagers = new TrustManager[]{
                        new X509TrustManager() {
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
                        }
                };
                try {
                    //初始化SSLConText
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustManagers, new SecureRandom());
                    //获取默认的SSLSocketFactory,并设置为信任所有证书
                    SSLSocketFactory ssf = sc.getSocketFactory();
                    HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    System.out.println("Error:" + e);
                    return;
                }
                System.out.println("Waring:SSL validation is turned off and your computer may be vulnerable!");
            } else {
                try {
                    //重新创建默认的SSLContext
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, null, null);
                    //设置默认的SSLSocketFactory
                    SSLSocketFactory ssf = sc.getSocketFactory();
                    HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    System.out.println("Error:" + e);
                    return;
                }
                System.out.println("SSL validation is enabled");
            }
        }
        EnableSecureConnection = Enable;
    }
}

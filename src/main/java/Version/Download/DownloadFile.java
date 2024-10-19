package Version.Download;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class DownloadFile {
    private static final int BUFFER_SIZE = 4096;//增加缓冲区大小
    String fileURL;
    String saveDir;
    Thread download;
    //当前下载文件总大小
    long totalBytesRead;
    //文件总大小
    long fileSize;
    //下载进度
    double progress;

    public DownloadFile(String fileURL, String saveDir) throws IOException {
        this.fileURL = fileURL;
        File f = new File(saveDir);
        if (f.isDirectory()) {
            this.saveDir = f.getPath() + getFileName(fileURL);
            return;
        }
        this.saveDir = saveDir;
    }

    public static String getFileName(String fileURL) throws MalformedURLException {
        URL url = new URL(fileURL);
        String path = url.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getCurrentFinishedDownloadBytesRead() {
        return totalBytesRead;
    }

    public double getDownloadProgress() {
        return progress;
    }

    public void startToDownload() {
        download = new Thread(() -> {
            try {
                URL url = new URL(fileURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                //设置读取超时
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(10000);
                int responseCode = 0;

                responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream(), BUFFER_SIZE);
                    FileOutputStream outputStream = new FileOutputStream(saveDir);
                    byte[] dataBuffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    totalBytesRead = 0;
                    fileSize = httpURLConnection.getContentLengthLong();//获取文件总大小
                    while ((bytesRead = inputStream.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
                        outputStream.write(dataBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        //计算下载进度
                        progress = (double) totalBytesRead / fileSize * 100;
                    }
                    outputStream.close();
                    inputStream.close();
                } else {
                    System.out.println("Error:Invalid HTTP response code:" + responseCode);
                }
                httpURLConnection.disconnect();
            } catch (IOException e) {
                System.out.println("Error:Request failed");
            }

        });
        download.start();

    }
}

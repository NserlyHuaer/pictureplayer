package Version.Download;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DownloadFile {
    private static final int BUFFER_SIZE = 4096;//增加缓冲区大小
    String fileURL;
    String saveDir;
    Thread download;
    //当前下载文件总大小
    public long totalBytesRead;
    //文件总大小
    public long fileSize;
    //下载进度
    public double progress;
    //每秒传输比特量（bits/s）
    public double bps;
    private long lastUpdate;
    //下载是否完成
    public boolean isCompleted;

    public DownloadFile(String fileURL, String saveDir) throws IOException {
        this.fileURL = fileURL;
        this.saveDir = saveDir;
        File f = new File(saveDir);
        if (!f.exists()) {
            f.mkdir();
        }

    }

    //返回保存路径
    public String getSaveDir() {
        return saveDir;
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
        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //设置读取超时
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(10000);
            int responseCode = 0;
            String disposition = httpURLConnection.getHeaderField("Content-Disposition");
            String fileName = null;
            if (disposition != null && disposition.startsWith("attachment")) {
                String tokens[] = disposition.split(";");
                for (String token : tokens) {
                    if (token.trim().startsWith("filename")) {
                        String fileName1 = token.substring(10);
                        fileName = new String(fileName1.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        break;
                    }
                }
            }
            if (fileName != null) {
                //如果Content-Disposition中有文件名，则使用它
                saveDir = saveDir + "/" + fileName;
            } else {
                saveDir = saveDir + "/" + fileURL.substring(fileURL.lastIndexOf("/") + 1);
            }

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream(), BUFFER_SIZE);
                FileOutputStream outputStream = new FileOutputStream(saveDir);
                byte[] dataBuffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long startTime = System.currentTimeMillis();
                totalBytesRead = 0;
                for (int i = 0; i < 5; i++) {
                    fileSize = httpURLConnection.getContentLengthLong();//获取文件总大小
                    if (fileSize != -1) {
                        break;
                    }
                    Thread.sleep(50);
                }
                if (fileSize == -1)
                    System.out.println("Error:Failed to get file size");


                while ((bytesRead = inputStream.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
                    outputStream.write(dataBuffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    processThroughput(totalBytesRead, startTime);
                    //计算下载进度
                    progress = (double) totalBytesRead / fileSize * 100;
                }
                bps = 0;
                isCompleted = true;
                progress = 100;
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } else {
                System.out.println("Error:Invalid HTTP response code:" + responseCode);
            }
            httpURLConnection.disconnect();
        } catch (IOException e) {
            System.out.println("Error:Request failed");
        } catch (InterruptedException e) {
            System.out.println("Error:Thread interrupted");
        }
    }

    public void startToDownloadOnNewThread() {
        download = new Thread(() -> {
            startToDownload();
        });
        download.start();

    }

    private void processThroughput(long byteReceived, long startTime) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate >= 1000) {
            long elapsed = currentTime - startTime;
            bps = (double) byteReceived * 8 / (elapsed / 1000.0);
            lastUpdate = currentTime;
        }
    }
}

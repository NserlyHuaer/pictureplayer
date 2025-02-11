package Tools.DownloadFile;

import Runner.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DownloadFile {
    private static final int BUFFER_SIZE = 524288;//增加缓冲区大小（512KB缓冲区）
    //设置连接超时时间
    public static final int ConnectionTimeout = 5000;
    //设置读取超时时间
    public static final int ReadTimeout = 5000;
    String fileURL;
    String saveDir;
    Thread download;
    //当前下载文件总大小
    public long CurrentCompletedBytesRead;
    //文件总大小
    public long fileSize;
    //下载进度
    public double progress;
    //每秒传输比特量（bits/s）
    public double BytesPerSecond;
    //文件还需要多少分钟才能下完(s)
    public long NeedDownloadTime;
    private long lastUpdate;
    private long lastReceivedBytes;
    //下载是否完成
    public boolean isCompleted;
    //判断是否获取到文件大小
    public boolean isGettingFileSize;
    //响应的Content-Type头
    public String contentType;
    private static final Logger logger = LoggerFactory.getLogger(DownloadFile.class);
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
        return CurrentCompletedBytesRead;
    }

    public double getDownloadProgress() {
        return progress;
    }

    public void startToDownload() throws IOException {
        HttpURLConnection httpURLConnection = null;
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        IOException ioException = null;

        try {
            URL url = new URL(fileURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            //设置超时
            httpURLConnection.setReadTimeout(ReadTimeout);
            httpURLConnection.setConnectTimeout(ConnectionTimeout);
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
                String cache = fileURL;
                if (cache.contains("?")) cache = cache.substring(0, cache.indexOf("?"));
                saveDir = saveDir + "/" + cache.substring(fileURL.lastIndexOf("/") + 1);
            }
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            responseCode = httpURLConnection.getResponseCode();
            // 检查响应状态码是否为200（HTTP OK）
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = new BufferedInputStream(httpURLConnection.getInputStream(), BUFFER_SIZE);
                outputStream = new FileOutputStream(saveDir);
                byte[] dataBuffer = new byte[BUFFER_SIZE];
                int bytesRead;
                CurrentCompletedBytesRead = 0;
                //获取响应的Content-Type头
                contentType = httpURLConnection.getContentType();
                //获取文件总大小
                fileSize = httpURLConnection.getContentLengthLong();
                if (fileSize == -1) logger.error("Failed to get file size");
                else isGettingFileSize = true;


                while ((bytesRead = inputStream.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
                    outputStream.write(dataBuffer, 0, bytesRead);
                    CurrentCompletedBytesRead += bytesRead;
                    processThroughput();
                    //计算下载进度
                    progress = (double) CurrentCompletedBytesRead / fileSize * 100;
                }

            } else {
                logger.error("Invalid HTTP response code:{}", responseCode);
            }
            progress = 100;
        } catch (IOException e) {
            ioException = e;
        } finally {
            BytesPerSecond = 0;
            if (outputStream != null) {
                outputStream.flush();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        if (ioException != null) throw ioException;
        isCompleted = true;
    }

    public void retryToDownload() throws IOException {
        RandomAccessFile outputStream = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        IOException ioException = null;
        try {
            if (CurrentCompletedBytesRead <= 0 || fileSize == 0) {
                startToDownload();
                return;
            }
            File outputFile = new File(saveDir);
            connection = null;
            outputStream = null;

            URL url = new URL(fileURL);
            connection = (HttpURLConnection) url.openConnection();
            //设置超时
            connection.setReadTimeout(ReadTimeout);
            connection.setConnectTimeout(ConnectionTimeout);

            // 设置 Range 请求头
            connection.setRequestProperty("Range", "bytes=" + CurrentCompletedBytesRead + "-");
            connection.connect();

            // 检查服务器是否支持断点续传
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                logger.error("The server does not support sumable upload, and it will be re-downloaded!By Invalid HTTP response code:{}", responseCode);
                startToDownload();
                return;
            }

            // 以追加模式打开文件
            outputStream = new RandomAccessFile(outputFile, "rw");
            outputStream.seek(CurrentCompletedBytesRead); // 移动文件指针到末尾

            // 检查响应状态码是否为200（HTTP OK）
            inputStream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
            byte[] dataBuffer = new byte[BUFFER_SIZE];
            int bytesRead;
            //获取响应的Content-Type头
            contentType = connection.getContentType();
            //获取文件总大小
            fileSize = connection.getContentLengthLong();
            if (fileSize == -1) logger.error("Failed to get file size");
            else {
                isGettingFileSize = true;
                fileSize += CurrentCompletedBytesRead;
            }

            while ((bytesRead = inputStream.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
                outputStream.write(dataBuffer, 0, bytesRead);
                CurrentCompletedBytesRead += bytesRead;
                processThroughput();
                //计算下载进度
                progress = (double) CurrentCompletedBytesRead / fileSize * 100;
            }
            progress = 100;
        } catch (IOException e) {
            ioException = e;
        } finally {
            BytesPerSecond = 0;
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
            if (ioException != null) throw ioException;
            isCompleted = true;
        }
    }

    public void startToDownloadOnNewThread() {
        download = new Thread(() -> {
            try {
                startToDownload();
            } catch (IOException e) {
                logger.error("Request failed");
            }
        });
        download.start();

    }

    public void retryToDownloadOnNewThread() {
        download = new Thread(() -> {
            try {
                retryToDownload();
            } catch (IOException e) {
                logger.error("Request failed");
            }
        });
        download.start();

    }


    private void processThroughput() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate >= 300) {
            //计算下载速度
            long elapsed = currentTime - lastUpdate;
            BytesPerSecond = (CurrentCompletedBytesRead - lastReceivedBytes) * 1000.0 / elapsed;
            lastUpdate = currentTime;
            lastReceivedBytes = CurrentCompletedBytesRead;
            if (!isGettingFileSize) return;
            //计算预计下载完的时间
            NeedDownloadTime = (long) ((fileSize - CurrentCompletedBytesRead) / BytesPerSecond);
        }
    }

}

package Tools.DownloadFile;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 多线程文件下载器，支持断点续传、速度计算和智能重命名
 */
public class FileDownloader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloader.class);
    private int redirectCount = 0;
    private static final int MAX_REDIRECTS = 5;

    // 配置常量
    private static final int BUFFER_SIZE = 524_288; // 512KB缓冲区
    private static final int CONNECT_TIMEOUT = 5_000;
    private static final int READ_TIMEOUT = 5_000;
    private static final String TEMP_SUFFIX = ".download";
    private static final int PROGRESS_UPDATE_INTERVAL = 300; // 进度更新间隔(ms)

    // 下载状态
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final AtomicBoolean isCompleted = new AtomicBoolean(false);
    private final AtomicLong bytesRead = new AtomicLong(0);
    // 状态获取方法
    @Getter
    private volatile long fileSize = -1;
    private volatile double downloadProgress = 0;
    private volatile double bytesPerSecond = 0;
    private volatile long remainingTime = -1;

    // 文件信息
    private String sourceUrl;
    private final String saveDirectory;
    private String finalFileName;
    private String tempFilePath;

    // 性能跟踪
    private long lastUpdateTime;
    private long lastBytesRecord;

    //下载报错控制
    private DownloadErrorHandler downloadErrorHandler;

    public FileDownloader(String sourceUrl, String saveDirectory) {
        this(sourceUrl, saveDirectory, null);
    }

    public FileDownloader(String sourceUrl, String saveDirectory, DownloadErrorHandler downloadErrorHandler) {
        this.sourceUrl = sourceUrl;
        this.saveDirectory = normalizeDirectoryPath(saveDirectory);
        createSaveDirectory();
        this.downloadErrorHandler = downloadErrorHandler;
    }

    public void setDownloadErrorHandler(DownloadErrorHandler downloadErrorHandler) {
        this.downloadErrorHandler = downloadErrorHandler;
    }

    /**
     * 在新线程中启动下载任务
     */
    public void startDownloadInNewThread() {
        new Thread(this).start();
    }

    /**
     * 启动下载任务
     */
    public void startDownload() {
        run();
    }

    @Override
    public void run() {
        try {
            if (bytesRead.get() > 0) {
                resumeDownload();
            } else {
                startNewDownload();
            }
            completeDownload();
        } catch (IOException e) {
            if (downloadErrorHandler != null)
                downloadErrorHandler.handler(e, this);
            else
                handleDownloadError(e);
        } finally {
            cleanResources();
        }
    }

    /**
     * 初始化全新下载
     */
    private void startNewDownload() throws IOException {
        HttpURLConnection connection = createConnection(false);
        try {
            validateResponse(connection);  // 先验证响应

            // 如果发生重定向，这里会直接返回
            if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                    || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                return;
            }

            initializeFileInfo(connection);
            try (InputStream is = connection.getInputStream();
                 OutputStream os = new FileOutputStream(tempFilePath)) {
                downloadStream(is, os);
            }
        } finally {
            connection.disconnect();
        }
    }


    /**
     * 恢复下载（修正版）
     */
    private void resumeDownload() throws IOException {
        HttpURLConnection connection = createConnection(true);
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                logger.warn("Server doesn't support resume, restarting download");
                resetDownload();
                startNewDownload();
                return;
            }

            // 使用追加模式的FileOutputStream替代RandomAccessFile
            try (InputStream is = connection.getInputStream();
                 OutputStream os = new FileOutputStream(tempFilePath, true)) {
                downloadStream(is, os);
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 通用下载流处理
     */
    private void downloadStream(InputStream is, OutputStream os) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int readBytes;

            while ((readBytes = bis.read(buffer)) != -1) {
                if (isStopped.get()) {
                    logger.info("Download stopped by user");
                    return;
                }

                os.write(buffer, 0, readBytes);
                updateProgress(readBytes);
                calculateMetrics();
            }
        }
    }


    /**
     * 创建HTTP连接
     */
    private HttpURLConnection createConnection(boolean isResume) throws IOException {
        int retry = 0;
        while (retry++ < 3) {
            try {
                URL url = new URL(sourceUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);

                if (isResume) {
                    connection.setRequestProperty("Range", "bytes=" + bytesRead.get() + "-");
                }

                return connection;
            } catch (IOException e) {
                if (downloadErrorHandler != null)
                    downloadErrorHandler.handler(e, this);
                else {
                    if (retry == 3) throw e;
                    logger.warn("Connection failed, retrying... ({}/3)", retry);

                }
            }
        }
        throw new IOException("Failed to establish connection after 3 attempts");
    }

    /**
     * 初始化文件信息
     */
    private void initializeFileInfo(HttpURLConnection connection) throws IOException {
        // 获取文件名
        String fileName = getFileNameFromHeader(connection);
        if (fileName == null) {
            fileName = getFileNameFromUrl();
        }

        // 处理重复文件名
        this.finalFileName = generateUniqueFileName(fileName);
        this.tempFilePath = saveDirectory + finalFileName + TEMP_SUFFIX;

        // 获取文件大小
        this.fileSize = connection.getContentLengthLong();
        if (fileSize == -1) {
            logger.warn("File size unknown, progress tracking limited");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String baseName) {
        String name = baseName;
        int counter = 1;

        while (new File(saveDirectory + name).exists()) {
            int dotIndex = baseName.lastIndexOf('.');
            if (dotIndex > 0) {
                name = baseName.substring(0, dotIndex) +
                        "(" + counter + ")" +
                        baseName.substring(dotIndex);
            } else {
                name = baseName + "(" + counter + ")";
            }
            counter++;
        }
        return name;
    }

    /**
     * 更新下载进度
     */
    private synchronized void updateProgress(int bytes) {
        bytesRead.addAndGet(bytes);
        if (fileSize > 0) {
            downloadProgress = (bytesRead.get() * 100.0) / fileSize;
            remainingTime = calculateRemainingTime();
        }
    }

    /**
     * 计算传输指标
     */
    private synchronized void calculateMetrics() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;

        if (elapsed >= PROGRESS_UPDATE_INTERVAL) {
            double bytesDiff = bytesRead.get() - lastBytesRecord;
            bytesPerSecond = (bytesDiff * 1000.0) / elapsed;

            lastUpdateTime = currentTime;
            lastBytesRecord = bytesRead.get();
        }
    }

    /**
     * 计算剩余时间（秒）
     */
    private long calculateRemainingTime() {
        if (fileSize <= 0 || bytesPerSecond <= 0) return -1;

        long remainingBytes = fileSize - bytesRead.get();
        return (long) (remainingBytes / bytesPerSecond);
    }

    /**
     * 完成下载处理
     */
    private void completeDownload() throws IOException {
        if (isStopped.get()) return;

        File tempFile = new File(tempFilePath);
        File finalFile = new File(saveDirectory + finalFileName);

        if (!tempFile.renameTo(finalFile)) {
            // 备选方案：使用文件复制
            try (InputStream in = new FileInputStream(tempFile);
                 OutputStream out = new FileOutputStream(finalFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            tempFile.delete();
        }

        isCompleted.set(true);
        logger.info("Download completed: {}", finalFile.getAbsolutePath());


    }

    /**
     * 处理下载错误
     */
    private void handleDownloadError(IOException e) {
        logger.error("Download failed: {}", e.getMessage());
        if (e instanceof FileNotFoundException) {
            logger.error("File not found on server");
        }
    }

    /**
     * 清理资源
     */
    private void cleanResources() {
        if (isStopped.get() && !isCompleted.get()) {
            new File(tempFilePath).delete();
            logger.info("Cleaned temp file");
        }
    }

    // 辅助方法

    /**
     * 验证HTTP响应有效性
     */
    private void validateResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();

        // 允许的状态码：200（完整下载），206（部分内容），重定向码
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            handleRedirect(connection);
            return;
        }

        if (responseCode != HttpURLConnection.HTTP_OK
                && responseCode != HttpURLConnection.HTTP_PARTIAL) {
            throw new IOException("无效的HTTP响应码: " + responseCode
                    + " | 消息: " + connection.getResponseMessage());
        }
    }

    /**
     * 处理重定向逻辑
     */
    private void handleRedirect(HttpURLConnection connection) throws IOException {
        if (redirectCount++ >= MAX_REDIRECTS) {
            throw new IOException("超过最大重定向次数 (" + MAX_REDIRECTS + ")");
        }

        String newUrl = connection.getHeaderField("Location");
        if (newUrl == null) {
            throw new IOException("收到重定向响应但没有Location头");
        }

        // 更新下载URL并重新开始
        this.sourceUrl = newUrl;
        logger.info("检测到重定向到: {}", newUrl);
        startNewDownload();
    }

    private String normalizeDirectoryPath(String path) {
        path = path.replace("\\", "/");
        return path.endsWith("/") ? path : path + "/";
    }

    private void createSaveDirectory() {
        File dir = new File(saveDirectory);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create save directory");
        }
    }

    private String getFileNameFromHeader(HttpURLConnection connection) {
        String disposition = connection.getHeaderField("Content-Disposition");
        if (disposition == null) return null;

        String[] tokens = disposition.split(";");
        for (String token : tokens) {
            token = token.trim();
            if (token.startsWith("filename=")) {
                String encodedName = token.substring(9).trim().replace("\"", "");
                return new String(encodedName.getBytes(StandardCharsets.ISO_8859_1),
                        StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /**
     * 从URL获取文件名（添加异常处理）
     */
    private String getFileNameFromUrl() {
        try {
            URL url = new URL(sourceUrl);
            String path = url.getPath();
            int slashIndex = path.lastIndexOf('/');
            return slashIndex == -1 ? path : path.substring(slashIndex + 1);
        } catch (MalformedURLException e) {
            if (downloadErrorHandler != null)
                downloadErrorHandler.handler(e, this);
            else
                logger.error("Invalid URL format: {}", sourceUrl, e);
            return "unknown_file";
        }
    }

    // 控制方法
    public void stopDownload() {
        isStopped.set(true);
    }

    public void resetDownload() {
        bytesRead.set(0);
        downloadProgress = 0;
        bytesPerSecond = 0;
        remainingTime = -1;
    }

    public double getProgress() {
        return downloadProgress;
    }

    public long getBytesRead() {
        return bytesRead.get();
    }

    public double getSpeedBytesPerSecond() {
        return bytesPerSecond;
    }

    public long getRemainingSeconds() {
        return remainingTime;
    }

    public boolean isCompleted() {
        return isCompleted.get();
    }

    public String getFinalPath() {
        return saveDirectory + finalFileName;
    }
}
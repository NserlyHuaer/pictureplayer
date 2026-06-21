/*
 * Copyright 2026 PicturePlayer;Nserly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.nserly.SoftwareCollections_API.Hashcode;

import lombok.extern.slf4j.Slf4j;
import top.nserly.SoftwareCollections_API.Thread.ThreadControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 高性能文件哈希工具：MD5 / SHA-1 / SHA-256
 * 约束：仅使用 VirtualThreadsController 对外提供的公开方法提交任务，不修改控制器内部代码
 * 特性：查表转十六进制低GC、大文件分块IO、批量虚拟线程并发、全英文日志、中文注释
 */
@Slf4j
public final class FileHashUtil {
    public static final ArrayList<String> SUPPORTED_ALGORITHMS = new ArrayList<>();
    // IO缓冲区64KB，减少系统IO调用，SSD推荐；机械硬盘可下调至32768
    private static final int BUFFER_SIZE = 65536;
    // 十六进制查表数组，替代String.format，大幅提升字节转16进制性能
    private static final char[] HEX_TABLE = "0123456789abcdef".toCharArray();
    // 哈希算法常量定义
    public static final String ALG_MD5 = "MD5";
    public static final String ALG_SHA1 = "SHA-1";
    public static final String ALG_SHA256 = "SHA-256";
    // 批量任务自增ID生成器，保证taskId全局唯一
    private static final AtomicInteger TASK_ID_SEQ;

    static {
        SUPPORTED_ALGORITHMS.add(ALG_MD5);
        SUPPORTED_ALGORITHMS.add(ALG_SHA1);
        SUPPORTED_ALGORITHMS.add(ALG_SHA256);
        TASK_ID_SEQ = new AtomicInteger(1);
        log.info("FileHashUtil initialized, IO buffer size: {}KB, virtual thread controller ready", BUFFER_SIZE / 1024);
    }

    // 私有构造，禁止外部实例化工具类
    private FileHashUtil() {
        throw new AssertionError("Utility class cannot be instantiated!");
    }

    // region 单文件哈希底层计算方法

    /**
     * 计算单个文件哈希核心逻辑
     *
     * @param file      目标文件对象
     * @param algorithm 哈希算法名称 MD5/SHA-1/SHA-256
     * @return 小写十六进制哈希字符串
     * @throws IOException              文件不存在、无权限、读取失败
     * @throws NoSuchAlgorithmException 传入的哈希算法不支持
     */
    public static String getFileHash(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        if (file == null) {
            log.error("File object is null, skip hash calculation");
            throw new IllegalArgumentException("File cannot be null");
        }
        String filePath = file.getAbsolutePath();
        if (!file.exists()) {
            log.error("Target file not found, path: {}", filePath);
            throw new IOException("File not exists: " + filePath);
        }
        if (!file.isFile()) {
            log.error("Target path is not a regular file, path: {}", filePath);
            throw new IOException("Target path is not a regular file: " + filePath);
        }
        if (!file.canRead()) {
            log.error("No read permission for file, path: {}", filePath);
            throw new IOException("No read permission for file: " + filePath);
        }

        log.debug("Start calculating {} hash for file: {}", algorithm, filePath);
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[BUFFER_SIZE];

        try (InputStream in = new FileInputStream(file)) {
            int readLen;
            while ((readLen = in.read(buffer)) != -1) {
                digest.update(buffer, 0, readLen);
            }
        }

        String hashResult = bytesToHex(digest.digest());
        log.debug("{} hash calculation completed, file: {}, result: {}", algorithm, filePath, hashResult);
        return hashResult;
    }

    /**
     * 字节数组转小写16进制字符串，查表法无多余临时对象
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int val = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_TABLE[val >>> 4];
            hexChars[i * 2 + 1] = HEX_TABLE[val & 0x0F];
        }
        return new String(hexChars);
    }

    // 单文件MD5快捷入口
    public static String getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        return getFileHash(file, ALG_MD5);
    }

    // 单文件SHA1快捷入口
    public static String getFileSHA1(File file) throws IOException, NoSuchAlgorithmException {
        return getFileHash(file, ALG_SHA1);
    }

    // 单文件SHA256快捷入口
    public static String getFileSHA256(File file) throws IOException, NoSuchAlgorithmException {
        return getFileHash(file, ALG_SHA256);
    }

    // 重载：支持文件路径字符串直接传入
    public static String getFileHash(String filePath, String algorithm) throws IOException, NoSuchAlgorithmException {
        return getFileHash(new File(filePath), algorithm);
    }

    public static String getFileMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        return getFileHash(filePath, ALG_MD5);
    }

    public static String getFileSHA256(String filePath) throws IOException, NoSuchAlgorithmException {
        return getFileHash(filePath, ALG_SHA256);
    }
    // endregion

    // region 哈希校验相关方法

    /**
     * 校验文件哈希值与预期字符串是否匹配，忽略大小写
     *
     * @param file       待校验文件
     * @param algorithm  哈希算法
     * @param expectHash 预期哈希字符串
     * @return true=校验通过 false=不匹配/参数非法
     */
    public static boolean verifyFileHash(File file, String algorithm, String expectHash)
            throws IOException, NoSuchAlgorithmException {
        if (expectHash == null || expectHash.isBlank()) {
            log.warn("Expected hash string is blank, verification failed, file path: {}", file.getAbsolutePath());
            return false;
        }
        String realHash = getFileHash(file, algorithm);
        boolean match = realHash.equalsIgnoreCase(expectHash.trim());
        if (match) {
            log.info("{} hash verification passed for file: {}", algorithm, file.getAbsolutePath());
        } else {
            log.warn("{} hash mismatch detected, file: {}, expected: {}, actual: {}",
                    algorithm, file.getAbsolutePath(), expectHash, realHash);
        }
        return match;
    }

    public static boolean verifyMD5(File file, String expectMd5) throws IOException, NoSuchAlgorithmException {
        return verifyFileHash(file, ALG_MD5, expectMd5);
    }

    public static boolean verifySHA256(File file, String expectSha256) throws IOException, NoSuchAlgorithmException {
        return verifyFileHash(file, ALG_SHA256, expectSha256);
    }

    public static boolean verifyFileHash(String filePath, String algorithm, String expectHash)
            throws IOException, NoSuchAlgorithmException {
        return verifyFileHash(new File(filePath), algorithm, expectHash);
    }
    // endregion

    // region 批量并发哈希（仅调用控制器 executeImmediately 公开方法）

    /**
     * 批量并发计算多文件哈希，严格遵循约束：只使用控制器提供的公开方法提交任务
     * 内部使用CountDownLatch阻塞等待全部任务完成，ConcurrentHashMap收集结果
     *
     * @param fileList  待计算文件集合
     * @param algorithm 哈希算法名称
     * @return 计算成功的文件与哈希映射，失败文件不存入Map
     */
    public static Map<File, String> batchGetFileHash(Collection<File> fileList, String algorithm) {
        if (fileList == null || fileList.isEmpty()) {
            log.warn("Input file collection is empty, skip batch hash calculation");
            return new HashMap<>(0);
        }
        int totalCount = fileList.size();
        CountDownLatch latch = new CountDownLatch(totalCount);
        ConcurrentHashMap<File, String> resultMap = new ConcurrentHashMap<>(totalCount);
        AtomicInteger failCounter = new AtomicInteger(0);

        log.info("Start batch {} hash task, total target files: {}", algorithm, totalCount);

        // 遍历所有文件，通过控制器公开executeImmediately提交Runnable任务
        for (File targetFile : fileList) {
            String taskId = "HASH_BATCH_TASK_" + TASK_ID_SEQ.getAndIncrement();
            Runnable hashTask = () -> {
                try {
                    String hashVal = getFileHash(targetFile, algorithm);
                    resultMap.put(targetFile, hashVal);
                } catch (Exception e) {
                    failCounter.incrementAndGet();
                    log.error("Failed to calculate {} hash for file {}", algorithm, targetFile.getAbsolutePath(), e);
                } finally {
                    // 无论成功失败，计数器减一，通知主线程任务完成
                    latch.countDown();
                }
            };
            // 唯一允许调用的控制器提交API，无任何内部字段/执行器直接访问
            ThreadControl.virtualThreadsController.executeImmediately(taskId, hashTask);
        }

        try {
            // 阻塞等待所有批量哈希任务执行完毕
            latch.await();
        } catch (InterruptedException e) {
            log.error("Batch hash waiting latch interrupted", e);
            Thread.currentThread().interrupt();
        }

        int successNum = resultMap.size();
        int failNum = failCounter.get();
        log.info("Batch {} hash task finished, total: {}, success: {}, failed: {}",
                algorithm, totalCount, successNum, failNum);
        // 转为普通HashMap返回
        return new HashMap<>(resultMap);
    }

    // 批量MD5快捷接口
    public static Map<File, String> batchGetMD5(Collection<File> fileList) {
        return batchGetFileHash(fileList, ALG_MD5);
    }

    // 批量SHA256快捷接口
    public static Map<File, String> batchGetSHA256(Collection<File> fileList) {
        return batchGetFileHash(fileList, ALG_SHA256);
    }

    /**
     * 批量校验一组文件哈希匹配情况
     *
     * @param fileExpectMap Key=文件对象 Value=预期哈希字符串
     * @param algorithm     哈希算法
     * @return 所有校验通过的文件列表
     */
    public static List<File> batchVerifyHash(Map<File, String> fileExpectMap, String algorithm) {
        log.info("Start batch hash verification, target file amount: {}", fileExpectMap.size());
        Map<File, String> realHashMap = batchGetFileHash(fileExpectMap.keySet(), algorithm);
        List<File> passedFiles = realHashMap.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(fileExpectMap.get(entry.getKey())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        log.info("Batch hash verification completed, passed file count: {}", passedFiles.size());
        return passedFiles;
    }

}
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

package top.nserly.SoftwareCollections_API.Queue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 支持动态切换「平台线程/虚拟线程」的任务队列
 * 任务按添加顺序执行，可通过参数控制是否启用虚拟线程
 */
@Slf4j
public class ThreadPoolTaskQueue {
    private final ExecutorService executor; // 统一封装平台/虚拟线程池
    private ThreadPoolExecutor platformThreadPool; // 仅平台线程模式使用
    /**
     * -- GETTER --
     *  获取最大线程数（仅平台线程模式有效）
     */
    @Getter
    private final int maxThreadCount;
    /**
     * -- GETTER --
     *  获取是否启用虚拟线程
     */
    @Getter
    private final boolean useVirtualThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final BlockingQueue<Runnable> taskQueue; // 通用任务队列

    /**
     * 构造任务队列（支持动态切换线程模式）
     * @param corePoolSize 核心线程数（仅平台线程模式有效）
     * @param maxThreadCount 最大线程数（仅平台线程模式有效）
     * @param useVirtualThread 是否启用虚拟线程
     */
    public ThreadPoolTaskQueue(int corePoolSize, int maxThreadCount, boolean useVirtualThread) {
        // 参数校验：平台线程模式下校验线程数，虚拟线程模式下仅做基础校验
        if (useVirtualThread) {
            if (corePoolSize <= 0 || maxThreadCount <= 0) {
                throw new IllegalArgumentException("Core/max thread count must be positive (for compatibility)");
            }
        } else {
            if (corePoolSize <= 0 || maxThreadCount <= 0 || corePoolSize > maxThreadCount) {
                throw new IllegalArgumentException("The thread number parameter is not legitimate");
            }
        }

        this.maxThreadCount = maxThreadCount;
        this.useVirtualThread = useVirtualThread;
        // 通用有界队列：平台线程模式用于任务缓冲，虚拟线程模式用于统计/拒绝策略
        this.taskQueue = new LinkedBlockingQueue<>(1024);

        // 根据参数初始化不同的线程池
        if (useVirtualThread) {
            // 虚拟线程模式：创建虚拟线程池
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
            log.info("Virtual thread pool initialized (compatible core: {}, max: {})", corePoolSize, maxThreadCount);
        } else {
            // 平台线程模式：保留原有平台线程池逻辑
            RejectedExecutionHandler handler = (r, executor) -> {
                try {
                    if (!executor.getQueue().offer(r, 1, TimeUnit.SECONDS)) {
                        throw new RejectedExecutionException("The task queue is full, and adding tasks fails");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RejectedExecutionException("Task addition is interrupted", e);
                }
            };

            this.platformThreadPool = new ThreadPoolExecutor(
                    corePoolSize,
                    maxThreadCount,
                    60,
                    TimeUnit.SECONDS,
                    taskQueue,
                    Executors.defaultThreadFactory(),
                    handler
            );
            this.platformThreadPool.allowCoreThreadTimeOut(true);
            this.executor = platformThreadPool; // 统一赋值给通用executor
            log.info("Platform thread pool initialized (core: {}, max: {})", corePoolSize, maxThreadCount);
        }
    }

    /**
     * 重载构造方法（兼容原有调用，默认使用平台线程）
     * @param corePoolSize 核心线程数
     * @param maxThreadCount 最大线程数
     */
    public ThreadPoolTaskQueue(int corePoolSize, int maxThreadCount) {
        this(corePoolSize, maxThreadCount, false);
    }

    /**
     * 添加任务并返回Future，逻辑兼容两种线程模式
     * @param task 待执行的任务
     * @return 任务的Future对象，可用于取消或获取结果
     */
    public Future<?> addTask(Runnable task) {
        if (!isRunning.get() || task == null) {
            return null;
        }
        try {
            // 统一的拒绝策略逻辑：先尝试加入队列
            if (!taskQueue.offer(task, 1, TimeUnit.SECONDS)) {
                throw new RejectedExecutionException("The task queue is full, and adding tasks fails");
            }

            // 包装任务：执行完成后从队列移除（保证统计准确）
            Runnable wrappedTask = () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("Task execution failed", e);
                } finally {
                    taskQueue.remove(task);
                }
            };

            // 提交任务到通用executor（自动适配平台/虚拟线程）
            return executor.submit(wrappedTask);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Task addition is interrupted", e);
            return null;
        } catch (RejectedExecutionException e) {
            log.error("Adding a task failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 停止任务队列，逻辑兼容两种线程模式
     * @param waitForCompletion 是否等待所有任务完成
     */
    public void stop(boolean waitForCompletion) {
        if (isRunning.compareAndSet(true, false)) {
            if (waitForCompletion) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                        executor.shutdownNow();
                        log.warn("Thread pool termination timed out, force shutdown");
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                    log.error("Thread pool termination interrupted", e);
                }
            } else {
                executor.shutdownNow();
                log.info("Thread pool force shutdown");
            }
            taskQueue.clear(); // 清空未执行任务
            log.info("Thread pool stopped, remaining tasks cleared: {}", taskQueue.size());
        }
    }

    /**
     * 获取当前活跃线程数（兼容两种模式）
     */
    public int getActiveThreadCount() {
        if (useVirtualThread) {
            // 虚拟线程模式：返回队列中待执行任务数（近似活跃数）
            return taskQueue.size();
        } else {
            // 平台线程模式：返回原生活跃线程数
            return platformThreadPool.getActiveCount();
        }
    }

    /**
     * 获取等待中的任务数（通用逻辑）
     */
    public int getPendingTaskCount() {
        return taskQueue.size();
    }

    /**
     * 检查队列是否正在运行
     */
    public boolean isRunning() {
        return isRunning.get();
    }
}
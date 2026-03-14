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

package top.nserly.SoftwareCollections_API.Thread;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread task manager (compatible with JDK 25)
 * Supports immediate execution, periodic execution, termination, shutdown, and thread viewing for Runnable tasks
 */
@Slf4j
public class VirtualThreadsController {

    // Core thread pool (supports scheduled tasks)
    private final ScheduledExecutorService executorService;
    // Store submitted scheduled tasks (for terminating specific tasks)
    private final Map<String, ScheduledFuture<?>> taskMap;
    // Flag to mark if the manager is shutdown
    private final AtomicBoolean isShutdown;

    /**
     * Constructor: Initialize thread pool (default core thread count is 5, adjustable as needed)
     */
    public VirtualThreadsController() {
        // JDK 25 recommends using Executors factory methods or custom ThreadPoolExecutor
        this.executorService = Executors.newScheduledThreadPool(5);
        this.taskMap = new ConcurrentHashMap<>(); // Thread-safe Map
        this.isShutdown = new AtomicBoolean(false);
    }

    /**
     * Execute Runnable task immediately
     * @param taskId Unique task identifier (for subsequent termination)
     * @param task Runnable task to execute
     */
    public void executeImmediately(String taskId, Runnable task) {
        checkShutdownStatus();
        if (taskMap.containsKey(taskId)) {
            throw new IllegalArgumentException("Task ID[" + taskId + "] already exists, do not submit repeatedly");
        }
        // Execute task immediately, store the returned Future in Map (even for immediate execution, it can be used for termination)
        ScheduledFuture<?> future = executorService.schedule(task, 0, TimeUnit.MILLISECONDS);
        taskMap.put(taskId, future);
        log.info("Task[{}] executed immediately", taskId);
    }

    /**
     * Execute Runnable task periodically (fixed delay execution, execute again after specified delay after one execution)
     * @param taskId Unique task identifier
     * @param task Runnable task to execute
     * @param initialDelay Initial delay time (how long to wait before first execution)
     * @param period Execution period (interval from end of last execution to start of next execution)
     * @param timeUnit Time unit
     */
    public void executePeriodically(String taskId, Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
        checkShutdownStatus();
        if (taskMap.containsKey(taskId)) {
            throw new IllegalArgumentException("Task ID[" + taskId + "] already exists, do not submit repeatedly");
        }
        // Execute task periodically (fixedDelay: fixed delay)
        ScheduledFuture<?> future = executorService.scheduleWithFixedDelay(task, initialDelay, period, timeUnit);
        taskMap.put(taskId, future);
        log.info("Task[{}] started periodic execution (initial delay: {} {}, period: {} {})",
                taskId, initialDelay, timeUnit, period, timeUnit);
    }

    /**
     * Terminate specified task immediately
     * @param taskId Unique task identifier
     * @return true: termination successful; false: task does not exist or already terminated
     */
    public boolean terminateTaskImmediately(String taskId) {
        checkShutdownStatus();
        ScheduledFuture<?> future = taskMap.get(taskId);
        if (future == null) {
            log.warn("Task[{}] does not exist, termination failed", taskId);
            return false;
        }
        // Try to terminate the task (mayInterruptIfRunning: whether to interrupt running tasks)
        boolean isCancelled = future.cancel(true);
        if (isCancelled) {
            taskMap.remove(taskId);
            log.info("Task[{}] terminated successfully", taskId);
        } else {
            log.warn("Task[{}] has been completed or cannot be terminated", taskId);
        }
        return isCancelled;
    }

    /**
     * Shutdown the entire task manager (reject new tasks, wait for submitted tasks to complete, force shutdown if timeout)
     * @param timeout Wait timeout time
     * @param timeUnit Time unit
     * @return true: normal shutdown; false: forced shutdown due to timeout
     * @throws InterruptedException Interrupted during waiting
     */
    public boolean shutdown(long timeout, TimeUnit timeUnit) throws InterruptedException {
        if (isShutdown.compareAndSet(false, true)) {
            executorService.shutdown(); // Reject new tasks, start shutdown process
            // Wait for all tasks to complete, force shutdown if timeout
            boolean isTerminated = executorService.awaitTermination(timeout, timeUnit);
            if (!isTerminated) {
                // Force shutdown, interrupt all running tasks
                List<Runnable> remainingTasks = executorService.shutdownNow();
                log.warn("Task manager timeout, forced shutdown, number of remaining unexecuted tasks: {}", remainingTasks.size());
            } else {
                log.info("Task manager shutdown normally");
            }
            taskMap.clear(); // Clear task cache
            return isTerminated;
        }
        log.info("Task manager already shutdown, no need to repeat operation");
        return true;
    }

    /**
     * View all active tasks (unterminated/uncompleted tasks)
     * @return List of active task IDs
     */
    public List<String> listActiveTasks() {
        if (isShutdown.get()) {
            log.info("Task manager already shutdown, no active tasks");
            return Collections.emptyList();
        }
        // Filter out uncompleted tasks (isDone()=false means task is still running/waiting to execute)
        List<String> activeTaskIds = new ArrayList<>();
        Iterator<Map.Entry<String, ScheduledFuture<?>>> iterator = taskMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ScheduledFuture<?>> entry = iterator.next();
            if (!entry.getValue().isDone()) {
                activeTaskIds.add(entry.getKey());
            } else {
                // Clean up completed tasks (avoid memory leaks)
                iterator.remove();
            }
        }
        log.info("Current number of active tasks: {}, task IDs: {}", activeTaskIds.size(), activeTaskIds);
        return activeTaskIds;
    }

    /**
     * Check if the manager is shutdown, throw exception if already shutdown
     */
    private void checkShutdownStatus() {
        if (isShutdown.get()) {
            throw new IllegalStateException("Task manager already shutdown, cannot perform this operation");
        }
    }
}
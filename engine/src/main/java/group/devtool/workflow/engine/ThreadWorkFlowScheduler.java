/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import group.devtool.workflow.engine.exception.WorkFlowRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class ThreadWorkFlowScheduler implements WorkFlowDelayTaskScheduler, Runnable {

    private final Object wait = new Object();
    private final ExecutorService pool;
    private final ArrayBlockingQueue<DelayItem> queue;
    private final int corePoolSize;
    private volatile boolean ready = false;

    protected ThreadWorkFlowScheduler(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        queue = new ArrayBlockingQueue<>(64);
        pool = Executors.newFixedThreadPool(corePoolSize + 1);
    }

    public boolean ready() {
        return ready;
    }

    public void start() {
        ready = true;
        pool.execute(() -> {
            boolean running = true;
            while (running) {
                try {
                    List<DelayItem> tasks = loadTask();
                    if (tasks.isEmpty()) {
                        sleep();
                    } else {
                        queue.addAll(tasks);
                    }
                } catch (InterruptedException e) {
                    running = false;
                    log.error("线程中断...", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("加载延时任务异常. 异常堆栈: ", e);
                    running = false;
                }
            }
        });
        for (int i = 0; i < corePoolSize; i++) {
            pool.execute(this);
        }
    }

    protected abstract List<DelayItem> loadTask();

    private synchronized void sleep() throws InterruptedException {
        while (queue.size() > 32) {
            wait.wait(1000);
        }
    }

    @Override
    public void run() {
        do {
            DelayItem item;
            try {
                item = queue.take();
            } catch (InterruptedException e) {
                log.error("线程中断...", e);
                Thread.currentThread().interrupt();
                break;
            }
            try {
                item.run();
                delayAfter(item);
            } catch (WorkFlowRuntimeException e) {
                log.error("延时任务执行异常. 异常堆栈: ", e);
            }
        } while (true);
    }

    protected abstract void delayAfter(DelayItem item);

    @Override
    public void close() {
        pool.shutdown();
    }

}

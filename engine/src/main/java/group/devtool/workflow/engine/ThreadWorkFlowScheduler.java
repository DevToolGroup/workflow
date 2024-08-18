/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import group.devtool.workflow.engine.exception.WorkFlowRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class ThreadWorkFlowScheduler implements WorkFlowDelayTaskScheduler, Runnable {

	private final ExecutorService pool;

	private final ArrayBlockingQueue<DelayItem> queue;

	private final Object lock = new Object();

	private volatile boolean ready = false;

	public ThreadWorkFlowScheduler(int corePoolSize) {
		queue = new ArrayBlockingQueue<>(64);
		pool = Executors.newFixedThreadPool(corePoolSize + 1);
		pool.execute(() -> {
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					return;
				}
			}
			boolean running = true;
			do {
				List<DelayItem> tasks = new ArrayList<>();
				tasks = loadTask();
				if (tasks.isEmpty()) {
					running = sleep();
				}
				for (DelayItem task : tasks) {
					if (queue.size() > 32) {
						running = sleep();
					}
					queue.add(task);
				}
			} while (running);
		});
		for (int i = 0; i < corePoolSize; i++) {
			pool.execute(this);
		}
	}

	public boolean ready() {
		return ready;
	}

	public void start() {
		synchronized (lock) {
			lock.notifyAll();
			ready = true;
		}
	}

	protected abstract List<DelayItem> loadTask();

	private synchronized boolean sleep() {
		boolean running = true;
		try {
			wait(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			running = false;
		}
		return running;
	}

	@Override
	public void run() {
		do {
			DelayItem item = null;
			try {
				item = queue.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			try {
				item.run();
				delayAfter(item);
			} catch (WorkFlowRuntimeException e) {
				// continue to next task
				log.error("WorkFlowRuntimeException: {}", e.getMessage());
			}
		} while (true);
	}

	protected abstract void delayAfter(DelayItem item);

	@Override
	public void close() throws IOException {
		pool.shutdown();
	}

}

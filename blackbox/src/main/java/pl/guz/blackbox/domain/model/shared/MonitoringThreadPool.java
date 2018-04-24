package pl.guz.blackbox.domain.model.shared;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MonitoringThreadPool {
    public static Executor threadPool(String threadPoolName,
                                      int capacity,
                                      int corePoolSize,
                                      int maximumPoolSize,
                                      int keepAliveTime
                                     ) {
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(capacity);
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                                                                    keepAliveTime, TimeUnit.MILLISECONDS,
                                                                    workQueue,
                                                                    new CustomizableThreadFactory(threadPoolName + "-"));
        Metrics.gauge("", Tags.of(threadPoolName, "queue-utilization"), workQueue, queue -> queue.size() / (double) (queue.size() + queue.remainingCapacity()));
        Metrics.gauge("", Tags.of(threadPoolName, "queue-size"), workQueue, LinkedBlockingQueue::size);
        Metrics.gauge("", Tags.of(threadPoolName, "activeThreads"), executorService, ThreadPoolExecutor::getActiveCount);
        return executorService;
    }
}

package spongecell.backend.metricsagregatator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spongecell.backend.metricsagregatator.config.HttpClientConfig;
import spongecell.backend.metricsagregatator.controller.RestWorker;
import spongecell.backend.metricsagregatator.controller.SharedObject;
import spongecell.backend.metricsagregatator.dto.MetricResponseDTO;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Service collects data from metrics backend
 * <p>
 * Created by abyakimenko on 04.03.2019.
 */
@Slf4j
@Service
public class MetricService {

    @Value("${metric.url}")
    private String metricUrl;
    @Value("${metric.checkRange}")
    private long metricCheckRange;
    @Value("${metric.workers}")
    private int workers;

    private final HttpClientConfig rest;
    private final ExecutorService threadPool;

    //create a list to hold the Future object associated with Callable
    private final List<MetricResponseDTO> listResult = new ArrayList<>();

    @Autowired
    public MetricService(HttpClientConfig rest, @Qualifier("fixedThreadPool") ExecutorService threadPool) {
        this.rest = rest;
        this.threadPool = threadPool;
    }

    public List<MetricResponseDTO> processTask() {
        listResult.clear();
        return parallelTask();
    }

    private List<MetricResponseDTO> parallelTask() {
        log.info("### parallelTask entered, thread: {}", Thread.currentThread().getName());
        //Get ExecutorService from Executors utility class, thread pool size is THREADS_CNT
        //Create RestWorker instance
        final CountDownLatch latch = new CountDownLatch(workers);
        final SharedObject sharedObject = new SharedObject(latch);
        final List<Future<MetricResponseDTO>> list = new ArrayList<>();
        
        for (int i = 0; i < workers; i++) {
            //submit Callable tasks to be executed by thread pool
            Future<MetricResponseDTO> future = threadPool.submit(new RestWorker(metricUrl, sharedObject, rest));
            //add Future to the list, we can get return value using Future
            list.add(future);
            latch.countDown();
        }

        sharedObject.setStartTime(LocalTime.now());
        // start executing threads
        latch.countDown();

        awaitAndTerminate(list);
        log.info("### TOTAL REQUESTS: {}", listResult.size());
        return listResult;
    }

    private void awaitAndTerminate(List<Future<MetricResponseDTO>> list) {
        try {
            if (!threadPool.awaitTermination(metricCheckRange, TimeUnit.MILLISECONDS)) {
                for (Future<MetricResponseDTO> fut : list) {
                    if (fut.isDone()) {
                        handleFuture(fut);
                    }
                }
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void handleFuture(Future<MetricResponseDTO> fut) {
        try {
            listResult.add(fut.get());
        } catch (InterruptedException ex) {
            log.error("Timer InterruptedException", ex);
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            log.error("Timer ExecutionException", ex);
        }
    }
}

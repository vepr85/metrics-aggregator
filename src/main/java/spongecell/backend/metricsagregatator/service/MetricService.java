package spongecell.backend.metricsagregatator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import spongecell.backend.metricsagregatator.settings.MetricSettings;
import spongecell.backend.metricsagregatator.workers.RestWorker;
import spongecell.backend.metricsagregatator.dto.MetricResponseDTO;

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
@RequiredArgsConstructor
public class MetricService {

    private final RestWorker worker;
    private final CountDownLatch latch;
    private final MetricSettings settings;
    private final ExecutorService threadPool;

    //create a list to hold the Future object associated with Callable
    private final List<MetricResponseDTO> listResult = new ArrayList<>();

    public List<MetricResponseDTO> processTask() {
        listResult.clear();
        return parallelTask();
    }

    private List<MetricResponseDTO> parallelTask() {
        log.info("### parallelTask entered, thread: {}", Thread.currentThread().getName());
        //Get ExecutorService from Executors utility class, thread pool size is THREADS_CNT
        final List<Future<MetricResponseDTO>> list = new ArrayList<>();
        
        for (int i = 0; i < settings.getWorkers(); i++) {
            final RestWorker restWorker = getWorker();
            Future<MetricResponseDTO> future = threadPool.submit(restWorker);
            //add Future to the list, we can get return value using Future
            list.add(future);
            latch.countDown();
        }
        
        // start executing threads
        latch.countDown();

        awaitAndTerminate(list);
        log.info("### TOTAL REQUESTS: {}", listResult.size());
        return listResult;
    }

    @Lookup
    private RestWorker getWorker() {
        return worker;
    }
    
    private void awaitAndTerminate(List<Future<MetricResponseDTO>> list) {
        try {
            if (!threadPool.awaitTermination(settings.getCheckRange(), TimeUnit.MILLISECONDS)) {
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

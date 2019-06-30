package spongecell.backend.metricsagregatator.workers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import spongecell.backend.metricsagregatator.settings.MetricSettings;
import spongecell.backend.metricsagregatator.configuration.RestConfiguration;
import spongecell.backend.metricsagregatator.dto.MetricResponseDTO;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RequiredArgsConstructor
@Component
public class RestWorker implements Callable<MetricResponseDTO> {

    private final RestConfiguration rest;
    private final CountDownLatch latch;
    private final MetricSettings settings;

    @Override
    public MetricResponseDTO call() throws InterruptedException {
        log.info("RestWorker thread STARTED: {}", Thread.currentThread().getName());
        latch.await();
        MetricResponseDTO result = processTask();
        log.info("RestWorker thread ENDED: {}", Thread.currentThread().getName());
        return result;
    }

    private MetricResponseDTO processTask() {
        ResponseEntity<MetricResponseDTO> responseBody = rest.restTemplate()
                .getForEntity(settings.getUrl().concat("/v1/metrics"), MetricResponseDTO.class);

        return Optional.ofNullable(responseBody.getBody())
                .orElse(MetricResponseDTO
                        .builder()
                        .size(0)
                        .brandMetrics(new ArrayList<>())
                        .build());
    }
}

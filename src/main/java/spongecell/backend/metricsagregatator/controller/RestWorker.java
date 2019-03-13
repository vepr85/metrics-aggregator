package spongecell.backend.metricsagregatator.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import spongecell.backend.metricsagregatator.config.HttpClientConfig;
import spongecell.backend.metricsagregatator.dto.MetricResponseDTO;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;

@Slf4j
public class RestWorker implements Callable<MetricResponseDTO> {

    private final String metricUrl;
    private final SharedObject object;
    private final HttpClientConfig rest;

    public RestWorker(String metricUrl, SharedObject object, HttpClientConfig rest) {
        this.metricUrl = metricUrl;
        this.object = object;
        this.rest = rest;
    }

    @Override
    public MetricResponseDTO call() throws InterruptedException {
        log.info("RestWorker thread STARTED: {}", Thread.currentThread().getName());
        object.getCountDownLatch().await();
        MetricResponseDTO result = processTask();
        log.info("RestWorker thread ENDED: {}", Thread.currentThread().getName());
        return result;
    }

    private MetricResponseDTO processTask() {

        ResponseEntity<MetricResponseDTO> responseBody = rest.restTemplate()
                .getForEntity(metricUrl.concat("/v1/metrics"), MetricResponseDTO.class);

        return Objects.isNull(responseBody) ? MetricResponseDTO
                .builder()
                .size(0)
                .brandMetrics(new ArrayList<>())
                .build()
                :
                responseBody.getBody();
    }
}
package spongecell.backend.metricsagregatator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spongecell.backend.metricsagregatator.dto.*;
import spongecell.backend.metricsagregatator.service.AggregatorService;
import spongecell.backend.metricsagregatator.service.MetricService;

import java.util.Collections;
import java.util.List;

/**
 * Controller for handling requests for metrics from backend
 * <p>
 * Created by abyakimenko on 04.03.2019.
 */
@RestController
@RequestMapping("/aggregate")
public class AggregatorController {
    private final MetricService metricService;
    private final AggregatorService aggregatorService;

    @Autowired
    public AggregatorController(MetricService metricService, AggregatorService aggregatorService) {
        this.metricService = metricService;
        this.aggregatorService = aggregatorService;
    }

    /**
     * test1 method - check1
     *
     * @return MetricResponseDTO
     */
    @GetMapping("/test")
    public MetricResponseDTO test() {
        List<MetricResponseDTO> responses = metricService.processTask();
        return responses.stream().findFirst().orElse(MetricResponseDTO.emptyResponse());
    }

    /**
     * test2 method - check2
     *
     * @return
     */
    @PostMapping("/test1")
    public void test1(@RequestBody MetricResponseDTO metrics) {
        aggregatorService.groupByBrand(Collections.singletonList(metrics));
    }

    /**
     * Collects data by brand
     *
     * @return
     */
    @GetMapping("/by-brand")
    public List<SummaryByBrandDTO> byBrand() {
        return aggregatorService.groupByBrand(metricService.processTask());
    }

    /**
     * Collects metric data by datetime and brand desc
     *
     * @return
     */
    @GetMapping("/by-date-brand")
    public List<SummaryByDateBrandDTO> byDateBrand() {
        return aggregatorService.groupByDateBrand(metricService.processTask());
    }

    /**
     * Collects metric data by datetime asc
     *
     * @return
     */
    @GetMapping("/by-date-time")
    public List<SummaryByDateTimeOnlyDTO> byDateTimeOnly() {
        return aggregatorService.groupByDateTime(metricService.processTask());
    }

    /**
     * Collects metric data by metric type
     *
     * @return
     */
    @GetMapping("/by-metric")
    public MetricAggDTO byMetric() {
        return aggregatorService.groupByMetric(metricService.processTask());
    }
}

package spongecell.backend.metricsagregatator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spongecell.backend.metricsagregatator.dto.MetricAggDTO;
import spongecell.backend.metricsagregatator.dto.SummaryByBrandDTO;
import spongecell.backend.metricsagregatator.dto.SummaryByDateBrandDTO;
import spongecell.backend.metricsagregatator.dto.SummaryByDateTimeOnlyDTO;
import spongecell.backend.metricsagregatator.service.AggregatorService;
import spongecell.backend.metricsagregatator.service.MetricService;

import java.util.List;


/**
 * Controller for handling requests for metrics from backend
 * <p>
 * Created by abyakimenko on 04.03.2019.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/aggregate")
public class AggregatorController {

    private final MetricService metricService;
    private final AggregatorService aggregatorService;

    /**
     * Collects data by brand
     *
     * @return List<SummaryByBrandDTO>
     */
    @GetMapping("/by-brand")
    public List<SummaryByBrandDTO> byBrand() {
        return aggregatorService.groupByBrand(metricService.processTask());
    }

    /**
     * Collects metric data by datetime and brand desc
     *
     * @return List<SummaryByDateBrandDTO>
     */
    @GetMapping("/by-date-brand")
    public List<SummaryByDateBrandDTO> byDateBrand() {
        return aggregatorService.groupByDateBrand(metricService.processTask());
    }

    /**
     * Collects metric data by datetime asc
     *
     * @return List<SummaryByDateTimeOnlyDTO>
     */
    @GetMapping("/by-date-time")
    public List<SummaryByDateTimeOnlyDTO> byDateTimeOnly() {
        return aggregatorService.groupByDateTime(metricService.processTask());
    }

    /**
     * Collects metric data by metric type
     *
     * @return MetricAggDTO
     */
    @GetMapping("/by-metric")
    public MetricAggDTO byMetric() {
        return aggregatorService.groupByMetric(metricService.processTask());
    }
}

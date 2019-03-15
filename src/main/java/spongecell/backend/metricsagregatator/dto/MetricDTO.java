package spongecell.backend.metricsagregatator.dto;

import lombok.Data;

/**
 * Simple data from metric
 */
@Data
public class MetricDTO {
    private MetricType metric;
    private long count;
}
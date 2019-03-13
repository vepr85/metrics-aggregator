package spongecell.backend.metricsagregatator.dto;

import lombok.Data;

@Data
public class MetricDTO {
    private String metric;
    private long count;
}
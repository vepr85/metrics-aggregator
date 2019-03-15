package spongecell.backend.metricsagregatator.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Complex container for metrics
 */
@Data
public class BrandMetricsDTO {
    private int brandId;
    private List<MetricDTO> metrics = new ArrayList<>();
    private ZonedDateTime dateTime;
}

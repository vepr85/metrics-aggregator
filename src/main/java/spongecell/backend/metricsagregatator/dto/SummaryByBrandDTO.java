package spongecell.backend.metricsagregatator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by abyakimenko on 04.03.2019.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryByBrandDTO {
    private int brandId;
    private String brandName;
    private MetricAggDTO metrics;
}

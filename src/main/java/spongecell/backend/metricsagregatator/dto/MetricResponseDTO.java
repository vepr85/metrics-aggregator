package spongecell.backend.metricsagregatator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abyakimenko on 04.03.2019.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricResponseDTO {
    private int size;
    private List<BrandMetricsDTO> brandMetrics = new ArrayList<>();

    public static MetricResponseDTO emptyResponse() {
        return MetricResponseDTO
                .builder()
                .size(0)
                .brandMetrics(new ArrayList<>())
                .build();
    }
}

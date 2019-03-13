package spongecell.backend.metricsagregatator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Created by abyakimenko on 04.03.2019.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummaryByDateTimeOnlyDTO {
    private ZonedDateTime dateTime;
    private MetricAggDTO metrics;

    public static SummaryByDateTimeOnlyDTO emptyEntity() {
        return SummaryByDateTimeOnlyDTO
                .builder()
                .dateTime(ZonedDateTime.now())
                .metrics(MetricAggDTO.emptyEntity())
                .build();
    }
}

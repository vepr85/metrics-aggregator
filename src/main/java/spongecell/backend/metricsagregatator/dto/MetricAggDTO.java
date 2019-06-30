package spongecell.backend.metricsagregatator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricAggDTO {
    private long impression;

    private long click;

    private long interaction;

    public void summImpression(long impression) {
        this.impression += impression;
    }

    public void summClick(long click) {
        this.click += click;
    }

    public void summInteraction(long interaction) {
        this.interaction += interaction;
    }

    public static MetricAggDTO emptyEntity() {
        return MetricAggDTO
                .builder()
                .click(0)
                .impression(0)
                .interaction(0)
                .build();
    }
}

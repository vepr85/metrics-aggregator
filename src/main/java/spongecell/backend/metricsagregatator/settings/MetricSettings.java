package spongecell.backend.metricsagregatator.settings;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by abyakimenko on 13.03.2019.
 */
@Data
@Component
@ConfigurationProperties(prefix = "metric")
public class MetricSettings {
    private String url;
    private Long checkRange;
    private Integer workers;
}

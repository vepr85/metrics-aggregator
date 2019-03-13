package spongecell.backend.metricsagregatator.dto;

/**
 * Created by abyakimenko on 07.03.2019.
 */
public enum MetricType {
    CLICK("click"),
    INTERACTION("interaction"),
    IMPRESSION("impression");

    private String desc;

    MetricType(String desc) {
        this.desc = desc;
    }
}

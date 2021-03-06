package spongecell.backend.metricsagregatator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MetricsAgregatatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetricsAgregatatorApplication.class, args);
    }
}

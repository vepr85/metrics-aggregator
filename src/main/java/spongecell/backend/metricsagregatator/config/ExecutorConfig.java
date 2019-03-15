package spongecell.backend.metricsagregatator.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by abyakimenko on 05.03.2019.
 */
@Configuration
@RequiredArgsConstructor
public class ExecutorConfig {

    private final MetricSettings settings;

    @Bean("fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(settings.getWorkers());
    }
}

package spongecell.backend.metricsagregatator.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spongecell.backend.metricsagregatator.settings.MetricSettings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by abyakimenko on 05.03.2019.
 */
@Configuration
@RequiredArgsConstructor
public class ExecutorConfiguration {

    private final MetricSettings settings;

    @Bean("fixedThreadPool")
    @Qualifier("fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(settings.getWorkers());
    }

    @Bean
    @Qualifier("taskLatch")
    public CountDownLatch latch() {
        return new CountDownLatch(settings.getWorkers());
    }
}

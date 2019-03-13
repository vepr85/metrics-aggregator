package spongecell.backend.metricsagregatator.controller;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;

@Getter
@Setter
public class SharedObject {
    private LocalTime startTime;
    private CountDownLatch countDownLatch;

    public SharedObject(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}